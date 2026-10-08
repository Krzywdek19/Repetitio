package pl.exceptionhandled.repetitio.shared.security;

import java.security.*;
import java.time.*;
import java.util.*;
import org.junit.jupiter.api.*;
import org.springframework.security.oauth2.jwt.*;
import org.springframework.security.oauth2.jose.jws.SignatureAlgorithm;
import static org.assertj.core.api.Assertions.*;

class JwtValidationTest {
    private static final Instant NOW = Instant.parse("2026-10-07T12:00:00Z");
    private static JwtEncoder encoder;
    private static JwtDecoder decoder;
    private static java.security.interfaces.RSAPrivateKey signingKey;

    @BeforeAll static void setup() throws Exception {
        var generator = KeyPairGenerator.getInstance("RSA");
        generator.initialize(2048);
        var keys = generator.generateKeyPair();
        signingKey = (java.security.interfaces.RSAPrivateKey) keys.getPrivate();
        var configuration = new JwtConfiguration();
        encoder = configuration.jwtEncoder(keys);
        decoder = configuration.jwtDecoder(keys, Clock.fixed(NOW, ZoneOffset.UTC), "repetitio", "repetitio-api");
    }

    private Map<String, Object> claims() {
        var claims = new HashMap<String, Object>();
        claims.put("sub", UUID.randomUUID().toString());
        claims.put("iss", "repetitio");
        claims.put("aud", List.of("repetitio-api"));
        claims.put("iat", NOW);
        claims.put("exp", NOW.plusSeconds(900));
        claims.put("jti", UUID.randomUUID().toString());
        return claims;
    }

    private String token(Map<String, Object> claims) {
        try {
            var builder = new com.nimbusds.jwt.JWTClaimsSet.Builder();
            claims.forEach((key, value) -> builder.claim(key, value instanceof Instant time ? Date.from(time) : value));
            var signed = new com.nimbusds.jwt.SignedJWT(
                    new com.nimbusds.jose.JWSHeader(com.nimbusds.jose.JWSAlgorithm.RS256), builder.build());
            signed.sign(new com.nimbusds.jose.crypto.RSASSASigner(signingKey));
            return signed.serialize();
        } catch (com.nimbusds.jose.JOSEException exception) {
            throw new IllegalStateException(exception);
        }
    }

    @Test void acceptsCorrectToken() {
        var claims = claims();
        assertThat(decoder.decode(token(claims)).getSubject()).isEqualTo(claims.get("sub"));
    }

    @Test void rejectsMissingRequiredClaims() {
        for (var claim : List.of("sub", "iss", "aud", "iat", "exp", "jti")) {
            var values = claims(); values.remove(claim);
            assertThatThrownBy(() -> decoder.decode(token(values))).as(claim).isInstanceOf(JwtException.class);
        }
    }

    @Test void rejectsInvalidClaims() {
        var invalid = Map.<String, Object>of("sub", "not-a-uuid", "iss", "someone-else",
                "aud", List.of("another-api"), "iat", NOW.plusSeconds(60), "exp", NOW.minusSeconds(60), "jti", "");
        invalid.forEach((key, value) -> {
            var claims = claims(); claims.put(key, value);
            assertThatThrownBy(() -> decoder.decode(token(claims))).as(key).isInstanceOf(JwtException.class);
        });
    }

    @Test void rejectsExcessiveLifetimeAndFutureNotBefore() {
        var longLived = claims(); longLived.put("exp", NOW.plusSeconds(901));
        assertThatThrownBy(() -> decoder.decode(token(longLived))).isInstanceOf(JwtException.class);
        var future = claims(); future.put("nbf", NOW.plusSeconds(60));
        assertThatThrownBy(() -> decoder.decode(token(future))).isInstanceOf(JwtException.class);
    }

    @Test void rejectsTamperedSignatureAndWrongKey() throws Exception {
        String token = token(claims());
        String[] parts = token.split("\\.");
        byte[] signature = Base64.getUrlDecoder().decode(parts[2]); signature[0] ^= 1;
        String altered = parts[0] + "." + parts[1] + "." + Base64.getUrlEncoder().withoutPadding().encodeToString(signature);
        assertThatThrownBy(() -> decoder.decode(altered)).isInstanceOf(JwtException.class);
        var generator = KeyPairGenerator.getInstance("RSA"); generator.initialize(2048);
        var other = new JwtConfiguration().jwtDecoder(generator.generateKeyPair(), Clock.fixed(NOW, ZoneOffset.UTC), "repetitio", "repetitio-api");
        assertThatThrownBy(() -> other.decode(token)).isInstanceOf(JwtException.class);
    }

    @Test void rejectsUnsignedAndSymmetricTokens() throws Exception {
        var claims = new com.nimbusds.jwt.JWTClaimsSet.Builder().subject(UUID.randomUUID().toString()).build();
        assertThatThrownBy(() -> decoder.decode(new com.nimbusds.jwt.PlainJWT(claims).serialize())).isInstanceOf(JwtException.class);
        var signed = new com.nimbusds.jwt.SignedJWT(new com.nimbusds.jose.JWSHeader(com.nimbusds.jose.JWSAlgorithm.HS256), claims);
        signed.sign(new com.nimbusds.jose.crypto.MACSigner(new byte[32]));
        assertThatThrownBy(() -> decoder.decode(signed.serialize())).isInstanceOf(JwtException.class);
    }
}

