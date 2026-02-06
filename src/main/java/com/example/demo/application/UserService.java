package com.example.demo.application;

import com.example.demo.application.dto.UserInfo;
import com.example.demo.domain.RefreshTokenRepository;
import com.example.demo.domain.User;
import com.example.demo.domain.UserRepository;
import java.time.Clock;
import java.time.LocalDateTime;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@RequiredArgsConstructor
@Service
public class UserService {
    private final UserRepository userRepository;
    private final RefreshTokenRepository refreshTokenRepository;
    private final Clock clock;

    @Transactional(readOnly = true)
    public UserInfo getUserInfo(Long userId) {
        User user = userRepository.findById(userId)
            .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 사용자입니다."));
        return new UserInfo(user.getId(), user.getNickname(), user.getLevel(), user.getProfile());
    }

    @Transactional
    public void withdrawUser(Long userId) {
        userRepository.findById(userId).ifPresent(user -> {
            user.withdraw(LocalDateTime.now(clock));
            refreshTokenRepository.deleteByUserId(userId);
        });
    }
}
