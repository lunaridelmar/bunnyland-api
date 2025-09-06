package fur.bunnyland.bunnylandapi.repository;

import fur.bunnyland.bunnylandapi.domain.AnnouncementApplication;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AnnouncementApplicationRepository extends JpaRepository<AnnouncementApplication, Long> {
}
