package fur.bunnyland.bunnylandapi.service;

import fur.bunnyland.bunnylandapi.api.dto.announce.AnnouncementResponse;
import fur.bunnyland.bunnylandapi.api.dto.announce.CreateAnnouncementRequest;
import fur.bunnyland.bunnylandapi.api.dto.announce.CreateAnnouncementResponse;
import fur.bunnyland.bunnylandapi.api.dto.announce.DeleteAnnouncementResponse;
import fur.bunnyland.bunnylandapi.api.dto.announce.CloseExpiredAnnouncementsResponse;
import fur.bunnyland.bunnylandapi.domain.*;
import fur.bunnyland.bunnylandapi.repository.AnnouncementRepository;
import fur.bunnyland.bunnylandapi.repository.UserRepository;
import fur.bunnyland.bunnylandapi.security.JwtUtil;
import io.jsonwebtoken.Claims;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.Objects;

@Service
@RequiredArgsConstructor
public class AnnouncementService {

    private final AnnouncementRepository announcementRepository;
    private final UserRepository userRepository;
    private final JwtUtil jwtUtil;

    @Transactional
    public ResponseObject<CreateAnnouncementResponse> create(String bearerToken, CreateAnnouncementRequest req) {
        // Extract owner from token
        Claims claims = jwtUtil.parseAccessToken(bearerToken);
        Long userId = claims.get("id", Long.class);

        User owner = userRepository.findById(userId).orElse(null);
        if (owner == null) {
            return ResponseObject.fail(
                    new MessageError(HttpStatus.UNAUTHORIZED,
                            ErrorCode.USER_NOT_FOUND,
                            "User not found",
                            "Invalid token user id")
            );
        }

        // Basic date sanity: endDate >= startDate (if both present)
        if (req.startDate() != null && req.endDate() != null && req.endDate().isBefore(req.startDate())) {
            return ResponseObject.fail(
                    new MessageError(HttpStatus.BAD_REQUEST, ErrorCode.INVALID_DATES, "endDate cannot be before startDate", "put a proper endDate")
            );
        }

        Announcement a = new Announcement();
        a.setOwner(owner);
        a.setTitle(req.title());
        a.setDescription(req.description());
        a.setCity(req.city());
        a.setCountry(req.country());
        a.setStartDate(req.startDate());
        a.setEndDate(req.endDate());
        a.setStatus(AnnouncementStatus.OPEN.name());

        Announcement saved = announcementRepository.save(a);

        CreateAnnouncementResponse body = new CreateAnnouncementResponse(
                saved.getId(),
                owner.getId(),
                saved.getTitle(),
                saved.getDescription(),
                saved.getCity(),
                saved.getCountry(),
                saved.getStartDate(),
                saved.getEndDate(),
                saved.getStatus(),
                saved.getCreatedAt()
        );
        return ResponseObject.ok(body);
    }

    @Transactional(readOnly = true)
    public List<AnnouncementResponse> listAll() {
        return announcementRepository.findByStatus(AnnouncementStatus.OPEN.name()).stream()
                .filter(a -> Objects.equals(a.getStatus(), AnnouncementStatus.OPEN.name()))
                .map(a -> new AnnouncementResponse(
                        a.getId(),
                        a.getOwner().getId(),
                        a.getTitle(),
                        a.getDescription(),
                        a.getCity(),
                        a.getCountry(),
                        a.getStartDate(),
                        a.getEndDate(),
                        a.getStatus(),
                        a.getCreatedAt()
                ))
                .toList();
    }

    @Transactional
    public ResponseObject<CloseExpiredAnnouncementsResponse> closeExpired(String bearerToken) {
        Claims claims = jwtUtil.parseAccessToken(bearerToken);
        List<String> roles = claims.get("roles", List.class);

        boolean isAdmin = roles != null && roles.contains("ADMIN");
        if (!isAdmin) {
            return ResponseObject.fail(
                    new MessageError(HttpStatus.FORBIDDEN,
                            ErrorCode.FORBIDDEN,
                            "Forbidden to use",
                            "Only admin can close expired announcements")
            );
        }

        LocalDate today = LocalDate.now();
        List<Announcement> expired = announcementRepository
                .findByStatusAndEndDateBefore(AnnouncementStatus.OPEN.name(), today);
        for (Announcement a : expired) {
            a.setStatus(AnnouncementStatus.CLOSED.name());
        }
        announcementRepository.saveAll(expired);

        return ResponseObject.ok(new CloseExpiredAnnouncementsResponse(expired.size()));
    }

    @Transactional(readOnly = true)
    public ResponseObject<AnnouncementResponse> get(Long id) {
        return announcementRepository.findById(id)
                .filter(a  -> !Objects.equals(a.getStatus(), AnnouncementStatus.DELETED.name()))
                .map(a -> ResponseObject.ok(new AnnouncementResponse(
                        a.getId(),
                        a.getOwner().getId(),
                        a.getTitle(),
                        a.getDescription(),
                        a.getCity(),
                        a.getCountry(),
                        a.getStartDate(),
                        a.getEndDate(),
                        a.getStatus(),
                        a.getCreatedAt()
                )))
                .orElseGet(() -> ResponseObject.fail(
                        new MessageError(HttpStatus.NOT_FOUND,
                                ErrorCode.ANNOUNCEMENT_NOT_FOUND,
                                "Announcement not found",
                                "No announcement found with id " + id))
                );
    }

    @Transactional
    public ResponseObject<DeleteAnnouncementResponse> delete(String bearerToken, Long id) {
        Claims claims = jwtUtil.parseAccessToken(bearerToken);
        Long userId = claims.get("id", Long.class);
        List<String> roles = claims.get("roles", List.class);

        Announcement a = announcementRepository.findById(id).orElse(null);
        if (a == null) {
            return ResponseObject.fail(
                    new MessageError(HttpStatus.NOT_FOUND,
                            ErrorCode.ANNOUNCEMENT_NOT_FOUND,
                            "Announcement not found",
                            "No announcement found with id " + id)
            );
        }

        boolean isAdmin = roles != null && roles.contains("ADMIN");
        if (!isAdmin && !a.getOwner().getId().equals(userId)) {
            return ResponseObject.fail(
                    new MessageError(HttpStatus.FORBIDDEN,
                            ErrorCode.FORBIDDEN,
                            "Forbidden",
                            "Only admin or owner can delete this announcement")
            );
        }

        a.setStatus(AnnouncementStatus.DELETED.name());
        announcementRepository.save(a);
        DeleteAnnouncementResponse body = new DeleteAnnouncementResponse(a.getId(), AnnouncementStatus.DELETED);
        return ResponseObject.ok(body);
    }
}
