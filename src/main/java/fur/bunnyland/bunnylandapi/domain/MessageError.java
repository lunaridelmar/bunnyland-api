package fur.bunnyland.bunnylandapi.domain;

import org.springframework.http.HttpStatus;

public record MessageError(
        HttpStatus status,
        ErrorCode code,
        String message,
        String details
) {}
