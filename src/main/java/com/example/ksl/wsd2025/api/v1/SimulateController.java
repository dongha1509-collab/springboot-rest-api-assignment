package com.example.ksl.wsd2025.api.v1;

import com.example.ksl.wsd2025.common.ApiException;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 5xx 응답 코드 시연용 (과제의 8개 API에는 포함되지 않음)
 */
@RestController
@RequestMapping("/api/v1/simulate")
public class SimulateController {

    // 처리되지 않은 예외 -> GlobalExceptionHandler 가 500 으로 변환
    @GetMapping("/500")
    public String internalError() {
        throw new IllegalStateException("의도적인 서버 오류");
    }

    // 서비스 점검 중 -> 503
    @GetMapping("/503")
    public String unavailable() {
        throw new ApiException(HttpStatus.SERVICE_UNAVAILABLE, "서비스 점검 중입니다. 잠시 후 다시 시도해 주세요.");
    }
}
