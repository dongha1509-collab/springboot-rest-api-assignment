package com.example.ksl.wsd2025.config;


import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

import java.util.UUID;

@Component
public class LoggingInterceptor implements HandlerInterceptor {

    private static final String START_TIME = "startTime";
    private static final String REQUEST_ID = "requestId";

    @Override
    public boolean preHandle(
            HttpServletRequest request,
            HttpServletResponse response,
            Object handler
    ) {
        String requestId = UUID.randomUUID().toString().substring(0, 8);
        request.setAttribute(START_TIME, System.currentTimeMillis());
        request.setAttribute(REQUEST_ID, requestId);
        response.setHeader("X-Request-Id", requestId);

        String method = request.getMethod();
        String uri = request.getRequestURI();
        String query = request.getQueryString();

        System.out.println("[LoggingInterceptor][" + requestId + "] 요청 시작: " + method + " " + uri +
                (query != null ? "?" + query : ""));
        return true; // false로 하면 컨트롤러로 안 넘어감
    }

    @Override
    public void afterCompletion(
            HttpServletRequest request,
            HttpServletResponse response,
            Object handler,
            Exception ex
    ) {
        Long start = (Long) request.getAttribute(START_TIME);
        long elapsed = start != null ? System.currentTimeMillis() - start : -1;

        System.out.println("[LoggingInterceptor][" + request.getAttribute(REQUEST_ID) + "] 요청 완료: status="
                + response.getStatus() + ", " + elapsed + "ms");
    }
}
