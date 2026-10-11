package com.example.ksl.wsd2025.common;

// 에러가 났을 때 내려주는 JSON 모양 (3주차 슬라이드)
// {"statusCode":404,"message":"게시글을 찾을 수 없습니다."}
public class ErrorResponse {

    private int statusCode;
    private String message;

    public ErrorResponse(int statusCode, String message) {
        this.statusCode = statusCode;
        this.message = message;
    }

    public int getStatusCode() {
        return statusCode;
    }

    public String getMessage() {
        return message;
    }
}
