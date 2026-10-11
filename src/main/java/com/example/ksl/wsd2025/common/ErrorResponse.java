package com.example.ksl.wsd2025.common;

// 에러 응답 모양: {"statusCode":404,"message":"..."} (3주차 슬라이드)
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
