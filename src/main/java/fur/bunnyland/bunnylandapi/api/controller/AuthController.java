package fur.bunnyland.bunnylandapi.api.controller;

import fur.bunnyland.bunnylandapi.api.dto.*;
import fur.bunnyland.bunnylandapi.domain.ResponseObject;
import fur.bunnyland.bunnylandapi.service.UserService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
public class AuthController {
    private final UserService userService;

    public AuthController(UserService userService) {
        this.userService = userService;
    }

    @PostMapping("/register")
    public ResponseEntity register(@Valid @RequestBody RegisterRequest request) {
        ResponseObject<RegisterResponse> registerResponse = userService.registerOwner(request);
        if (registerResponse.hasError()) {
           return new ResponseEntity<>(registerResponse.error().message(), registerResponse.error().status());
        }
        return ResponseEntity.status(HttpStatus.CREATED).body(registerResponse.body());
    }

    @PostMapping("/login")
    public ResponseEntity login(@Valid @RequestBody LoginRequest req) {
        ResponseObject<LoginResponse> loginResponse = userService.login(req);
        if (loginResponse.hasError()) {
            return new ResponseEntity<>(loginResponse.error().message(), loginResponse.error().status());
        }
        return ResponseEntity.ok(ResponseObject.ok(loginResponse));
    }

    @PostMapping("/refresh")
    public ResponseEntity refresh(@Valid @RequestBody RefreshRequest req) {
        ResponseObject<RefreshResponse>  refreshResponse = userService.refresh(req);
        if (refreshResponse.hasError()) {
            return new ResponseEntity<>(refreshResponse.error().message(), refreshResponse.error().status());
        }
        return ResponseEntity.ok(ResponseObject.ok(refreshResponse));
    }

    @GetMapping("/me")
    public ResponseEntity me(@RequestHeader(value = "Authorization", required = false) String authorization) {
        if (authorization == null || !authorization.startsWith("Bearer ")) {
            return new ResponseEntity<>("Missing or invalid Authorization header", HttpStatus.UNAUTHORIZED);
        }
        String token = authorization.substring(7);
        ResponseObject<ProfileResponse> resp = userService.me(token);
        if (resp.hasError()) {
            return new ResponseEntity<>(resp.error().message(), resp.error().status());
        }
        return ResponseEntity.ok(resp.body());
    }
}

