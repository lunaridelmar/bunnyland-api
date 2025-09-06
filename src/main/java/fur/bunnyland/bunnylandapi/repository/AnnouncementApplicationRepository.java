package fur.bunnyland.bunnylandapi.repository;

import fur.bunnyland.bunnylandapi.domain.AnnouncementApplication;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface AnnouncementApplicationRepository extends JpaRepository<AnnouncementApplication, Long> {
    List<AnnouncementApplication> findByAnnouncementOwnerId(Long ownerId);
    List<AnnouncementApplication> findByAnnouncementOwnerIdAndAnnouncementStatus(Long ownerId, String status);
}
