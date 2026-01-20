package com.example.demo.infrastructure.interceptor;

import com.example.demo.application.oauth.TokenProvider;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.resource.ResourceHttpRequestHandler;

@Component
@RequiredArgsConstructor
public class LoginInterceptor implements HandlerInterceptor {

    private final TokenProvider tokenProvider;

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler)
            throws Exception {

        if (handler instanceof ResourceHttpRequestHandler) {
            return true;
        }

        String authorization = request.getHeader("Authorization");

        if (authorization == null || !authorization.startsWith("Bearer ")) {
            // 로그인이 필요 시 401로 응답
            response.sendError(HttpServletResponse.SC_UNAUTHORIZED);
            return false;
        }

        String token = authorization.substring("Bearer ".length());
        Long userId = tokenProvider.validateToken(token);

        request.setAttribute("userId", userId);
        return true;
    }
}
