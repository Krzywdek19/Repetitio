package pl.exceptionhandled.repetitio.shared.security;

import java.security.*;
import java.security.interfaces.RSAPrivateKey;
import java.security.interfaces.RSAPublicKey;
import java.security.spec.*;
import java.time.*;
import java.util.*;
import com.nimbusds.jose.jwk.*;
import com.nimbusds.jose.jwk.source.ImmutableJWKSet;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.*;
import org.springframework.security.oauth2.core.*;
import org.springframework.security.oauth2.jose.jws.SignatureAlgorithm;
import org.springframework.security.oauth2.jwt.*;

@Configuration
public class JwtConfiguration {
    @Bean
    KeyPair signingKeys(@Value("${security.jwt.private-key}") String privatePem,
                        @Value("${security.jwt.public-key}") String publicPem) throws Exception {
        var factory = KeyFactory.getInstance("RSA");
        var privateKey = (RSAPrivateKey) factory.generatePrivate(new PKCS8EncodedKeySpec(decode(privatePem, "PRIVATE KEY")));
        var publicKey = (RSAPublicKey) factory.generatePublic(new X509EncodedKeySpec(decode(publicPem, "PUBLIC KEY")));
        if (publicKey.getModulus().bitLength() < 2048 || !publicKey.getModulus().equals(privateKey.getModulus()))
            throw new IllegalArgumentException("JWT keys must be a matching RSA pair of at least 2048 bits");
        return new KeyPair(publicKey, privateKey);
    }

    private static byte[] decode(String pem, String type) {
        return Base64.getDecoder().decode(pem.replace("-----BEGIN " + type + "-----", "")
                .replace("-----END " + type + "-----", "").replaceAll("\\s", ""));
    }

    @Bean
    JwtEncoder jwtEncoder(KeyPair keys) {
        var rsa = new RSAKey.Builder((RSAPublicKey) keys.getPublic())
                .privateKey((RSAPrivateKey) keys.getPrivate()).keyID("repetitio").build();
        return new NimbusJwtEncoder(new ImmutableJWKSet<>(new JWKSet(rsa)));
    }

    @Bean
    JwtDecoder jwtDecoder(KeyPair keys, Clock clock,
                          @Value("${security.jwt.issuer}") String issuer,
                          @Value("${security.jwt.audience}") String audience) {
        var decoder = NimbusJwtDecoder.withPublicKey((RSAPublicKey) keys.getPublic())
                .signatureAlgorithm(SignatureAlgorithm.RS256).build();
        var timestamps = new JwtTimestampValidator(Duration.ofSeconds(30));
        timestamps.setClock(clock);
        OAuth2TokenValidator<Jwt> required = jwt -> {
            try {
                UUID.fromString(jwt.getSubject());
                if (jwt.getIssuedAt() == null || jwt.getExpiresAt() == null || jwt.getId() == null
                        || jwt.getId().isBlank() || !jwt.getAudience().contains(audience)
                        || jwt.getIssuedAt().isAfter(clock.instant().plusSeconds(30))
                        || !jwt.getExpiresAt().isAfter(jwt.getIssuedAt())
                        || Duration.between(jwt.getIssuedAt(), jwt.getExpiresAt()).compareTo(Duration.ofMinutes(15)) > 0)
                    return invalid();
                return OAuth2TokenValidatorResult.success();
            } catch (RuntimeException exception) { return invalid(); }
        };
        decoder.setJwtValidator(new DelegatingOAuth2TokenValidator<>(timestamps, new JwtIssuerValidator(issuer), required));
        return decoder;
    }

    private static OAuth2TokenValidatorResult invalid() {
        return OAuth2TokenValidatorResult.failure(new OAuth2Error("invalid_token", "Invalid access token", null));
    }
}

