package pl.exceptionhandled.repetitio.auth.infrastructure.security;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.util.Base64;

@Component
public class RefreshTokenCodec {

    private static final int TOKEN_SIZE = 32;

    private final SecureRandom secureRandom = new SecureRandom();

    public String generate() {
        var bytes = new byte[TOKEN_SIZE];
        secureRandom.nextBytes(bytes);

        return Base64.getUrlEncoder()
                .withoutPadding()
                .encodeToString(bytes);
    }

    public String hash(String rawToken) {
        try {
            var digest = MessageDigest.getInstance("SHA-256");

            var  hashBites = digest.digest(rawToken.getBytes(StandardCharsets.UTF_8));

            var hexString = new StringBuilder();
            for(byte b : hashBites) {
                var hex = Integer.toHexString(0xff & b);
                if (hex.length() == 1) hexString.append('0');
                hexString.append(hex);
            }

            return hexString.toString();
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException(e);
        }
    }
}