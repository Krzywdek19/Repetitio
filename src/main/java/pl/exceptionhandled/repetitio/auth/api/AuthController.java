package pl.exceptionhandled.repetitio.auth.api;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;
import pl.exceptionhandled.repetitio.auth.application.*;
import pl.exceptionhandled.repetitio.user.application.UserIdentity;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
class AuthController {
    private final AuthService auth;
    @PostMapping("/register")
    @ResponseStatus(HttpStatus.CREATED)
    UserIdentity register(@Valid @RequestBody CredentialsRequest request) {
        return auth.register(request.email(), request.password());
    }
    @PostMapping("/login")
    ResponseEntity<TokenResponse> login(@Valid @RequestBody CredentialsRequest request) {
        return ResponseEntity.ok().cacheControl(CacheControl.noStore())
                .body(auth.login(request.email(), request.password()));
    }
}

