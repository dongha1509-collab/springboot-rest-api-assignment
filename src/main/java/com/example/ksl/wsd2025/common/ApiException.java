package com.example.ksl.wsd2025.common;

// 우리가 직접 던지는 에러 (상태 코드 + 메시지)
// 예) throw new ApiException(404, "아이템을 찾을 수 없습니다.");
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
