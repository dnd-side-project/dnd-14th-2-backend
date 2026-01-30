package com.example.demo.application.user;

import com.example.demo.application.dto.OauthUserInfo;
import com.example.demo.application.dto.UserInfo;
import com.example.demo.domain.RefreshTokenRepository;
import com.example.demo.domain.Nickname;
import com.example.demo.domain.NicknameGenerator;
import com.example.demo.domain.Provider;
import com.example.demo.domain.User;
import com.example.demo.domain.UserRepository;
import java.time.Clock;
import java.time.LocalDateTime;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Slf4j
public class UserService {

    private static final int MAX_RETRY = 10;

    private final UserRepository userRepository;
    private final NicknameGenerator nicknameGenerator;
    private final InvitationCodeGenerator invitationCodeGenerator;
    private final RefreshTokenRepository refreshTokenRepository;
    private final Clock clock;

    @Transactional(readOnly = true)
    public UserInfo getUserInfo(Long userId) {
        User user = findUserById(userId);
        return new UserInfo(user.getId(), user.getNickname(), user.getLevel(), user.getProfile());
    }

    @Transactional
    public UserInfo login(Provider provider, OauthUserInfo oauthUserInfo) {
        User user = userRepository.findByProviderAndProviderId(provider, oauthUserInfo.providerId())
            .orElseGet(() -> createUserWithRetries(provider, oauthUserInfo));
        return new UserInfo(user.getId(), user.getNickname(), user.getLevel(), user.getProfile());
    }

    private User createUserWithRetries(Provider provider, OauthUserInfo info) {
        for (int retry = 1; retry <= MAX_RETRY; retry++) {
            User user = new User(
                info.email(),
                info.picture(),
                provider,
                info.providerId()
            );

            Nickname nickname = nicknameGenerator.generate();
            String inviteCode = invitationCodeGenerator.generate(nickname.value());

            user.registerNickname(nickname);
            user.registerInvitationCode(inviteCode);

            try {
                // flush를 통해 유니크 제약 조건 검사
                return userRepository.saveAndFlush(user);
            } catch (DataIntegrityViolationException e) {
                // 동시 로그인 경쟁으로 provider/providerId 유저가 이미 생성됐을 수 있음
                Optional<User> existing = userRepository.findByProviderAndProviderId(provider, info.providerId());
                if (existing.isPresent()) {
                    return existing.get();
                }
                // 아직 없으면 닉네임/초대코드 유니크 충돌 가능성이 높으니 retry
                log.warn("유저 닉네임/초대코드 중복으로 인한 재시도 횟수: {}/{}, userProvider: {}, userProviderId: {}",
                    retry, MAX_RETRY, provider.name(), info.providerId(), e);
            }
        }
        throw new IllegalStateException(
            String.format(
                "닉네임/초대코드 중복으로 인해 유저 생성에 실패했습니다. userProvider: %s, userProviderId: %s",
                provider.name(), info.providerId()
            )
        );
        return invitationCode;
    }

    private void validateIsDuplicateNickname(String nickname) {
        if (userRepository.existsByNickname(nickname)) {
            throw new IllegalArgumentException("이미 존재하는 닉네임입니다.");
        }
    }

    @Transactional
    public void withdrawUser(Long userId) {
        userRepository.findById(userId).ifPresent(user -> {
            user.withdraw(LocalDateTime.now(clock));
            refreshTokenRepository.deleteByUserId(userId);
        });
    }

    @Transactional
    public void changeNickname(Long userId, String nickname) {
        User user = findUserById(userId);

        user.changeNickname(nickname);
    }

    private User findUserById(Long userId) {
        return userRepository.findById(userId)
            .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 사용자입니다."));
    }
}
