package pl.exceptionhandled.repetitio.auth.infrastructure;

import java.time.Clock;
import java.util.*;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.security.oauth2.jose.jws.SignatureAlgorithm;
import org.springframework.security.oauth2.jwt.*;
import pl.exceptionhandled.repetitio.auth.application.*;

@Component
class NimbusAccessTokenIssuer implements AccessTokenIssuer {
    private final JwtEncoder encoder;
    private final Clock clock;
    private final String issuer;
    private final String audience;
    NimbusAccessTokenIssuer(JwtEncoder encoder, Clock clock, @Value("${security.jwt.issuer}") String issuer,
                            @Value("${security.jwt.audience}") String audience) {
        this.encoder = encoder; this.clock = clock; this.issuer = issuer; this.audience = audience;
    }
    public TokenResponse issue(UUID userId) {
        var now = clock.instant();
        var claims = JwtClaimsSet.builder().subject(userId.toString()).issuer(issuer).audience(List.of(audience))
                .issuedAt(now).expiresAt(now.plusSeconds(900)).id(UUID.randomUUID().toString()).build();
        var header = JwsHeader.with(SignatureAlgorithm.RS256).type("JWT").build();
        return new TokenResponse(encoder.encode(JwtEncoderParameters.from(header, claims)).getTokenValue(), "Bearer", 900);
    }
}

