package com.example.ksl.wsd2025.common;

/**
 * 오류 응답 포맷 (3주차 슬라이드): {"statusCode":404,"message":"..."}
 */
public record ErrorResponse(int statusCode, String message) {
}
