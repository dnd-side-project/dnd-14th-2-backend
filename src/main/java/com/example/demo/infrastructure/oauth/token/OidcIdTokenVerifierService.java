package com.example.demo.infrastructure.oauth.token;

import com.example.demo.application.dto.OauthUserInfo;
import com.example.demo.application.oauth.IdTokenVerifier;
import com.example.demo.domain.Provider;
import com.example.demo.infrastructure.oauth.google.OidcVerifyProperties;
import com.nimbusds.jwt.JWTClaimsSet;
import java.util.EnumMap;
import java.util.Map;
import org.springframework.stereotype.Component;

@Component
public class OidcIdTokenVerifierService implements IdTokenVerifier {

    private final Map<Provider, NimbusOidcIdTokenProcessor> processors = new EnumMap<>(Provider.class);

    public OidcIdTokenVerifierService(OidcVerifyProperties properties) {
        processors.put(Provider.GOOGLE, new NimbusOidcIdTokenProcessor(
            properties.google().issuer(),
            properties.google().audience(),
            properties.google().jwksUri(),
            java.time.Clock.systemUTC()
        ));
    }

    @Override
    public OauthUserInfo verifyAndGetUserInfo(Provider provider, String idToken) {
        NimbusOidcIdTokenProcessor processor = processors.get(provider);
        if (processor == null) throw new IllegalArgumentException("지원하지 않는 provider: " + provider);

        JWTClaimsSet claims = processor.verifyAndGetClaims(idToken);
        String providerId = claims.getSubject();
        String email = safeString(claims, "email");
        String picture = safeString(claims, "picture");

        return new OauthUserInfo(providerId, email, picture);
    }

    private String safeString(JWTClaimsSet claims, String key) {
        try {
            return claims.getStringClaim(key);
        } catch (Exception e) {
            return null;
        }
    }
}
