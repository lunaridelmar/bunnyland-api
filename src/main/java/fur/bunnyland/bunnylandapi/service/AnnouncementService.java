package fur.bunnyland.bunnylandapi.service;

import fur.bunnyland.bunnylandapi.api.dto.announce.AnnouncementResponse;
import fur.bunnyland.bunnylandapi.api.dto.announce.CreateAnnouncementRequest;
import fur.bunnyland.bunnylandapi.api.dto.announce.CreateAnnouncementResponse;
import fur.bunnyland.bunnylandapi.domain.*;
import fur.bunnyland.bunnylandapi.repository.AnnouncementRepository;
import fur.bunnyland.bunnylandapi.repository.UserRepository;
import fur.bunnyland.bunnylandapi.security.JwtUtil;
import io.jsonwebtoken.Claims;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

import static fur.bunnyland.bunnylandapi.domain.ErrorCode.INVALID_DATES;
import static fur.bunnyland.bunnylandapi.domain.ErrorCode.USER_NOT_FOUND;

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
                            USER_NOT_FOUND,
                            "User not found",
                            "Invalid token user id")
            );
        }

        // Basic date sanity: endDate >= startDate (if both present)
        if (req.startDate() != null && req.endDate() != null && req.endDate().isBefore(req.startDate())) {
            return ResponseObject.fail(
                    new MessageError(HttpStatus.BAD_REQUEST, INVALID_DATES, "endDate cannot be before startDate", "put a proper endDate")
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
        return announcementRepository.findAll().stream()
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
}
