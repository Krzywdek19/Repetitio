package pl.exceptionhandled.repetitio.auth.infrastructure;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.apache.tomcat.util.http.Method;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@RequiredArgsConstructor
public class AuthLimitFilter extends OncePerRequestFilter {
    private final AuthRateLimiter limiter;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {
        if(!request.getMethod().equals(Method.POST) || !request.getServletPath().equals("/api/auth/login")) {
            filterChain.doFilter(request, response);
            return;
        }

        String clientKey = "login:%s".formatted(request.getRemoteAddr());

        if(!limiter.allow(clientKey)) {
            response.setStatus(429);
            response.setContentType("application/problem-json");
            response.getWriter().write("""
                    {"type":"about:blank",
                    "title":"Too Many Requests",
                    "status":429,
                    "detail":"Too many login attempts"}
                    """);
            return;
        }

        filterChain.doFilter(request, response);
    }
}
