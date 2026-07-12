package com.bullboard;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.servlet.HandlerInterceptor;

import java.time.LocalDateTime;

public class LoggingInterceptor implements HandlerInterceptor {
    private static final Logger log = LoggerFactory.getLogger(LoggingInterceptor.class);
    private static final String START_TIME = "startTime";

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) {
        long startTime = System.currentTimeMillis();
        request.setAttribute(START_TIME, startTime);

        log.info("[API 요청 수행 시작] method={} url={} start={}",
                request.getMethod(),
                request.getRequestURI(),
                LocalDateTime.now());

        return true;
    }

    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex) {
        long endTime = System.currentTimeMillis();
        long startTime = (Long) request.getAttribute(START_TIME);
        long duration = endTime - startTime;

        log.info("[API 요청 수행 완료]  method={} url={} end={} duration={}ms status={}",
                request.getMethod(),
                request.getRequestURI(),
                LocalDateTime.now(),
                duration,
                response.getStatus());
    }
}
