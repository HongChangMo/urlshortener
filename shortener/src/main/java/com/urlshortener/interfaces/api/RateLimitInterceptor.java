package com.urlshortener.interfaces.api;

import com.urlshortener.infrastructure.ratelimit.RateLimitService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

@Component
@RequiredArgsConstructor
public class RateLimitInterceptor implements HandlerInterceptor {

    private final RateLimitService rateLimitService;

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) {
        String ip = extractIp(request);
        String method = request.getMethod();
        String uri = request.getRequestURI();

        if ("POST".equals(method) && uri.contains("/data/shorten")) {
            rateLimitService.checkShorten(ip);
        } else if ("GET".equals(method)) {
            rateLimitService.checkRedirect(ip);
        }
        return true;
    }

    private String extractIp(HttpServletRequest request) {
        String forwarded = request.getHeader("X-Forwarded-For");
        if (forwarded != null && !forwarded.isBlank()) {
            return forwarded.split(",")[0].trim();
        }
        return request.getRemoteAddr();
    }
}
