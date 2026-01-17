package com.example.demo.infrastructure.interceptor;

import com.example.demo.application.TokenProvider;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

@Component
@RequiredArgsConstructor
public class LoginInterceptor implements HandlerInterceptor {

    private final TokenProvider tokenProvider;

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler)
        throws Exception {
        String authorization = request.getHeader("Authorization");

        String uri = request.getRequestURI();
        if (uri.startsWith("/oauth/kakao")|| uri.startsWith("/oauth/google")) {
            return true;
        }

        if (authorization == null || !authorization.startsWith("Bearer ")) {
            // 로그인이 필요하다는 예외
            throw new RuntimeException();
        }

        String token = authorization.substring("Bearer ".length());
        Long userId = tokenProvider.validateToken(token);

        request.setAttribute("userId", userId);
        return true;
    }
}
