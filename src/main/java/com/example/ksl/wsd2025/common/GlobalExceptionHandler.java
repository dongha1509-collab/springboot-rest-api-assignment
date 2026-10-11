package com.example.ksl.wsd2025.common;

import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

// 컨트롤러에서 에러(예외)가 나면 여기서 한 번에 처리해서
// {"statusCode":..., "message":"..."} 모양으로 응답한다.
// (슬라이드의 "오류 처리 미들웨어"와 같은 역할)
@RestControllerAdvice
public class GlobalExceptionHandler {

    // 1) 우리가 직접 던진 에러 (400, 404, 409, 503 ...)
    @ExceptionHandler(ApiException.class)
    public ResponseEntity<ErrorResponse> handleApiException(ApiException e) {
        ErrorResponse body = new ErrorResponse(e.getStatus(), e.getMessage());
        return ResponseEntity.status(e.getStatus()).body(body);
    }

    // 2) JSON 형식이 잘못됐거나 body가 없을 때 -> 400
    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<ErrorResponse> handleBadJson(HttpMessageNotReadableException e) {
        ErrorResponse body = new ErrorResponse(400, "요청 본문(JSON)이 올바르지 않습니다.");
        return ResponseEntity.status(400).body(body);
    }

    // 3) /items/abc 처럼 id에 숫자가 아닌 값이 들어왔을 때 -> 400
    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public ResponseEntity<ErrorResponse> handleBadId(MethodArgumentTypeMismatchException e) {
        ErrorResponse body = new ErrorResponse(400, "잘못된 값입니다: " + e.getName());
        return ResponseEntity.status(400).body(body);
    }

    // 4) 그 외에 예상 못한 에러 -> 500
    // 슬라이드처럼 에러 내용은 응답에 보여주지 않는다.
    @ExceptionHandler(RuntimeException.class)
    public ResponseEntity<ErrorResponse> handleServerError(RuntimeException e) {
        ErrorResponse body = new ErrorResponse(500, "서버 처리 중 오류가 발생했습니다.");
        return ResponseEntity.status(500).body(body);
    }
}
