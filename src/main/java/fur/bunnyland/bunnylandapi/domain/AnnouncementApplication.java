package fur.bunnyland.bunnylandapi.domain;

import jakarta.persistence.*;
import java.time.Instant;

@Entity
@Table(name = "announcement_applications")
public class AnnouncementApplication {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(optional = false)@JoinColumn(name = "announcement_id")
    private Announcement announcement;

    @Column(nullable = false, columnDefinition = "text")
    private String message;

    @Column(nullable = false, length = 200)
    private String contact;

    @Column(nullable = false)
    private Instant createdAt = Instant.now();

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Announcement getAnnouncement() {
        return announcement;
    }

    public void setAnnouncement(Announcement announcement) {
        this.announcement = announcement;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public String getContact() {
        return contact;
    }

    public void setContact(String contact) {
        this.contact = contact;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Instant createdAt) {
        this.createdAt = createdAt;
    }
}
