package pl.exceptionhandled.repetitio.support.auth;

import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
import pl.exceptionhandled.repetitio.auth.api.LoginRequest;
import pl.exceptionhandled.repetitio.auth.api.RegisterRequest;
import pl.exceptionhandled.repetitio.shared.api.ApiPaths;
import tools.jackson.databind.json.JsonMapper;

import java.util.Objects;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;

public final class AuthTestClient {

    private final MockMvc mockMvc;
    private final JsonMapper jsonMapper;

    public AuthTestClient(MockMvc mockMvc, JsonMapper jsonMapper) {
        this.mockMvc = Objects.requireNonNull(mockMvc);
        this.jsonMapper = Objects.requireNonNull(jsonMapper);
    }

    public ResultActions register(RegisterRequest request) throws Exception {
        return postJson(ApiPaths.AUTH_REGISTER, request);
    }

    public ResultActions register(String email, String password) throws Exception {
        return register(new RegisterRequest(email, password));
    }

    public ResultActions login(LoginRequest request) throws Exception {
        return postJson(ApiPaths.AUTH_LOGIN, request);
    }

    public ResultActions login(String email, String password) throws Exception {
        return login(new LoginRequest(email, password));
    }

    private ResultActions postJson(String path, Object request) throws Exception {
        return mockMvc.perform(
                post(path)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonMapper.writeValueAsString(request))
        );
    }
}
