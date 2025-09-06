package fur.bunnyland.bunnylandapi.api.dto.announce;

import java.time.Instant;

public record ApplyAnnouncementResponse(
        Long id,
        Long announcementId,
        String message,
        String contact,
        Instant createdAt
) {
}
