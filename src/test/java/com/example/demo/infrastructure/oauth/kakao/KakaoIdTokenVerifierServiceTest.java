package com.example.demo.infrastructure.oauth.kakao;

import com.example.demo.application.dto.OauthUserInfo;
import com.nimbusds.jose.JWSAlgorithm;
import com.nimbusds.jose.JWSHeader;
import com.nimbusds.jose.crypto.RSASSASigner;
import com.nimbusds.jose.jwk.RSAKey;
import com.nimbusds.jose.jwk.source.JWKSource;
import com.nimbusds.jose.proc.SecurityContext;
import com.nimbusds.jwt.JWTClaimsSet;
import com.nimbusds.jwt.SignedJWT;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.security.KeyPairGenerator;
import java.security.interfaces.RSAPublicKey;
import java.time.Duration;
import java.util.Date;
import java.util.List;

import static com.example.demo.common.config.RestClientConfig.KAKAO_ISSUER;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;


@ExtendWith(MockitoExtension.class)
class KakaoIdTokenVerifierServiceTest {

    private KakaoIdTokenVerifierService kakaoIdTokenVerifierService;

    @Mock
    private JWKSource<SecurityContext> kakaoJwkSource;

    @Mock
    private KakaoOauthProperties kakaoOauthProperties;

    private RSAKey rsaKey;
    private final String clientId = "test-client-id";

    @BeforeEach
    void setUp() throws Exception {
        // 1. 테스트용 RSA 키쌍 생성
        KeyPairGenerator gen = KeyPairGenerator.getInstance("RSA");
        gen.initialize(2048);
        var keyPair = gen.generateKeyPair();

        rsaKey = new RSAKey.Builder((RSAPublicKey) keyPair.getPublic())
                .privateKey(keyPair.getPrivate())
                .keyID("kid-1")
                .build();

        kakaoIdTokenVerifierService = new KakaoIdTokenVerifierService(kakaoJwkSource, kakaoOauthProperties);
    }

    @Test
    void 유효한_ID_토큰을_검증하고_OauthUserInfo를_반환한다() throws Exception {
        // given
        String sub = "kakao";
        String email = "test@email.com";
        String picture = "https://profile.png";
        String idToken = createMockIdToken(sub, email, picture);                // 토큰 생성
        when(kakaoJwkSource.get(any(), any())).thenReturn(List.of(rsaKey));     // 공개키 반환
        when(kakaoOauthProperties.clientId()).thenReturn(clientId);

        // when
        OauthUserInfo userInfo = kakaoIdTokenVerifierService.verify(idToken);

        // then
        assertThat(userInfo.providerId()).isEqualTo(sub);
        assertThat(userInfo.email()).isEqualTo(email);
        assertThat(userInfo.picture()).isEqualTo(picture);
    }


    @Test
    void 알고리즘이_일치하지_않는_경우_예외를_던진다() throws Exception {
        // given: RS256이 아닌 HS256으로 서명된 토큰 생성
        JWTClaimsSet claimsSet = createDefaultClaims().build();
        SignedJWT signedJWT = new SignedJWT(
                new JWSHeader.Builder(JWSAlgorithm.HS256).build(), // RS256 아님
                claimsSet
        );
        signedJWT.sign(new com.nimbusds.jose.crypto.MACSigner("secret-key-64-characters-long-for-hs256-test"));
        String invalidAlgToken = signedJWT.serialize();

        // when & then
        assertThatThrownBy(() -> kakaoIdTokenVerifierService.verify(invalidAlgToken))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void 유효하지_않은_발행자일_경우_예외를_던진다() throws Exception {
        // given: 잘못된 issuer 설정
        String wrongToken = createTokenWithCustomClaims(createDefaultClaims()
                .issuer("https://wrong-issuer.com")
                .build());

        // when & then
        assertThatThrownBy(() -> kakaoIdTokenVerifierService.verify(wrongToken))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void 클라이언트_ID가_일치하지_않으면_예외를_던진다() throws Exception {
        // given: 다른 audience(client_id) 설정
        String wrongToken = createTokenWithCustomClaims(createDefaultClaims()
                .audience("wrong-client-id")
                .build());

        // when & then
        assertThatThrownBy(() -> kakaoIdTokenVerifierService.verify(wrongToken))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void 만료된_토큰일_경우_예외를_던진다() throws Exception {
        // given: 만료 시간을 10분 전으로 설정
        Date past = new Date(new Date().getTime() - Duration.ofMinutes(10).toMillis());
        String expiredToken = createTokenWithCustomClaims(createDefaultClaims()
                .expirationTime(past)
                .build());

        // when & then
        assertThatThrownBy(() -> kakaoIdTokenVerifierService.verify(expiredToken))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void 검증_중_형식이_잘못된_토큰이면_IllegalArgumentException을_던진다() {
        // given: 파싱 자체가 불가능한 문자열
        String malformedToken = "this.is.not.a.jwt";

        // when & then
        assertThatThrownBy(() -> kakaoIdTokenVerifierService.verify(malformedToken))
                .isInstanceOf(IllegalArgumentException.class);
    }

    private String createMockIdToken(String sub, String email, String picture) throws Exception {
        JWTClaimsSet claimsSet = new JWTClaimsSet.Builder()
                .issuer(KAKAO_ISSUER)
                .audience(clientId)
                .subject(sub)
                .expirationTime(new Date(new Date().getTime() + 60 * 1000))
                .issueTime(new Date())
                .claim("email", email)
                .claim("picture", picture)
                .build();

        SignedJWT signedJWT = new SignedJWT(
                new JWSHeader.Builder(JWSAlgorithm.RS256).keyID(rsaKey.getKeyID()).build(),
                claimsSet
        );

        signedJWT.sign(new RSASSASigner(rsaKey));
        return signedJWT.serialize();
    }

    // 기본 클레임 생성
    private JWTClaimsSet.Builder createDefaultClaims() {
        return new JWTClaimsSet.Builder()
                .issuer(KAKAO_ISSUER)
                .audience(clientId)
                .subject("user-123")
                .expirationTime(new Date(new Date().getTime() + 60 * 1000))
                .claim("email", "test@email.com")
                .claim("picture", "https://profile.png");
    }

    // 특정 클레임으로 서명된 토큰 생성
    private String createTokenWithCustomClaims(JWTClaimsSet claims) throws Exception {
        SignedJWT signedJWT = new SignedJWT(
                new JWSHeader.Builder(JWSAlgorithm.RS256).keyID(rsaKey.getKeyID()).build(),
                claims
        );
        signedJWT.sign(new RSASSASigner(rsaKey));
        return signedJWT.serialize();
    }
}