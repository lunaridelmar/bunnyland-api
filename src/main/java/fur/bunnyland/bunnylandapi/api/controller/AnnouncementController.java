package fur.bunnyland.bunnylandapi.api.controller;

import fur.bunnyland.bunnylandapi.api.dto.announce.*;
import fur.bunnyland.bunnylandapi.domain.ResponseObject;
import fur.bunnyland.bunnylandapi.service.AnnouncementService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.*;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/announcements")
@RequiredArgsConstructor
public class AnnouncementController {

    private final AnnouncementService announcementService;

    @GetMapping
    public ResponseEntity<List<AnnouncementResponse>> list() {
        List<AnnouncementResponse> resp = announcementService.listAll();
        return ResponseEntity.ok(resp);
    }

    @GetMapping("/{id}")
    public ResponseEntity get(@PathVariable Long id) {
        ResponseObject<AnnouncementResponse> resp = announcementService.get(id);
        if (resp.hasError()) {
            return ResponseEntity.status(resp.error().status()).body(resp.error().message());
        }
        return ResponseEntity.ok(resp.body());
    }

    @PostMapping("/{id}/apply")
    public ResponseEntity apply(@PathVariable Long id, @Valid @RequestBody ApplyAnnouncementRequest req) {
        ResponseObject<ApplyAnnouncementResponse> resp = announcementService.apply(id, req);
        if (resp.hasError()) {
            return ResponseEntity.status(resp.error().status()).body(resp.error().message());
        }
        return ResponseEntity.status(HttpStatus.CREATED).body(resp.body());
    }

    @PreAuthorize("hasAuthority('OWNER') or hasRole('OWNER')")
    @PostMapping
    public ResponseEntity create(
            @RequestHeader(value = HttpHeaders.AUTHORIZATION, required = false) String authorization,
            @Valid @RequestBody CreateAnnouncementRequest req
    ) {
        if (authorization == null || !authorization.startsWith("Bearer ")) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body("Missing or invalid Authorization header");
        }
        String token = authorization.substring(7);

        ResponseObject<CreateAnnouncementResponse> resp = announcementService.create(token, req);

        if (resp.hasError()) {
            return ResponseEntity.status(resp.error().status()).body(resp.error());
        }

        HttpHeaders headers = new HttpHeaders();
        headers.set(HttpHeaders.LOCATION, "/api/announcements/" + resp.body().id());
        return new ResponseEntity<>(resp.body(), headers, HttpStatus.CREATED);
    }

    @PreAuthorize("hasAuthority('ADMIN') or hasRole('ADMIN') or hasAuthority('OWNER') or hasRole('OWNER')")
    @DeleteMapping("/{id}")
    public ResponseEntity delete(
            @RequestHeader(value = HttpHeaders.AUTHORIZATION, required = false) String authorization,
            @PathVariable Long id
    ) {
        if (authorization == null || !authorization.startsWith("Bearer ")) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body("Missing or invalid Authorization header");
        }
        String token = authorization.substring(7);

        ResponseObject<DeleteAnnouncementResponse> resp = announcementService.delete(token, id);

        if (resp.hasError()) {
            return ResponseEntity.status(resp.error().status()).body(resp.error().message());
        }

        return ResponseEntity.ok(resp.body());
    }

    @PreAuthorize("hasAuthority('ADMIN') or hasRole('ADMIN')")
    @PatchMapping("/{id}/moderate")
    public ResponseEntity moderate(
            @RequestHeader(value = HttpHeaders.AUTHORIZATION, required = false) String authorization,
            @PathVariable Long id,
            @RequestBody ModerateAnnouncementRequest req
    ) {
        if (authorization == null || !authorization.startsWith("Bearer ")) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body("Missing or invalid Authorization header");
        }
        String token = authorization.substring(7);

        ResponseObject<ModerateAnnouncementResponse> resp = announcementService.moderate(token, id, req.status());

        if (resp.hasError()) {
            return ResponseEntity.status(resp.error().status()).body(resp.error().message());
        }

        return ResponseEntity.ok(resp.body());
    }

    @PreAuthorize("hasAuthority('ADMIN') or hasRole('ADMIN')")
    @PostMapping("/close-expired")
    public ResponseEntity closeExpired(
            @RequestHeader(value = HttpHeaders.AUTHORIZATION, required = false) String authorization
    ) {
        if (authorization == null || !authorization.startsWith("Bearer ")) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body("Missing or invalid Authorization header");
        }
        String token = authorization.substring(7);

        ResponseObject<CloseExpiredAnnouncementsResponse> resp = announcementService.closeExpired(token);

        if (resp.hasError()) {
            return ResponseEntity.status(resp.error().status()).body(resp.error().message());
        }

        return ResponseEntity.ok(resp.body());
    }

    @PreAuthorize("hasAuthority('OWNER') or hasRole('OWNER') or hasAuthority('ADMIN') or hasRole('ADMIN')")
    @GetMapping("/applications")
    public ResponseEntity listApplications(
            @RequestHeader(value = HttpHeaders.AUTHORIZATION, required = false) String authorization
    ) {
        if (authorization == null || !authorization.startsWith("Bearer ")) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body("Missing or invalid Authorization header");
        }
        String token = authorization.substring(7);

        ResponseObject<List<AnnouncementApplicationResponse>> resp = announcementService.listApplicationsForOwner(token);
        if (resp.hasError()) {
            return ResponseEntity.status(resp.error().status()).body(resp.error().message());
        }
        return ResponseEntity.ok(resp.body());
    }
}
