package pl.exceptionhandled.repetitio.auth;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.context.annotation.Import;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import pl.exceptionhandled.repetitio.TestcontainersConfiguration;
import pl.exceptionhandled.repetitio.auth.api.RegisterRequest;
import pl.exceptionhandled.repetitio.support.auth.AuthTestClient;
import pl.exceptionhandled.repetitio.support.auth.AuthTestDataFactory;
import pl.exceptionhandled.repetitio.users.domain.UserRepository;
import tools.jackson.databind.json.JsonMapper;

import java.util.Locale;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static pl.exceptionhandled.repetitio.support.auth.AuthTestDataFactory.VALID_PASSWORD;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Import(TestcontainersConfiguration.class)
class AuthIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private JsonMapper jsonMapper;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private JwtDecoder jwtDecoder;

    private AuthTestClient auth;

    @BeforeEach
    void setUp() {
        auth = new AuthTestClient(mockMvc, jsonMapper);
    }

    @Test
    void shouldRegisterUserSuccessfully() throws Exception {
        RegisterRequest request = AuthTestDataFactory.validRegistration();

        auth.register(request)
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").exists())
                .andExpect(jsonPath("$.email").value(request.email()));

        var savedUser = userRepository.findByEmailIgnoreCase(request.email())
                .orElseThrow();

        assertEquals(request.email(), savedUser.getEmail());
        assertNotEquals(request.password(), savedUser.getPasswordHash());
        assertTrue(passwordEncoder.matches(request.password(), savedUser.getPasswordHash()));
    }

    @Test
    void shouldRejectDuplicateEmailRegardlessOfCase() throws Exception {
        String email = AuthTestDataFactory.uniqueEmail();

        auth.register(email, VALID_PASSWORD)
                .andExpect(status().isCreated());

        auth.register(email.toUpperCase(Locale.ROOT), VALID_PASSWORD)
                .andExpect(status().isConflict());
    }

    @Test
    void shouldRejectWeakPassword() throws Exception {
        auth.register(AuthTestDataFactory.uniqueEmail(), "12345678901234")
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("WEAK_PASSWORD"));
    }

    @Test
    void shouldLoginWithValidCredentialsAndReturnSignedJwt() throws Exception {
        RegisterRequest request = AuthTestDataFactory.validRegistration();

        auth.register(request)
                .andExpect(status().isCreated());

        var result = auth.login(request.email(), request.password())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.accessToken").isNotEmpty())
                .andExpect(jsonPath("$.tokenType").value("Bearer"))
                .andExpect(jsonPath("$.expiresIn").value(1800))
                .andReturn();

        String accessToken = jsonMapper
                .readTree(result.getResponse().getContentAsString())
                .path("accessToken")
                .asText();

        var jwt = jwtDecoder.decode(accessToken);
        var savedUser = userRepository.findByEmailIgnoreCase(request.email())
                .orElseThrow();

        assertEquals(savedUser.getId().toString(), jwt.getSubject());
    }

    @Test
    void shouldRejectIncorrectPassword() throws Exception {
        RegisterRequest request = AuthTestDataFactory.validRegistration();

        auth.register(request)
                .andExpect(status().isCreated());

        auth.login(request.email(), "WrongPassword123!")
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.code").value("INVALID_CREDENTIALS"));
    }

    @Test
    void shouldRejectUnknownEmail() throws Exception {
        auth.login(AuthTestDataFactory.uniqueEmail(), VALID_PASSWORD)
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.code").value("INVALID_CREDENTIALS"));
    }
}
