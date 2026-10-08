package pl.exceptionhandled.repetitio.shared.security;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletRequestWrapper;
import java.util.concurrent.atomic.AtomicBoolean;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import static org.assertj.core.api.Assertions.*;

class RequestSizeFilterTest {
    @Test void preservesBodyWithinLimit() throws Exception {
        var request = new MockHttpServletRequest("POST", "/api/notes");
        byte[] content = new byte[RequestSizeFilter.MAX_BYTES];
        request.setContent(content);
        var called = new AtomicBoolean();
        new RequestSizeFilter().doFilter(request, new MockHttpServletResponse(), (req, res) -> {
            called.set(true);
            assertThat(req.getInputStream().readAllBytes()).isEqualTo(content);
        });
        assertThat(called).isTrue();
    }

    @Test void rejectsOversizedBodyWithoutContentLength() throws Exception {
        var request = new MockHttpServletRequest("PATCH", "/api/notes/example");
        request.setContent(new byte[RequestSizeFilter.MAX_BYTES + 1]);
        HttpServletRequest chunked = new HttpServletRequestWrapper(request) {
            @Override public long getContentLengthLong() { return -1; }
        };
        var response = new MockHttpServletResponse();
        var called = new AtomicBoolean();
        new RequestSizeFilter().doFilter(chunked, response, (req, res) -> called.set(true));
        assertThat(called).isFalse();
        assertThat(response.getStatus()).isEqualTo(413);
        assertThat(response.getContentType()).isEqualTo("application/problem+json");
    }
}
