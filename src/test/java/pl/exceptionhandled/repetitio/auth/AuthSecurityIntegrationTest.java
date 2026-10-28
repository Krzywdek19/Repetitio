package pl.exceptionhandled.repetitio.auth;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import pl.exceptionhandled.repetitio.TestcontainersConfiguration;
import pl.exceptionhandled.repetitio.shared.api.ApiPaths;
import pl.exceptionhandled.repetitio.users.domain.User;
import pl.exceptionhandled.repetitio.users.domain.UserRepository;
import tools.jackson.databind.json.JsonMapper;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Import(TestcontainersConfiguration.class)
class AuthSecurityIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private JsonMapper jsonMapper;

    @Test
    void shouldNotRevealUserExistenceThroughLoginResponse()
            throws Exception {

        String existingEmail = UUID.randomUUID() + "@example.com";
        String missingEmail = UUID.randomUUID() + "@example.com";

        userRepository.save(
                new User(
                        existingEmail,
                        passwordEncoder.encode("CorrectPassword123!")
                )
        );

        var existingUserResponse = login(
                existingEmail,
                "WrongPassword123!"
        );

        var missingUserResponse = login(
                missingEmail,
                "WrongPassword123!"
        );

        assertEquals(401, existingUserResponse.getStatus());
        assertEquals(401, missingUserResponse.getStatus());

        var existingBody = jsonMapper.readTree(
                existingUserResponse.getContentAsString()
        );

        var missingBody = jsonMapper.readTree(
                missingUserResponse.getContentAsString()
        );

        assertEquals(
                existingBody.get("code").toString(),
                missingBody.get("code").toString()
        );

        assertEquals(
                existingBody.get("detail").toString(),
                missingBody.get("detail").toString()
        );
    }

    private org.springframework.mock.web.MockHttpServletResponse login(
            String email,
            String password
    ) throws Exception {

        String body = """
                {
                    "email": "%s",
                    "password": "%s"
                }
                """.formatted(email, password);

        return mockMvc.perform(
                post(ApiPaths.AUTH_LOGIN)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body)
        ).andReturn().getResponse();
    }
}