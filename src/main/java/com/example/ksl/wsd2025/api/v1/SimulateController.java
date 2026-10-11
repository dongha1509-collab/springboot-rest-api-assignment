package com.example.ksl.wsd2025.api.v1;

import com.example.ksl.wsd2025.common.ApiException;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

// 5xx 확인용 (8개 API에는 안 들어감)
@RestController
@RequestMapping("/api/v1/simulate")
public class SimulateController {

    // 일부러 에러 -> 500
    @GetMapping("/500")
    public String error500() {
        throw new RuntimeException("일부러 낸 서버 에러");
    }

    // 점검 중이라 치고 -> 503
    @GetMapping("/503")
    public String error503() {
        throw new ApiException(503, "서비스 점검 중입니다. 잠시 후 다시 시도해 주세요.");
    }
}
