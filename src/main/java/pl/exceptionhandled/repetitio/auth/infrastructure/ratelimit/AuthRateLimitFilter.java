package pl.exceptionhandled.repetitio.auth.infrastructure.ratelimit;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ProblemDetail;
import org.springframework.web.filter.OncePerRequestFilter;

import pl.exceptionhandled.repetitio.shared.api.ApiPaths;
import tools.jackson.databind.json.JsonMapper;

import java.io.IOException;
import java.nio.charset.StandardCharsets;

public class AuthRateLimitFilter extends OncePerRequestFilter {

    private final AuthRateLimiter limiter;
    private final JsonMapper jsonMapper;

    public AuthRateLimitFilter(
            AuthRateLimiter limiter,
            JsonMapper jsonMapper
    ) {
        this.limiter = limiter;
        this.jsonMapper = jsonMapper;
    }

    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain chain
    ) throws ServletException, IOException {

        if (!"POST".equals(request.getMethod())) {
            chain.doFilter(request, response);
            return;
        }

        String path = request.getServletPath();
        String clientIp = request.getRemoteAddr();

        AuthRateLimiter.RateLimitDecision decision;

        if (ApiPaths.AUTH_LOGIN.equals(path)) {
            decision = limiter.consumeLogin(clientIp);

        } else if (ApiPaths.AUTH_REGISTER.equals(path)) {
            decision = limiter.consumeRegistration(clientIp);

        } else {
            chain.doFilter(request, response);
            return;
        }

        if (!decision.allowed()) {
            handleRateLimitExceeded(response, decision);
            return;
        }

        chain.doFilter(request, response);
    }

    private void handleRateLimitExceeded(
            HttpServletResponse response,
            AuthRateLimiter.RateLimitDecision decision
    ) throws IOException {

        ProblemDetail problem = ProblemDetail.forStatusAndDetail(
                HttpStatus.TOO_MANY_REQUESTS,
                "Too many requests. Please try again later."
        );

        problem.setProperty("code", "RATE_LIMIT_EXCEEDED");

        response.setStatus(HttpStatus.TOO_MANY_REQUESTS.value());

        response.setContentType(
                MediaType.APPLICATION_PROBLEM_JSON_VALUE
        );

        response.setCharacterEncoding(StandardCharsets.UTF_8.name());

        response.setHeader(
                HttpHeaders.RETRY_AFTER,
                String.valueOf(decision.retryAfterSeconds())
        );

        response.setHeader(HttpHeaders.CACHE_CONTROL, "no-store");

        response.getWriter().write(
                jsonMapper.writeValueAsString(problem)
        );
    }
}