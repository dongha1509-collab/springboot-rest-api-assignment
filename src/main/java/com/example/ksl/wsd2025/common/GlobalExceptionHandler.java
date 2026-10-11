package com.example.ksl.wsd2025.common;

import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

// 에러 나면 여기서 {statusCode, message} 모양으로 바꿔서 응답
@RestControllerAdvice
public class GlobalExceptionHandler {

    // 직접 던진 에러 (400, 404, 409, 503 ...)
    @ExceptionHandler(ApiException.class)
    public ResponseEntity<ErrorResponse> handleApiException(ApiException e) {
        ErrorResponse body = new ErrorResponse(e.getStatus(), e.getMessage());
        return ResponseEntity.status(e.getStatus()).body(body);
    }

    // JSON이 이상하거나 body가 없으면 400
    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<ErrorResponse> handleBadJson(HttpMessageNotReadableException e) {
        ErrorResponse body = new ErrorResponse(400, "요청 본문(JSON)이 올바르지 않습니다.");
        return ResponseEntity.status(400).body(body);
    }

    // id에 숫자 아닌 값(/items/abc)이 오면 400
    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public ResponseEntity<ErrorResponse> handleBadId(MethodArgumentTypeMismatchException e) {
        ErrorResponse body = new ErrorResponse(400, "잘못된 값입니다: " + e.getName());
        return ResponseEntity.status(400).body(body);
    }

    // 나머지 에러는 500 (에러 내용은 안 보여줌)
    @ExceptionHandler(RuntimeException.class)
    public ResponseEntity<ErrorResponse> handleServerError(RuntimeException e) {
        ErrorResponse body = new ErrorResponse(500, "서버 처리 중 오류가 발생했습니다.");
        return ResponseEntity.status(500).body(body);
    }
}
