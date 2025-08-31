package fur.bunnyland.bunnylandapi.api.dto;

import java.util.Set;

public record RegisterResponse(Long id,
                               String email,
                               String displayName,
                               String city,
                               String country,
                               Set<Role> roles) { }