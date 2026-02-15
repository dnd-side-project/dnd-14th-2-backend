package com.example.demo.infrastructure.filter;

import com.example.demo.infrastructure.filter.dto.HttpRequestInfo;
import com.example.demo.infrastructure.filter.dto.HttpResponseInfo;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.List;
import java.util.UUID;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.MDC;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;
import org.springframework.web.util.ContentCachingRequestWrapper;
import org.springframework.web.util.ContentCachingResponseWrapper;

@Slf4j
@Component
public class HttpLoggingFilter extends OncePerRequestFilter {

    private static final List<String> SKIP_PATHS = List.of(
        "/actuator", "/swagger-ui", "/v3/api-docs");

    @Override
    protected void doFilterInternal(
        HttpServletRequest request,
        HttpServletResponse response,
        FilterChain filterChain) throws ServletException, IOException {

        String requestId = UUID.randomUUID().toString().substring(0, 8);
        MDC.put("requestId", requestId);
        response.setHeader("X-Request-Id", requestId);

        ContentCachingRequestWrapper wrappedRequest = new ContentCachingRequestWrapper(request);
        ContentCachingResponseWrapper wrappedResponse = new ContentCachingResponseWrapper(response);

        long startTime = System.currentTimeMillis();

        try {
            filterChain.doFilter(wrappedRequest, wrappedResponse);
        } finally {
            long duration = System.currentTimeMillis() - startTime;
            try {
                logRequest(wrappedRequest);
                logResponse(wrappedRequest, wrappedResponse, duration);
                wrappedResponse.copyBodyToResponse();
            } finally {
                MDC.remove("requestId");
            }
        }
    }

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        String path = request.getRequestURI();
        return SKIP_PATHS.stream().anyMatch(path::startsWith);
    }

    private void logRequest(ContentCachingRequestWrapper request) {
        HttpRequestInfo info = HttpRequestInfo.from(request);
        log.info("[REQUEST] {} {} | query={}", info.method(), info.uri(), info.query());
    }

    private void logResponse(ContentCachingRequestWrapper request,
                             ContentCachingResponseWrapper response,
                             long duration) {
        HttpResponseInfo info = HttpResponseInfo.from(request, response, duration);
        if (info.isServerError()) {
            log.error("[SERVER_ERROR] {} {} | status={} | duration={}ms | body={}",
                info.method(), info.uri(), info.status(), info.duration(), info.body());
        } else if (info.isClientError()) {
            log.warn("[CLIENT_ERROR] {} {} | status={} | duration={}ms | body={}",
                info.method(), info.uri(), info.status(), info.duration(), info.body());
        } else {
            log.info("[RESPONSE] {} {} | status={} | duration={}ms",
                info.method(), info.uri(), info.status(), info.duration());
        }
    }
}