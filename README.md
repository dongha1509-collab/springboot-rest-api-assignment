# Spring Boot REST API 과제

DB는 안 쓰고 메모리에 저장함. 서버 끄면 데이터 없어짐.
/api/v1 이 과제 코드고 v2, v3은 수업 때 받은 코드 그대로임.

## 실행

    ./gradlew bootRun     (http://localhost:8080)
    ./gradlew test

Java 21 필요

## API (/api/v1/items)

    POST    /items                생성 (201, 400, 409)
    POST    /items/bulk           여러 개 한번에 생성 (201, 400, 409)
    GET     /items                전체 조회 (200)
    GET     /items/{id}           1개 조회 (200, 404)
    PUT     /items/{id}           전체 교체, name이랑 price 둘 다 필요 (200, 400, 404)
    PUT     /items/{id}/price     가격만 교체 (200, 400, 404)
    DELETE  /items/{id}           1개 삭제 (204, 404)
    DELETE  /items?confirm=true   전체 삭제 (204, 400)

5xx 확인용으로 /api/v1/simulate/500, /api/v1/simulate/503 도 만들어 둠

## 미들웨어

LoggingInterceptor. 요청 오면 로그 찍고, 끝나면 상태 코드랑 걸린 시간 찍음.

    [LoggingInterceptor] request start: POST /api/v1/items
    [LoggingInterceptor] request end, status=201, 12ms

## 응답 코드

- 2xx : 200, 201, 204
- 4xx : 400, 404, 409
- 5xx : 500, 503

## 테스트

앱 켜 놓고 PowerShell에서

    .\test-requests.ps1 success
    .\test-requests.ps1 4xx
    .\test-requests.ps1 5xx

## 코드 위치

- api/v1/ItemController.java : API 8개
- common/GlobalExceptionHandler.java : 에러 응답 만들어줌
- config/LoggingInterceptor.java : 미들웨어
