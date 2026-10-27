package pl.exceptionhandled.repetitio.auth.infrastructure.security;

import lombok.RequiredArgsConstructor;
import org.springframework.security.oauth2.jose.jws.MacAlgorithm;
import org.springframework.security.oauth2.jwt.*;
import org.springframework.stereotype.Service;

import pl.exceptionhandled.repetitio.auth.api.LoginResponse;
import pl.exceptionhandled.repetitio.auth.config.JwtProperties;
import pl.exceptionhandled.repetitio.users.domain.User;

import java.time.Instant;

@Service
@RequiredArgsConstructor
public class JwtTokenService {

    private final JwtEncoder jwtEncoder;
    private final JwtProperties jwtProperties;

    public LoginResponse generate(User user) {

        Instant now = Instant.now();
        Instant expiresAt = now.plus(jwtProperties.ttl());

        JwtClaimsSet claims = JwtClaimsSet.builder()
                .issuer(jwtProperties.issuer())
                .subject(user.getId().toString())
                .issuedAt(now)
                .expiresAt(expiresAt)
                .build();

        JwsHeader header = JwsHeader
                .with(MacAlgorithm.HS256)
                .build();

        String token = jwtEncoder.encode(
                JwtEncoderParameters.from(header, claims)
        ).getTokenValue();

        return new LoginResponse(
                token,
                "Bearer",
                jwtProperties.ttl().toSeconds()
        );
    }
}