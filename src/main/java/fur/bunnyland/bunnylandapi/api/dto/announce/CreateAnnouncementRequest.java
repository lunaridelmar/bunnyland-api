package fur.bunnyland.bunnylandapi.api.dto.announce;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

import java.time.LocalDate;

public record CreateAnnouncementRequest(
        @NotBlank @Size(max = 200) String title,
        @NotBlank String description,
        @Size(max = 120) String city,
        @Size(max = 120) String country,
        LocalDate startDate,
        LocalDate endDate
) {}
