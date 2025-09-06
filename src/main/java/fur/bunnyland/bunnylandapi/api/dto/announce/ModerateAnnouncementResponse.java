package fur.bunnyland.bunnylandapi.api.dto.announce;

import fur.bunnyland.bunnylandapi.domain.AnnouncementStatus;

public record ModerateAnnouncementResponse(Long id,
                                           AnnouncementStatus status) {
}

