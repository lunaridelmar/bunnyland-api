package fur.bunnyland.bunnylandapi.api.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

public record RegisterRequest(@Email @NotBlank String email,
                              @NotBlank String password,
                              String displayName,
                              String city,
                              String country) { }

    