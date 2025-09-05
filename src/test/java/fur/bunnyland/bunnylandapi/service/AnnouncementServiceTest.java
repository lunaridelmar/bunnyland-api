package fur.bunnyland.bunnylandapi.service;

import fur.bunnyland.bunnylandapi.api.dto.announce.AnnouncementResponse;
import fur.bunnyland.bunnylandapi.api.dto.announce.CreateAnnouncementRequest;
import fur.bunnyland.bunnylandapi.api.dto.announce.CreateAnnouncementResponse;
import fur.bunnyland.bunnylandapi.api.dto.announce.DeleteAnnouncementResponse;
import fur.bunnyland.bunnylandapi.domain.*;
import fur.bunnyland.bunnylandapi.repository.AnnouncementRepository;
import fur.bunnyland.bunnylandapi.repository.UserRepository;
import fur.bunnyland.bunnylandapi.security.JwtUtil;
import io.jsonwebtoken.Claims;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AnnouncementServiceTest {

    @Mock
    private AnnouncementRepository announcementRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private JwtUtil jwtUtil;

    @InjectMocks
    private AnnouncementService announcementService;

    @Test
    void createFailsWhenUserNotFound() {
        String token = "token";
        Claims claims = mock(Claims.class);
        when(jwtUtil.parseAccessToken(token)).thenReturn(claims);
        when(claims.get("id", Long.class)).thenReturn(99L);
        when(userRepository.findById(99L)).thenReturn(Optional.empty());

        CreateAnnouncementRequest req = new CreateAnnouncementRequest("title", "desc", null, null, null, null);

        ResponseObject<CreateAnnouncementResponse> result = announcementService.create(token, req);

        assertThat(result.hasError()).isTrue();
        assertThat(result.error().status()).isEqualTo(HttpStatus.UNAUTHORIZED);
        assertThat(result.error().code()).isEqualTo(ErrorCode.USER_NOT_FOUND);
        verify(announcementRepository, never()).save(any());
    }

    @Test
    void createFailsWhenEndDateBeforeStartDate() {
        String token = "token";
        Claims claims = mock(Claims.class);
        when(jwtUtil.parseAccessToken(token)).thenReturn(claims);
        when(claims.get("id", Long.class)).thenReturn(1L);

        User owner = new User();
        owner.setId(1L);
        when(userRepository.findById(1L)).thenReturn(Optional.of(owner));

        LocalDate start = LocalDate.of(2024, 1, 10);
        LocalDate end = LocalDate.of(2024, 1, 5);
        CreateAnnouncementRequest req = new CreateAnnouncementRequest("title", "desc", null, null, start, end);

        ResponseObject<CreateAnnouncementResponse> result = announcementService.create(token, req);

        assertThat(result.hasError()).isTrue();
        assertThat(result.error().status()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(result.error().code()).isEqualTo(ErrorCode.INVALID_DATES);
        verify(announcementRepository, never()).save(any());
    }

    @Test
    void createPersistsAnnouncementAndReturnsResponse() {
        String token = "token";
        Claims claims = mock(Claims.class);
        when(jwtUtil.parseAccessToken(token)).thenReturn(claims);
        when(claims.get("id", Long.class)).thenReturn(5L);

        User owner = new User();
        owner.setId(5L);
        when(userRepository.findById(5L)).thenReturn(Optional.of(owner));

        CreateAnnouncementRequest req = new CreateAnnouncementRequest("title", "desc", "city", "country", null, null);

        when(announcementRepository.save(any(Announcement.class))).thenAnswer(invocation -> {
            Announcement a = invocation.getArgument(0);
            a.setId(10L);
            return a;
        });

        ResponseObject<CreateAnnouncementResponse> result = announcementService.create(token, req);

        assertThat(result.hasError()).isFalse();
        CreateAnnouncementResponse body = result.body();
        assertThat(body.id()).isEqualTo(10L);
        assertThat(body.ownerId()).isEqualTo(5L);
        assertThat(body.title()).isEqualTo("title");

        ArgumentCaptor<Announcement> captor = ArgumentCaptor.forClass(Announcement.class);
        verify(announcementRepository).save(captor.capture());
        Announcement saved = captor.getValue();
        assertThat(saved.getOwner()).isEqualTo(owner);
        assertThat(saved.getTitle()).isEqualTo("title");
        assertThat(saved.getDescription()).isEqualTo("desc");
        assertThat(saved.getStatus()).isEqualTo(AnnouncementStatus.OPEN.name());
    }

    @Test
    void listAllReturnsMappedAnnouncements() {
        User owner = new User();
        owner.setId(3L);
        Announcement a = new Announcement();
        a.setId(7L);
        a.setOwner(owner);
        a.setTitle("t");
        a.setDescription("d");
        when(announcementRepository.findByStatus(AnnouncementStatus.OPEN.name())).thenReturn(List.of(a));

        List<AnnouncementResponse> result = announcementService.listAll();

        assertThat(result).hasSize(1);
        AnnouncementResponse resp = result.get(0);
        assertThat(resp.id()).isEqualTo(7L);
        assertThat(resp.ownerId()).isEqualTo(3L);
        assertThat(resp.title()).isEqualTo("t");
        assertThat(resp.description()).isEqualTo("d");
        verify(announcementRepository).findByStatus(AnnouncementStatus.OPEN.name());
    }

    @Test
    void listAllClosesExpiredAnnouncements() {
        User owner = new User();
        owner.setId(3L);
        Announcement a = new Announcement();
        a.setId(7L);
        a.setOwner(owner);
        a.setTitle("t");
        a.setDescription("d");
        a.setEndDate(LocalDate.now().minusDays(1));
        when(announcementRepository.findByStatus(AnnouncementStatus.OPEN.name())).thenReturn(List.of(a));

        List<AnnouncementResponse> result = announcementService.listAll();

        assertThat(result).isEmpty();
        assertThat(a.getStatus()).isEqualTo(AnnouncementStatus.CLOSED.name());
        verify(announcementRepository).save(a);
        verify(announcementRepository).findByStatus(AnnouncementStatus.OPEN.name());
    }

    @Test
    void getReturnsErrorWhenAnnouncementNotFound() {
        when(announcementRepository.findById(1L)).thenReturn(Optional.empty());

        ResponseObject<AnnouncementResponse> result = announcementService.get(1L);

        assertThat(result.hasError()).isTrue();
        assertThat(result.error().status()).isEqualTo(HttpStatus.NOT_FOUND);
        assertThat(result.error().code()).isEqualTo(ErrorCode.ANNOUNCEMENT_NOT_FOUND);
    }

    @Test
    void getReturnsMappedAnnouncement() {
        User owner = new User();
        owner.setId(3L);
        Announcement a = new Announcement();
        a.setId(7L);
        a.setOwner(owner);
        a.setTitle("t");
        a.setDescription("d");
        when(announcementRepository.findById(7L)).thenReturn(Optional.of(a));

        ResponseObject<AnnouncementResponse> result = announcementService.get(7L);

        assertThat(result.hasError()).isFalse();
        AnnouncementResponse resp = result.body();
        assertThat(resp.id()).isEqualTo(7L);
        assertThat(resp.ownerId()).isEqualTo(3L);
        assertThat(resp.title()).isEqualTo("t");
        assertThat(resp.description()).isEqualTo("d");
    }

    @Test
    void deleteReturnsErrorWhenAnnouncementNotFound() {
        String token = "token";
        Claims claims = mock(Claims.class);
        when(jwtUtil.parseAccessToken(token)).thenReturn(claims);
        when(claims.get("id", Long.class)).thenReturn(1L);
        when(claims.get("roles", List.class)).thenReturn(List.of("OWNER"));
        when(announcementRepository.findById(5L)).thenReturn(Optional.empty());

        ResponseObject<DeleteAnnouncementResponse> result = announcementService.delete(token, 5L);

        assertThat(result.hasError()).isTrue();
        assertThat(result.error().status()).isEqualTo(HttpStatus.NOT_FOUND);
        assertThat(result.error().code()).isEqualTo(ErrorCode.ANNOUNCEMENT_NOT_FOUND);
    }

    @Test
    void deleteFailsWhenUserIsNotOwnerOrAdmin() {
        String token = "token";
        Claims claims = mock(Claims.class);
        when(jwtUtil.parseAccessToken(token)).thenReturn(claims);
        when(claims.get("id", Long.class)).thenReturn(2L);
        when(claims.get("roles", List.class)).thenReturn(List.of("OWNER"));

        User owner = new User();
        owner.setId(1L);
        Announcement a = new Announcement();
        a.setId(5L);
        a.setOwner(owner);
        when(announcementRepository.findById(5L)).thenReturn(Optional.of(a));

        ResponseObject<DeleteAnnouncementResponse> result = announcementService.delete(token, 5L);

        assertThat(result.hasError()).isTrue();
        assertThat(result.error().status()).isEqualTo(HttpStatus.FORBIDDEN);
        assertThat(result.error().code()).isEqualTo(ErrorCode.FORBIDDEN);
        verify(announcementRepository, never()).save(any());
    }

    @Test
    void deleteDeletesWhenOwner() {
        String token = "token";
        Claims claims = mock(Claims.class);
        when(jwtUtil.parseAccessToken(token)).thenReturn(claims);
        when(claims.get("id", Long.class)).thenReturn(1L);
        when(claims.get("roles", List.class)).thenReturn(List.of("OWNER"));

        User owner = new User();
        owner.setId(1L);
        Announcement a = new Announcement();
        a.setId(5L);
        a.setOwner(owner);
        when(announcementRepository.findById(5L)).thenReturn(Optional.of(a));

        ResponseObject<DeleteAnnouncementResponse> result = announcementService.delete(token, 5L);

        assertThat(result.hasError()).isFalse();
        assertThat(a.getStatus()).isEqualTo(AnnouncementStatus.DELETED.name());
        assertThat(result.body().id()).isEqualTo(5L);
        assertThat(result.body().status()).isEqualTo(AnnouncementStatus.DELETED);
        verify(announcementRepository).save(a);
    }

    @Test
    void deleteDeletesWhenAdmin() {
        String token = "token";
        Claims claims = mock(Claims.class);
        when(jwtUtil.parseAccessToken(token)).thenReturn(claims);
        when(claims.get("id", Long.class)).thenReturn(9L);
        when(claims.get("roles", List.class)).thenReturn(List.of("ADMIN"));

        User owner = new User();
        owner.setId(1L);
        Announcement a = new Announcement();
        a.setId(5L);
        a.setOwner(owner);
        when(announcementRepository.findById(5L)).thenReturn(Optional.of(a));

        ResponseObject<DeleteAnnouncementResponse> result = announcementService.delete(token, 5L);

        assertThat(result.hasError()).isFalse();
        assertThat(a.getStatus()).isEqualTo(AnnouncementStatus.DELETED.name());
        assertThat(result.body().id()).isEqualTo(5L);
        assertThat(result.body().status()).isEqualTo(AnnouncementStatus.DELETED);
        verify(announcementRepository).save(a);
    }
}

