package com.example.demo.infrastructure.filter.dto;

import java.nio.charset.StandardCharsets;
import org.springframework.web.util.ContentCachingRequestWrapper;
import org.springframework.web.util.ContentCachingResponseWrapper;

public record HttpResponseInfo(
    String method,
    String uri,
    int status,
    long duration,
    String body
) {
    private static final int MAX_BODY_LENGTH = 1000;

    public static HttpResponseInfo from(ContentCachingRequestWrapper request,
                                 ContentCachingResponseWrapper response,
                                 long duration) {
        return new HttpResponseInfo(
            request.getMethod(),
            request.getRequestURI(),
            response.getStatus(),
            duration,
            truncate(extractBody(response))
        );
    }

    public boolean isClientError() {
        return status >= 400 && status < 500;
    }

    public boolean isServerError() {
        return status >= 500;
    }

    private static String extractBody(ContentCachingResponseWrapper response) {
        byte[] content = response.getContentAsByteArray();
        if (content.length == 0) {
            return "";
        }
        return new String(content, StandardCharsets.UTF_8);
    }

    private static String truncate(String text) {
        if (text.length() <= MAX_BODY_LENGTH) {
            return text;
        }
        return text.substring(0, MAX_BODY_LENGTH) + "...(truncated)";
    }
}
