package com.example.demo.application.user;

import com.example.demo.application.dto.OauthUserInfo;
import com.example.demo.application.dto.UserInfo;
import com.example.demo.domain.InvitationCode;
import com.example.demo.domain.Nickname;
import com.example.demo.domain.NicknameGenerator;
import com.example.demo.domain.Provider;
import com.example.demo.domain.RandomBytesSource;
import com.example.demo.domain.RefreshTokenRepository;
import com.example.demo.domain.User;
import com.example.demo.domain.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Slf4j
public class UserService {

    private final UserRepository userRepository;
    private final RandomBytesSource randomBytesSource;
    private final NicknameGenerator nicknameGenerator;
    private final RefreshTokenRepository refreshTokenRepository;

    @Transactional(readOnly = true)
    public UserInfo getUserInfo(Long userId) {
        User user = findUserById(userId);
        return UserInfo.from(user);
    }

    @Transactional
    public User findOrCreateUser(Provider provider, OauthUserInfo oauthUserInfo) {
        return userRepository.findByProviderAndProviderId(provider, oauthUserInfo.providerId())
            .orElseGet(() -> createUser(provider, oauthUserInfo));
    }

    private User createUser(Provider provider, OauthUserInfo oauthUserInfo) {
        InvitationCode invitationCode = InvitationCode.generate(randomBytesSource);
        Nickname nickname = nicknameGenerator.generate();

        User user = new User(
            nickname,
            invitationCode,
            oauthUserInfo.email(),
            null,
            provider,
            oauthUserInfo.providerId()
        );

        log.info("새로운 유저가 생성되었습니다. providerId: {}, nickname: {}", user.getProviderId(), user.getNickname());

        return userRepository.save(user);
    }

    @Transactional
    public void withdrawUser(Long userId) {
        userRepository.findById(userId).ifPresent(user -> {
            Provider provider = user.getProvider();
            String providerId = user.getProviderId();

            userRepository.deleteById(userId);
            refreshTokenRepository.deleteByUserId(userId);

            log.info("유저가 탈퇴했습니다. userId: {}, provider: {}, providerId: {}", userId, provider, providerId);
        });
    }

    @Transactional
    public String changeNickname(Long userId, String nickname) {
        User user = findUserById(userId);

        if (userRepository.existsByNickname_Value(nickname)) {
            throw new IllegalArgumentException("중복되는 닉네임입니다.");
        }

        try {
            user.changeNickname(new Nickname(nickname));
            userRepository.saveAndFlush(user);
        } catch (DataIntegrityViolationException e) {
            throw new IllegalArgumentException("중복되는 닉네임입니다.");
        }

        return user.getNickname();
    }

    private User findUserById(Long userId) {
        return userRepository.findById(userId)
            .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 사용자입니다."));
    }
}
