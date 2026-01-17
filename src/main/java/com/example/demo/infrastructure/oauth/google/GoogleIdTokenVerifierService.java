package com.example.demo.infrastructure.oauth.google;

import com.example.demo.application.oauth.IdTokenVerifier;
import com.example.demo.application.dto.OauthUserInfo;
import com.google.api.client.googleapis.auth.oauth2.GoogleIdToken;
import com.google.api.client.googleapis.auth.oauth2.GoogleIdTokenVerifier;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.gson.GsonFactory;
import java.util.List;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class GoogleIdTokenVerifierService implements IdTokenVerifier {
    private final GoogleIdTokenVerifier verifier;

    public GoogleIdTokenVerifierService(@Value("${oauth.google.client-id}") String clientId) {
        this.verifier = new GoogleIdTokenVerifier.Builder(
            new NetHttpTransport(),
            GsonFactory.getDefaultInstance()
        ).setAudience(List.of(clientId)).build();
    }

    public OauthUserInfo verify(String idToken) {
        try {
            GoogleIdToken token = verifier.verify(idToken);
            if (token == null) {
                throw new IllegalArgumentException("유효하지 않은 ID토큰입니다.");
            }

            var payload = token.getPayload();

            String iss = payload.getIssuer();
            if (!"https://accounts.google.com".equals(iss) && !"accounts.google.com".equals(iss)) {
                throw new IllegalArgumentException("유효하지 않은 발행자: " + iss);
            }

            String providerId = payload.getSubject();
            String email = payload.getEmail();
            String picture = (String) payload.get("picture");

            return new OauthUserInfo(providerId, email, picture);
        } catch (Exception e) {
            throw new IllegalArgumentException("ID토큰 검증에 실패했습니다.", e);
        }
    }
}
