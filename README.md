# Spring Boot REST API 과제

Spring Boot 3.5.7 / Java 21 / Gradle
DB 없이 메모리에 저장함 (서버 끄면 데이터 사라짐)
과제 코드는 `/api/v1`, v2랑 v3은 수업 때 받은 코드 그대로

## 실행

```
./gradlew bootRun     # http://localhost:8080
./gradlew test
```

## API 8개 (`/api/v1/items`)

| 메소드 | 주소 | 하는 일 | 응답 코드 |
|---|---|---|---|
| POST | `/items` | 1개 생성 | 201, 400, 409 |
| POST | `/items/bulk` | 여러 개 생성 | 201, 400, 409 |
| GET | `/items` | 전체 조회 (`?keyword=`로 검색) | 200 |
| GET | `/items/{id}` | 1개 조회 | 200, 404 |
| PUT | `/items/{id}` | 전체 교체 (name, price 둘 다) | 200, 400, 404 |
| PUT | `/items/{id}/price` | 가격만 교체 | 200, 400, 404 |
| DELETE | `/items/{id}` | 1개 삭제 | 204, 404 |
| DELETE | `/items?confirm=true` | 전체 삭제 | 204, 400 |

5xx 확인용 주소도 있음: `GET /api/v1/simulate/500`, `GET /api/v1/simulate/503`

## 미들웨어

`LoggingInterceptor`로 요청 올 때랑 끝날 때 로그 찍음 (상태 코드, 걸린 시간)

```
[LoggingInterceptor] request start: POST /api/v1/items
[LoggingInterceptor] request end, status=201, 12ms
```

## 응답 코드

- 2xx: 200, 201, 204
- 4xx: 400, 404, 409
- 5xx: 500, 503

## 테스트

테스트 코드: `./gradlew test`
직접 호출해보기 (앱 켜 놓고 PowerShell에서):

```
.\test-requests.ps1 success   # 201, 200, 204
.\test-requests.ps1 4xx       # 400, 404, 409
.\test-requests.ps1 5xx       # 500, 503
```

## 코드 위치

- `api/v1/ItemController.java` : API 8개
- `common/GlobalExceptionHandler.java` : 에러를 {statusCode, message} 모양으로 바꿔줌
- `config/LoggingInterceptor.java` : 미들웨어
