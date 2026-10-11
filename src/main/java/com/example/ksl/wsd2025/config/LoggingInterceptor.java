package com.example.ksl.wsd2025.config;


import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

@Component
public class LoggingInterceptor implements HandlerInterceptor {

    @Override
    public boolean preHandle(
            HttpServletRequest request,
            HttpServletResponse response,
            Object handler
    ) {
        // 시작 시간 저장 (끝날 때 걸린 시간 구하려고)
        request.setAttribute("startTime", System.currentTimeMillis());

        String method = request.getMethod();
        String uri = request.getRequestURI();
        String query = request.getQueryString();

        System.out.println("[LoggingInterceptor] request start: " + method + " " + uri +
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
        long startTime = (Long) request.getAttribute("startTime");
        long time = System.currentTimeMillis() - startTime;

        System.out.println("[LoggingInterceptor] request end, status=" + response.getStatus()
                + ", " + time + "ms");
    }
}
