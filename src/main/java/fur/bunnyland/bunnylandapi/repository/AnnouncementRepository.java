package fur.bunnyland.bunnylandapi.repository;

import fur.bunnyland.bunnylandapi.domain.Announcement;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AnnouncementRepository extends JpaRepository<Announcement, Long> {
}
