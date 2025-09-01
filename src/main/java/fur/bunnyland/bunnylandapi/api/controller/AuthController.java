package fur.bunnyland.bunnylandapi.api.controller;

import fur.bunnyland.bunnylandapi.api.dto.RegisterRequest;
import fur.bunnyland.bunnylandapi.api.dto.RegisterResponse;
import fur.bunnyland.bunnylandapi.domain.ResponseObject;
import fur.bunnyland.bunnylandapi.service.UserService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

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
}

