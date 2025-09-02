package fur.bunnyland.bunnylandapi.service;

import fur.bunnyland.bunnylandapi.api.dto.*;
import fur.bunnyland.bunnylandapi.domain.MessageError;
import fur.bunnyland.bunnylandapi.domain.ResponseObject;
import fur.bunnyland.bunnylandapi.domain.User;
import fur.bunnyland.bunnylandapi.repository.UserRepository;
import fur.bunnyland.bunnylandapi.security.JwtUtil;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;
import java.util.Set;

import static fur.bunnyland.bunnylandapi.domain.ErrorCode.*;

@Service
@RequiredArgsConstructor
public class UserService {
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;

    @Transactional
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

    @Transactional(readOnly = true)
    public ResponseObject<LoginResponse> login(LoginRequest request) {
        Optional<User> userOptional = userRepository.findByEmailIgnoreCase(request.email());
        if (userOptional.isEmpty()) {
            return ResponseObject.fail(
                    new MessageError(HttpStatus.UNAUTHORIZED,
                            USER_NOT_FOUND,
                            "User not found",
                            "Register first or enter with different email"));
        }
        User user = userOptional.get();
        if (!passwordEncoder.matches(request.password(), user.getPasswordHash())) {
            return ResponseObject.fail(
                    new MessageError(HttpStatus.UNAUTHORIZED,
                            INVALID_CREDENTIALS,
                            "Invalid email or password",
                            "Check the password and email you have entered"));
        }
        String accessToken = jwtUtil.generateAccessToken(user.getId(), user.getEmail(), user.getRoles());
        String refreshToken = jwtUtil.generateRefreshToken(user.getId(), user.getEmail());
        long expiresIn      = jwtUtil.getAccessTtlSeconds();

        return ResponseObject.ok(new LoginResponse(user.getId(), user.getEmail(), user.getRoles(), accessToken, refreshToken, expiresIn));
    }

    @Transactional(readOnly = true)
    public ResponseObject<RefreshResponse> refresh(RefreshRequest request) {
        try {
            Claims claims = jwtUtil.parseRefreshToken(request.refreshToken());
            String email = claims.getSubject();
            Long userId = claims.get("id", Long.class);

            Optional<User> userOptional = userRepository.findById(userId)
                    .filter(u -> u.getEmail().equalsIgnoreCase(email));
            if (userOptional.isEmpty()) {
                return ResponseObject.fail(
                        new MessageError(HttpStatus.UNAUTHORIZED,
                                USER_NOT_FOUND,
                                "User not found",
                                "No user found with this email " + email));
            }
            User user = userOptional.get();

            String newAccessToken = jwtUtil.generateAccessToken(user.getId(), user.getEmail(), user.getRoles());
            long expiresIn = jwtUtil.getAccessTtlSeconds();
            String refreshToken = jwtUtil.generateRefreshToken(userId, user.getEmail());

            RefreshResponse body = new RefreshResponse(
                    user.getId(),
                    user.getEmail(),
                    user.getRoles(),
                    newAccessToken,
                    refreshToken,
                    expiresIn
            );
            return ResponseObject.ok(body);
        } catch (JwtException e) {
            return ResponseObject.fail(
                    new MessageError(HttpStatus.UNAUTHORIZED,
                            INVALID_REFRESH_TOKEN,
                            "Invalid or expired refresh token",
                            e.getMessage()));
        }
    }

}
