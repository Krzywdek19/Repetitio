package pl.exceptionhandled.repetitio;

import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.util.Base64;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.postgresql.PostgreSQLContainer;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public abstract class PostgresIntegrationTest {
    static final PostgreSQLContainer DATABASE = new PostgreSQLContainer("postgres:17-alpine");
    static final KeyPair KEYS = keys();

    @DynamicPropertySource
    static void properties(DynamicPropertyRegistry registry) {
        DATABASE.start();
        registry.add("spring.datasource.url", DATABASE::getJdbcUrl);
        registry.add("spring.datasource.username", DATABASE::getUsername);
        registry.add("spring.datasource.password", DATABASE::getPassword);
        registry.add("security.jwt.private-key", () -> pem("PRIVATE KEY", KEYS.getPrivate().getEncoded()));
        registry.add("security.jwt.public-key", () -> pem("PUBLIC KEY", KEYS.getPublic().getEncoded()));
    }

    private static KeyPair keys() {
        try {
            var generator = KeyPairGenerator.getInstance("RSA");
            generator.initialize(2048);
            return generator.generateKeyPair();
        } catch (Exception exception) {
            throw new IllegalStateException(exception);
        }
    }

    private static String pem(String type, byte[] encoded) {
        return "-----BEGIN " + type + "-----\n" + Base64.getEncoder().encodeToString(encoded)
                + "\n-----END " + type + "-----";
    }
}
