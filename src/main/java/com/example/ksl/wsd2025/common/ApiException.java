package com.example.ksl.wsd2025.common;

// 직접 던지는 에러 (상태 코드 + 메시지)
public class ApiException extends RuntimeException {

    private int status;

    public ApiException(int status, String message) {
        super(message);
        this.status = status;
    }

    public int getStatus() {
        return status;
    }
}
