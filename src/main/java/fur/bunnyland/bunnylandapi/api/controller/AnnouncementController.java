package fur.bunnyland.bunnylandapi.api.controller;

import fur.bunnyland.bunnylandapi.api.dto.announce.AnnouncementResponse;
import fur.bunnyland.bunnylandapi.api.dto.announce.CreateAnnouncementRequest;
import fur.bunnyland.bunnylandapi.api.dto.announce.CreateAnnouncementResponse;
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
}
