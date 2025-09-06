package fur.bunnyland.bunnylandapi.repository;

import fur.bunnyland.bunnylandapi.domain.Announcement;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
import java.util.List;

public interface AnnouncementRepository extends JpaRepository<Announcement, Long> {
    List<Announcement> findByStatus(String status);

    List<Announcement> findByStatusAndEndDateBefore(String status, LocalDate endDate);
}
