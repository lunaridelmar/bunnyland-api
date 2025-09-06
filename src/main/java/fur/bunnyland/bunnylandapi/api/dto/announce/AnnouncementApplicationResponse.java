package fur.bunnyland.bunnylandapi.api.dto.announce;

import java.time.Instant;

public record AnnouncementApplicationResponse(
        Long id,
        Long announcementId,
        String message,
        String contact,
        Instant createdAt
) {
}