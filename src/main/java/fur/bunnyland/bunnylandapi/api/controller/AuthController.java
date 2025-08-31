package fur.bunnyland.bunnylandapi.api.controller;

import fur.bunnyland.bunnylandapi.api.dto.RegisterRequest;
import fur.bunnyland.bunnylandapi.api.dto.RegisterResponse;
import fur.bunnyland.bunnylandapi.api.dto.Role;
import fur.bunnyland.bunnylandapi.domain.User;
import fur.bunnyland.bunnylandapi.repository.UserRepository;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Set;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public AuthController(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @PostMapping("/register")
    public ResponseEntity<RegisterResponse> register(@Valid @RequestBody RegisterRequest request) {
        if (userRepository.existsByEmail(request.email())) {
            return ResponseEntity.status(HttpStatus.CONFLICT).build();
        }
        User user = new User();
        user.setEmail(request.email());
        user.setPasswordHash(passwordEncoder.encode(request.password()));
        user.setDisplayName(request.displayName());
        user.setCity(request.city());
        user.setCountry(request.country());
        user.setRoles(Set.of("OWNER"));
        User saved = userRepository.save(user);
        RegisterResponse response = new RegisterResponse(saved.getId(),
                saved.getEmail(),
                saved.getDisplayName(),
                saved.getCity(),
                saved.getCountry(),
                Set.of(Role.OWNER));
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }
}

