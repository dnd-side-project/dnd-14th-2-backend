package com.example.demo.application;

import com.example.demo.application.dto.TokenResponse;
import com.example.demo.application.oauth.AuthService;
import com.example.demo.domain.*;
import com.example.demo.util.AbstractIntegrationTest;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

class AuthServiceTest extends AbstractIntegrationTest {

    @Autowired
    private AuthService sut;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private RefreshTokenRepository refreshTokenRepository;

    @Test
    void 토큰을_발급_받을_수_있다() {
        // given
        User user = new User("test@email.com", "http://test.jpg", Provider.GOOGLE, "test-provider-id");
        User savedUser = userRepository.save(user);

        // when
        TokenResponse tokenResponse = sut.issueTokens(savedUser);

        // then
        Optional<RefreshToken> refreshToken = refreshTokenRepository.findByUserId(savedUser.getId());
        assertThat(refreshToken.get())
                .isNotNull();
        assertThat(refreshToken.get())
                .extracting("token")
                .isEqualTo(tokenResponse.refreshToken());
    }
}
