package fur.bunnyland.bunnylandapi.api.dto;

import java.util.Set;

public record LoginResponse(
        Long id,
        String email,
        Set<String> roles,
        String accessToken,
        String refreshToken,
        long expiresIn
) {}
