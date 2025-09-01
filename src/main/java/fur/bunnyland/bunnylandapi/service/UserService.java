package fur.bunnyland.bunnylandapi.service;

import fur.bunnyland.bunnylandapi.api.dto.RegisterRequest;
import fur.bunnyland.bunnylandapi.api.dto.RegisterResponse;
import fur.bunnyland.bunnylandapi.api.dto.Role;
import fur.bunnyland.bunnylandapi.domain.MessageError;
import fur.bunnyland.bunnylandapi.domain.ResponseObject;
import fur.bunnyland.bunnylandapi.domain.User;
import fur.bunnyland.bunnylandapi.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Set;

import static fur.bunnyland.bunnylandapi.domain.ErrorCode.EMAIL_TAKEN;

@Service
@RequiredArgsConstructor
public class UserService {
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public ResponseObject<RegisterResponse> registerOwner(RegisterRequest request) {
        if (userRepository.existsByEmail(request.email())) {
            return ResponseObject.fail(
                    new MessageError(HttpStatus.CONFLICT,
                            EMAIL_TAKEN,
                            "This email has been already registered",
                            "User other email, or log in"));
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
        return ResponseObject.ok(response);
    }
}
