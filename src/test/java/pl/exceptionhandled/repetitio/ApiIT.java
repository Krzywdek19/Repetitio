package pl.exceptionhandled.repetitio;

import java.net.URI;
import java.net.http.*;
import java.util.*;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.*;
import org.springframework.jdbc.core.JdbcTemplate;
import tools.jackson.databind.*;
import static org.assertj.core.api.Assertions.*;

class ApiIT extends PostgresIntegrationTest {
    @Value("${local.server.port}") int port;
    @Autowired ObjectMapper json;
    @Autowired JdbcTemplate jdbc;
    private final HttpClient client = HttpClient.newHttpClient();
    private static final String PASSWORD = "a-safe-test-password";

    record Response(int status, JsonNode body) {}

    Response request(String method, String path, String token, Object body) throws Exception {
        var builder = HttpRequest.newBuilder(URI.create("http://localhost:" + port + path))
                .header("Content-Type", "application/json");
        if (token != null) builder.header("Authorization", "Bearer " + token);
        var request = builder.method(method, body == null ? HttpRequest.BodyPublishers.noBody()
                : HttpRequest.BodyPublishers.ofString(json.writeValueAsString(body))).build();
        var response = client.send(request, HttpResponse.BodyHandlers.ofString());
        return new Response(response.statusCode(), response.body().isBlank() ? null : json.readTree(response.body()));
    }

    String account() throws Exception {
        String email = UUID.randomUUID() + "@example.com";
        var registered = request("POST", "/api/auth/register", null, Map.of("email", email, "password", PASSWORD));
        assertThat(registered.status()).isEqualTo(201);
        assertThat(registered.body().has("passwordHash")).isFalse();
        var login = request("POST", "/api/auth/login", null, Map.of("email", email, "password", PASSWORD));
        assertThat(login.status()).isEqualTo(200);
        return login.body().get("accessToken").asText();
    }

    String note(String token) throws Exception {
        var response = request("POST", "/api/notes", token, Map.of("title", "Title", "content", "Content"));
        assertThat(response.status()).isEqualTo(201);
        return response.body().get("id").asText();
    }

    String card(String token, String noteId) throws Exception {
        var body = new HashMap<String, Object>();
        body.put("front", "Question"); body.put("back", "Answer");
        if (noteId != null) body.put("noteId", noteId);
        var response = request("POST", "/api/flashcards", token, body);
        assertThat(response.status()).isEqualTo(201);
        return response.body().get("id").asText();
    }

    @Test void authenticationAndValidation() throws Exception {
        assertThat(request("GET", "/api/notes", null, null).status()).isEqualTo(401);
        assertThat(request("GET", "/api/notes", "invalid-token", null).status()).isEqualTo(401);
        assertThat(request("POST", "/api/auth/register", null, Map.of("email", "bad", "password", "short")).status()).isEqualTo(400);
        assertThat(request("POST", "/api/auth/register", null, Map.of("email", "long@example.com", "password", "ą".repeat(37))).status()).isEqualTo(400);
        assertThat(request("POST", "/api/auth/login", null, Map.of("email", "missing@example.com", "password", PASSWORD)).status()).isEqualTo(401);
        var token = account();
        assertThat(request("GET", "/api/notes?size=101", token, null).status()).isEqualTo(400);
        assertThat(request("GET", "/api/notes?page=-1", token, null).status()).isEqualTo(400);
        assertThat(request("POST", "/api/notes", token, Map.of("title", "t", "content", "c", "ownerId", UUID.randomUUID())).status()).isEqualTo(400);
    }

    @Test void registrationNormalizesEmailAndHashesPassword() throws Exception {
        String prefix = UUID.randomUUID().toString();
        var body = Map.of("email", " " + prefix.toUpperCase(Locale.ROOT) + "@EXAMPLE.COM ", "password", PASSWORD);
        assertThat(request("POST", "/api/auth/register", null, body).status()).isEqualTo(201);
        assertThat(request("POST", "/api/auth/register", null, Map.of("email", prefix + "@example.com", "password", PASSWORD)).status()).isEqualTo(409);
        assertThat(request("POST", "/api/auth/login", null, body).status()).isEqualTo(200);
        assertThat(request("POST", "/api/auth/login", null, Map.of("email", prefix + "@example.com", "password", "incorrect-password")).status()).isEqualTo(401);
        var hash = jdbc.queryForObject("select password_hash from app_user where email = ?", String.class, prefix + "@example.com");
        assertThat(hash).startsWith("$2").isNotEqualTo(PASSWORD);
    }

    @Test void noteOwnershipAndPatchSemantics() throws Exception {
        var owner = account(); var stranger = account(); var id = note(owner);
        String path = "/api/notes/" + id;
        assertThat(request("GET", path, stranger, null).status()).isEqualTo(404);
        assertThat(request("PATCH", path, stranger, Map.of("title", "stolen")).status()).isEqualTo(404);
        assertThat(request("DELETE", path, stranger, null).status()).isEqualTo(404);
        assertThat(request("GET", "/api/notes", stranger, null).body().get("items").isEmpty()).isTrue();
        var patched = request("PATCH", path, owner, Map.of("title", "Changed"));
        assertThat(patched.status()).isEqualTo(200);
        assertThat(patched.body().get("content").asText()).isEqualTo("Content");
        assertThat(request("PATCH", path, owner, Collections.singletonMap("title", null)).status()).isEqualTo(400);
        assertThat(request("DELETE", path, owner, null).status()).isEqualTo(204);
        assertThat(request("GET", path, owner, null).status()).isEqualTo(404);
    }

    @Test void cardOwnershipNoteLinkingAndDeletion() throws Exception {
        var owner = account(); var stranger = account();
        var note = note(owner); var foreignNote = note(stranger); var id = card(owner, note);
        String path = "/api/flashcards/" + id;
        assertThat(request("GET", path, stranger, null).status()).isEqualTo(404);
        assertThat(request("PATCH", path, stranger, Map.of("front", "stolen")).status()).isEqualTo(404);
        assertThat(request("DELETE", path, stranger, null).status()).isEqualTo(404);
        assertThat(request("GET", "/api/flashcards", stranger, null).body().get("items").isEmpty()).isTrue();
        assertThat(request("POST", "/api/flashcards", stranger, Map.of("noteId", note, "front", "q", "back", "a")).status()).isEqualTo(404);
        assertThat(request("PATCH", path, owner, Map.of("noteId", foreignNote)).status()).isEqualTo(404);
        assertThat(request("PATCH", path, owner, Collections.singletonMap("noteId", null)).body().get("noteId").isNull()).isTrue();
        assertThat(request("PATCH", path, owner, Map.of("noteId", note)).status()).isEqualTo(200);
        assertThat(request("PATCH", path, owner, Map.of("front", "Changed")).body().get("noteId").asText()).isEqualTo(note);
        assertThat(request("DELETE", "/api/notes/" + note, owner, null).status()).isEqualTo(204);
        assertThat(request("GET", path, owner, null).body().get("noteId").isNull()).isTrue();
        assertThat(request("DELETE", path, owner, null).status()).isEqualTo(204);
        assertThat(jdbc.queryForObject("select count(*) from flashcard_schedule where flashcard_id = ?", Long.class, UUID.fromString(id))).isZero();
    }

    @Test void studyIsOwnedAndRejectsStaleOrPrematureReviews() throws Exception {
        var owner = account(); var stranger = account(); var id = card(owner, null);
        var due = request("GET", "/api/study/due", owner, null);
        assertThat(due.status()).isEqualTo(200);
        assertThat(due.body().get("items").size()).isEqualTo(1);
        assertThat(request("GET", "/api/study/due", stranger, null).body().get("items").isEmpty()).isTrue();
        String path = "/api/study/flashcards/" + id + "/review";
        assertThat(request("POST", path, stranger, Map.of("rating", "GOOD", "expectedVersion", 0)).status()).isEqualTo(404);
        assertThat(request("POST", path, owner, Map.of("rating", "UNKNOWN", "expectedVersion", 0)).status()).isEqualTo(400);
        assertThat(request("POST", path, owner, Map.of("rating", "GOOD")).status()).isEqualTo(400);
        var reviewed = request("POST", path, owner, Map.of("rating", "GOOD", "expectedVersion", 0));
        assertThat(reviewed.status()).isEqualTo(200);
        assertThat(reviewed.body().get("version").asLong()).isEqualTo(1);
        assertThat(reviewed.body().get("state").get("intervalDays").asInt()).isEqualTo(2);
        assertThat(request("POST", path, owner, Map.of("rating", "GOOD", "expectedVersion", 0)).status()).isEqualTo(409);
        assertThat(request("POST", path, owner, Map.of("rating", "GOOD", "expectedVersion", 1)).status()).isEqualTo(409);
        assertThat(request("GET", "/api/study/due", owner, null).body().get("items").isEmpty()).isTrue();
    }
}

