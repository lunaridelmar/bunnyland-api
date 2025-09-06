package fur.bunnyland.bunnylandapi.api.dto.announce;

import jakarta.validation.constraints.NotBlank;

public record ApplyAnnouncementRequest(
        @NotBlank String message,
        @NotBlank String contact
) {
}
