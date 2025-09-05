package fur.bunnyland.bunnylandapi.api.dto.announce;

import fur.bunnyland.bunnylandapi.domain.AnnouncementStatus;

public record DeleteAnnouncementResponse (Long id,
                                          AnnouncementStatus status) {
}
