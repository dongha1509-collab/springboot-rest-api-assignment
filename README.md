# Spring Boot REST API 실습 과제 (WSD-2025)

Spring Boot 3.5.7 / Java 21 / Gradle. 데이터는 DB 없이 메모리(`LinkedHashMap`)에 저장합니다.
과제용 구현은 `/api/v1/**` 입니다. (`v2`, `v3`은 수업 실습 코드 그대로)

## 실행

```bash
./gradlew bootRun     # http://localhost:8080
./gradlew test        # 테스트 (MockMvc)
```

## A. API 목록 (POST/GET/PUT/DELETE 각 2개, 총 8개)

| # | Method | URL | 설명 | 응답 코드 |
|---|--------|-----|------|-----------|
| 1 | POST | `/api/v1/items` | 아이템 1개 생성 | 201(+Location), 400, 409(이름 중복) |
| 2 | POST | `/api/v1/items/bulk` | 아이템 여러 개 한 번에 생성 | 201, 400, 409 |
| 3 | GET | `/api/v1/items?keyword=` | 목록 조회 (이름 검색) | 200 |
| 4 | GET | `/api/v1/items/{id}` | 단건 조회 | 200, 404, 400(id가 숫자가 아님) |
| 5 | PUT | `/api/v1/items/{id}` | 전체 교체 (name, price 모두 필요) | 200, 400, 404 |
| 6 | PUT | `/api/v1/items/{id}/price` | 가격 교체 (price만 필요) | 200, 400, 404 |
| 7 | DELETE | `/api/v1/items/{id}` | 1개 삭제 | 204, 404 |
| 8 | DELETE | `/api/v1/items?confirm=true` | 전체 삭제 | 204, 400(confirm 없음) |

5xx 응답 확인용 API (위 8개에는 포함되지 않음):

| Method | URL | 응답 코드 |
|--------|-----|-----------|
| GET | `/api/v1/simulate/500` | 500 (일부러 에러 발생) |
| GET | `/api/v1/simulate/503` | 503 (점검 중이라고 가정) |

## B. 미들웨어

`config/LoggingInterceptor` (`WebConfig`에서 `/api/**`에 등록)
- 요청이 들어올 때 `메서드 URI?쿼리`를 콘솔에 출력합니다.
- 요청이 끝나면 응답 `status`와 처리 시간(ms)을 출력합니다.

```
[LoggingInterceptor] request start: POST /api/v1/items
[LoggingInterceptor] request end, status=201, 12ms
```

## C. 사용한 응답 코드

| 범주 | 코드 |
|------|------|
| 2xx | 200 OK, 201 Created, 204 No Content |
| 4xx | 400 Bad Request, 404 Not Found, 409 Conflict |
| 5xx | 500 Internal Server Error, 503 Service Unavailable |

## D. 응답 형식 (3주차 강의 슬라이드 기준)

성공은 응답 본문을 감싸지 않고 **객체/배열을 그대로** 보내고, 결과는 HTTP 상태 코드로 알려줍니다.
에러는 `common/GlobalExceptionHandler`에서 한 곳에서 `{statusCode, message}` 모양으로 만듭니다.

| 상황 | 상태 코드 | 본문 |
|------|-----------|------|
| 조회·수정 성공 | 200 | 객체 또는 배열 (없으면 `[]`) |
| 등록 성공 | 201 + `Location: /api/v1/items/{id}` | 만들어진 객체 |
| 삭제 성공 | 204 | 없음 |
| 입력 오류 / id 형식 오류 | 400 | `{"statusCode":400,"message":"..."}` |
| 대상 없음 | 404 | `{"statusCode":404,"message":"..."}` |
| 이름 중복 | 409 | `{"statusCode":409,"message":"..."}` |
| 서버 내부 오류 | 500 | `{"statusCode":500,"message":"서버 처리 중 오류가 발생했습니다."}` (에러 내용은 숨김) |
| 점검 중 | 503 | `{"statusCode":503,"message":"..."}` |

성공 예시 (`POST /api/v1/items`)
```json
{ "id": 1, "name": "pen", "price": 1000 }
```
에러 예시 (`GET /api/v1/items/999`)
```json
{ "statusCode": 404, "message": "아이템을 찾을 수 없습니다. id=999" }
```

PUT은 "전체 교체"라서 `name`, `price`를 모두 받습니다. (일부만 수정하는 PATCH는 만들지 않았습니다.)

## 테스트 예시 (curl)

PowerShell에서는 `.\test-requests.ps1` 을 실행하면 아래 호출을 순서대로 보여줍니다.

```bash
curl -i -X POST localhost:8080/api/v1/items -H "Content-Type: application/json" -d '{"name":"pen","price":1000}'     # 201
curl -i -X POST localhost:8080/api/v1/items -H "Content-Type: application/json" -d '{"name":"pen","price":1}'        # 409
curl -i -X POST localhost:8080/api/v1/items -H "Content-Type: application/json" -d '{"name":"","price":-1}'          # 400
curl -i -X POST localhost:8080/api/v1/items/bulk -H "Content-Type: application/json" -d '[{"name":"a","price":1},{"name":"b","price":2}]'  # 201
curl -i localhost:8080/api/v1/items                      # 200
curl -i localhost:8080/api/v1/items/1                    # 200
curl -i localhost:8080/api/v1/items/999                  # 404
curl -i -X PUT localhost:8080/api/v1/items/1 -H "Content-Type: application/json" -d '{"name":"pencil","price":700}'  # 200
curl -i -X PUT localhost:8080/api/v1/items/1/price -H "Content-Type: application/json" -d '{"price":500}'            # 200
curl -i -X DELETE localhost:8080/api/v1/items/1          # 204
curl -i -X DELETE localhost:8080/api/v1/items            # 400
curl -i -X DELETE "localhost:8080/api/v1/items?confirm=true"   # 204
curl -i localhost:8080/api/v1/simulate/500               # 500
curl -i localhost:8080/api/v1/simulate/503               # 503
```

## 프로젝트 구조

```
api/dto, api/request     DTO / 요청 객체
api/v1                   과제용 컨트롤러 (ItemController, SimulateController)
common                   ApiException, ErrorResponse, GlobalExceptionHandler
config                   LoggingInterceptor, WebConfig
```
