package com.example.demo.infrastructure.filter.dto;

import org.springframework.web.util.ContentCachingRequestWrapper;

public record HttpRequestInfo(
    String method,
    String uri,
    String query
) {
    public static HttpRequestInfo from(ContentCachingRequestWrapper request) {
        return new HttpRequestInfo(
            request.getMethod(),
            request.getRequestURI(),
            request.getQueryString()
        );
    }
}
