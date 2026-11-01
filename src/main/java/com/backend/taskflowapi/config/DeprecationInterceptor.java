package com.backend.taskflowapi.config;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

@Component
public class DeprecationInterceptor implements HandlerInterceptor {

    private final String sunsetDate;

    public DeprecationInterceptor(@Value("${app.api.v1.sunset-date}") String sunsetDate){
        this.sunsetDate = sunsetDate;
    }

    @Override
    public boolean preHandle(
            HttpServletRequest request,
            HttpServletResponse response,
            Object handler
    ) {
        response.setHeader("Deprecation", "true");
        response.setHeader("Sunset", sunsetDate);
        return true;
    }
}
