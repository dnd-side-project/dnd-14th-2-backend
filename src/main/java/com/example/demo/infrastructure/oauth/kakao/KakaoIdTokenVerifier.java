package com.example.demo.infrastructure.oauth.kakao;

import com.example.demo.application.IdTokenVerifier;
import com.example.demo.application.dto.OauthUserInfo;
import com.nimbusds.jwt.JWTClaimsSet;
import com.nimbusds.jwt.SignedJWT;
import org.springframework.stereotype.Component;

@Component
public class KakaoIdTokenVerifier implements IdTokenVerifier {
    @Override
    public OauthUserInfo verify(String idToken) {
        try {
            SignedJWT jwt = SignedJWT.parse(idToken);
            JWTClaimsSet claims = jwt.getJWTClaimsSet();
            String sub = claims.getSubject();
            String email = claims.getStringClaim("email");
            String picture = claims.getStringClaim("picture");
            return new OauthUserInfo(sub, email, picture);
        } catch (Exception e) {
            throw new IllegalArgumentException("Invalid id_token", e);
        }
    }
}
