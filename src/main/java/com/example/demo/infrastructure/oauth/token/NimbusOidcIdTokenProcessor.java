package com.example.demo.infrastructure.oauth.token;

import com.nimbusds.jose.JWSAlgorithm;
import com.nimbusds.jose.jwk.source.JWKSource;
import com.nimbusds.jose.jwk.source.RemoteJWKSet;
import com.nimbusds.jose.proc.JWSKeySelector;
import com.nimbusds.jose.proc.SecurityContext;
import com.nimbusds.jose.proc.JWSVerificationKeySelector;
import com.nimbusds.jwt.JWTClaimsSet;
import com.nimbusds.jwt.proc.*;

import java.net.URL;
import java.time.Clock;
import java.util.Date;
import java.util.Set;

public class NimbusOidcIdTokenProcessor {

    private final ConfigurableJWTProcessor<SecurityContext> processor;
    private final String issuer;
    private final String audience;
    private final Clock clock;

    public NimbusOidcIdTokenProcessor(String issuer, String audience, String jwksUri, Clock clock) {
        try {
            this.issuer = issuer;
            this.audience = audience;
            this.clock = clock;

            this.processor = new DefaultJWTProcessor<>();

            JWKSource<SecurityContext> keySource = new RemoteJWKSet<>(new URL(jwksUri));
            JWSKeySelector<SecurityContext> keySelector =
                new JWSVerificationKeySelector<>(JWSAlgorithm.RS256, keySource);

            processor.setJWSKeySelector(keySelector);

            var required = Set.of("sub", "iss", "aud", "exp", "iat");

            processor.setJWTClaimsSetVerifier(new DefaultJWTClaimsVerifier<>(
                new JWTClaimsSet.Builder().issuer(issuer).build(),
                required
            ) {
                @Override
                public void verify(JWTClaimsSet claims, SecurityContext context) throws BadJWTException {
                    super.verify(claims, context);

                    if (!issuer.equals(claims.getIssuer())) {
                        throw new BadJWTException("Invalid issuer");
                    }

                    if (claims.getAudience() == null || !claims.getAudience().contains(audience)) {
                        throw new BadJWTException("Invalid audience");
                    }

                    Date exp = claims.getExpirationTime();
                    if (exp == null || exp.before(Date.from(clock.instant()))) {
                        throw new BadJWTException("Token expired");
                    }
                }
            });
        } catch (Exception e) {
            throw new IllegalStateException("Failed to init NimbusOidcIdTokenProcessor", e);
        }
    }

    public JWTClaimsSet verifyAndGetClaims(String idToken) {
        try {
            return processor.process(idToken, null);
        } catch (Exception e) {
            throw new IllegalArgumentException("ID토큰 검증에 실패했습니다.", e);
        }
    }
}
