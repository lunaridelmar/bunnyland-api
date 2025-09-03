package fur.bunnyland.bunnylandapi.api.dto.announce;

import java.time.Instant;
import java.time.LocalDate;

public record CreateAnnouncementResponse(
        Long id,
        Long ownerId,
        String title,
        String description,
        String city,
        String country,
        LocalDate startDate,
        LocalDate endDate,
        String status,
        Instant createdAt
) {}
