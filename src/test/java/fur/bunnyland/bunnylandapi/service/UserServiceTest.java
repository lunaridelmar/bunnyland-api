package fur.bunnyland.bunnylandapi.service;

import fur.bunnyland.bunnylandapi.api.dto.*;
import fur.bunnyland.bunnylandapi.domain.ErrorCode;
import fur.bunnyland.bunnylandapi.domain.ResponseObject;
import fur.bunnyland.bunnylandapi.domain.User;
import fur.bunnyland.bunnylandapi.repository.UserRepository;
import fur.bunnyland.bunnylandapi.security.JwtUtil;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;


import java.util.Optional;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @InjectMocks
    private UserService userService;

    @Mock
    private JwtUtil jwtUtil;

    @Test
    void registerOwnerReturnsConflictWhenEmailTaken() {
        RegisterRequest request = new RegisterRequest("test@example.com", "password", "name", "city", "country");
        when(userRepository.existsByEmail("test@example.com")).thenReturn(true);

        ResponseObject<RegisterResponse> result = userService.registerOwner(request);

        assertThat(result.hasError()).isTrue();
        assertThat(result.error().status()).isEqualTo(HttpStatus.CONFLICT);
        verify(userRepository, never()).save(any());
        verify(passwordEncoder, never()).encode(anyString());
    }

    @Test
    void registerOwnerCreatesUserAndReturnsResponse() {
        RegisterRequest request = new RegisterRequest("new@example.com", "password", "name", "city", "country");
        when(userRepository.existsByEmail("new@example.com")).thenReturn(false);
        when(passwordEncoder.encode("password")).thenReturn("hashed");
        when(userRepository.save(any(User.class))).thenAnswer(invocation -> {
            User user = invocation.getArgument(0);
            user.setId(1L);
            return user;
        });

        ResponseObject<RegisterResponse> result = userService.registerOwner(request);

        assertThat(result.hasError()).isFalse();
        RegisterResponse body = result.body();
        assertThat(body).isNotNull();
        assertThat(body.id()).isEqualTo(1L);
        assertThat(body.email()).isEqualTo("new@example.com");
        assertThat(body.roles()).containsExactly(Role.OWNER);

        ArgumentCaptor<User> userCaptor = ArgumentCaptor.forClass(User.class);
        verify(userRepository).save(userCaptor.capture());
        User saved = userCaptor.getValue();
        assertThat(saved.getPasswordHash()).isEqualTo("hashed");
        assertThat(saved.getRoles()).containsExactly("OWNER");
    }

    @Test
    void loginReturnsUnauthorizedWhenUserNotFound() {
        LoginRequest req = new LoginRequest("nouser@example.com", "pass");
        when(userRepository.findByEmailIgnoreCase("nouser@example.com")).thenReturn(Optional.empty());

        var result = userService.login(req);

        assertThat(result.hasError()).isTrue();
        assertThat(result.error().status()).isEqualTo(HttpStatus.UNAUTHORIZED);
        assertThat(result.error().code()).isEqualTo(ErrorCode.USER_NOT_FOUND);
        verify(passwordEncoder, never()).matches(anyString(), anyString());
        verify(jwtUtil, never()).generateAccessToken(any(), any(), any());
        verify(jwtUtil, never()).generateRefreshToken(any(), any());
    }

    @Test
    void loginReturnsUnauthorizedWhenPasswordInvalid() {
        var user = new User();
        user.setId(10L);
        user.setEmail("kate@example.com");
        user.setPasswordHash("hashed");
        user.setRoles(Set.of("OWNER"));

        LoginRequest req = new LoginRequest("kate@example.com", "wrong");
        when(userRepository.findByEmailIgnoreCase("kate@example.com"))
                .thenReturn(Optional.of(user));
        when(passwordEncoder.matches("wrong", "hashed")).thenReturn(false);

        var result = userService.login(req);

        assertThat(result.hasError()).isTrue();
        assertThat(result.error().status()).isEqualTo(HttpStatus.UNAUTHORIZED);
        assertThat(result.error().code()).isEqualTo(ErrorCode.INVALID_CREDENTIALS);
        verify(jwtUtil, never()).generateAccessToken(any(), any(), any());
        verify(jwtUtil, never()).generateRefreshToken(any(), any());
    }

    @Test
    void loginReturnsTokensAndExpiryOnSuccess() {
        var user = new User();
        user.setId(42L);
        user.setEmail("admin@bunnyland.com");
        user.setPasswordHash("hashed");
        user.setRoles(Set.of("ADMIN"));

        LoginRequest req = new LoginRequest("admin@bunnyland.com", "admin123");
        when(userRepository.findByEmailIgnoreCase("admin@bunnyland.com"))
                .thenReturn(Optional.of(user));
        when(passwordEncoder.matches("admin123", "hashed")).thenReturn(true);

        when(jwtUtil.generateAccessToken(42L, "admin@bunnyland.com", Set.of("ADMIN")))
                .thenReturn("access-token");
        when(jwtUtil.generateRefreshToken(42L, "admin@bunnyland.com"))
                .thenReturn("refresh-token");
        when(jwtUtil.getAccessTtlSeconds()).thenReturn(900L);

        var result = userService.login(req);

        assertThat(result.hasError()).isFalse();
        LoginResponse body = result.body();
        assertThat(body).isNotNull();
        assertThat(body.id()).isEqualTo(42L);
        assertThat(body.email()).isEqualTo("admin@bunnyland.com");
        assertThat(body.roles()).containsExactlyInAnyOrder("ADMIN");
        assertThat(body.accessToken()).isEqualTo("access-token");
        assertThat(body.refreshToken()).isEqualTo("refresh-token");
        assertThat(body.expiresIn()).isEqualTo(900L);

        verify(jwtUtil).generateAccessToken(42L, "admin@bunnyland.com", Set.of("ADMIN"));
        verify(jwtUtil).generateRefreshToken(42L, "admin@bunnyland.com");
        verify(jwtUtil).getAccessTtlSeconds();
    }

    @Test
    void refreshReturnsNewTokensOnSuccess() {
        // given
        String refresh = "refresh-token";
        Claims claims = mock(Claims.class);
        when(jwtUtil.parseRefreshToken(refresh)).thenReturn(claims);
        when(claims.getSubject()).thenReturn("admin@bunnyland.com");
        when(claims.get("id", Long.class)).thenReturn(42L);

        var user = new User();
        user.setId(42L);
        user.setEmail("admin@bunnyland.com");
        user.setRoles(Set.of("ADMIN"));

        when(userRepository.findById(42L)).thenReturn(Optional.of(user));
        when(jwtUtil.generateAccessToken(42L, "admin@bunnyland.com", Set.of("ADMIN"))).thenReturn("new-access");
        when(jwtUtil.generateRefreshToken(42L, "admin@bunnyland.com")).thenReturn("new-refresh");
        when(jwtUtil.getAccessTtlSeconds()).thenReturn(900L);

        // when
        var result = userService.refresh(new RefreshRequest(refresh));

        // then
        assertThat(result.hasError()).isFalse();
        RefreshResponse body = result.body();
        assertThat(body).isNotNull();
        assertThat(body.id()).isEqualTo(42L);
        assertThat(body.email()).isEqualTo("admin@bunnyland.com");
        assertThat(body.roles()).containsExactlyInAnyOrder("ADMIN");
        assertThat(body.accessToken()).isEqualTo("new-access");
        assertThat(body.refreshToken()).isEqualTo("new-refresh");
        assertThat(body.expiresIn()).isEqualTo(900L);

        verify(jwtUtil).parseRefreshToken(refresh);
        verify(jwtUtil).generateAccessToken(42L, "admin@bunnyland.com", Set.of("ADMIN"));
        verify(jwtUtil).generateRefreshToken(42L, "admin@bunnyland.com");
        verify(jwtUtil).getAccessTtlSeconds();
    }

    @Test
    void refreshFailsWhenUserNotFoundOrEmailMismatch() {
        // given: token says id=7 & subject=email A, but DB has a different email (filter makes Optional empty)
        String refresh = "rt";
        Claims claims = mock(Claims.class);
        when(jwtUtil.parseRefreshToken(refresh)).thenReturn(claims);
        when(claims.getSubject()).thenReturn("alice@example.com");
        when(claims.get("id", Long.class)).thenReturn(7L);

        var dbUser = new User();
        dbUser.setId(7L);
        dbUser.setEmail("different@example.com"); // mismatch → filter fails

        when(userRepository.findById(7L)).thenReturn(Optional.of(dbUser));

        // when
        var result = userService.refresh(new RefreshRequest(refresh));

        // then
        assertThat(result.hasError()).isTrue();
        assertThat(result.error().status()).isEqualTo(HttpStatus.UNAUTHORIZED);
        assertThat(result.error().code()).isEqualTo(ErrorCode.USER_NOT_FOUND);
        verify(jwtUtil, never()).generateAccessToken(any(), any(), any());
        verify(jwtUtil, never()).generateRefreshToken(any(), any());
    }

    @Test
    void refreshFailsWhenTokenInvalid() {
        // given
        String refresh = "bad-token";
        when(jwtUtil.parseRefreshToken(refresh)).thenThrow(new JwtException("invalid"));

        // when
        var result = userService.refresh(new RefreshRequest(refresh));

        // then
        assertThat(result.hasError()).isTrue();
        assertThat(result.error().status()).isEqualTo(HttpStatus.UNAUTHORIZED);
        assertThat(result.error().code()).isEqualTo(ErrorCode.INVALID_REFRESH_TOKEN);
        verify(userRepository, never()).findById(any());
        verify(jwtUtil, never()).generateAccessToken(any(), any(), any());
        verify(jwtUtil, never()).generateRefreshToken(any(), any());
    }

    @Test
    void meReturnsProfileWhenTokenValid() {
        // given
        String access = "access-token";
        Claims claims = mock(Claims.class);
        when(jwtUtil.parseAccessToken(access)).thenReturn(claims);
        when(claims.get("id", Long.class)).thenReturn(15L);

        var user = new User();
        user.setId(15L);
        user.setEmail("user@example.com");
        user.setDisplayName("User");
        user.setCity("City");
        user.setCountry("Country");
        user.setRoles(Set.of("OWNER"));

        when(userRepository.findById(15L)).thenReturn(Optional.of(user));

        // when
        var result = userService.me(access);

        // then
        assertThat(result.hasError()).isFalse();
        ProfileResponse body = result.body();
        assertThat(body).isNotNull();
        assertThat(body.id()).isEqualTo(15L);
        assertThat(body.email()).isEqualTo("user@example.com");
        assertThat(body.displayName()).isEqualTo("User");
        assertThat(body.city()).isEqualTo("City");
        assertThat(body.country()).isEqualTo("Country");
        assertThat(body.roles()).containsExactly(Role.OWNER);

        verify(jwtUtil).parseAccessToken(access);
        verify(userRepository).findById(15L);
    }

    @Test
    void meFailsWhenUserNotFound() {
        // given
        String access = "a";
        Claims claims = mock(Claims.class);
        when(jwtUtil.parseAccessToken(access)).thenReturn(claims);
        when(claims.get("id", Long.class)).thenReturn(9L);

        when(userRepository.findById(9L)).thenReturn(Optional.empty());

        // when
        var result = userService.me(access);

        // then
        assertThat(result.hasError()).isTrue();
        assertThat(result.error().status()).isEqualTo(HttpStatus.UNAUTHORIZED);
        assertThat(result.error().code()).isEqualTo(ErrorCode.USER_NOT_FOUND);
        verify(jwtUtil).parseAccessToken(access);
        verify(userRepository).findById(9L);
    }

    @Test
    void meFailsWhenTokenInvalid() {
        // given
        String access = "bad";
        when(jwtUtil.parseAccessToken(access)).thenThrow(new JwtException("invalid"));

        // when
        var result = userService.me(access);

        // then
        assertThat(result.hasError()).isTrue();
        assertThat(result.error().status()).isEqualTo(HttpStatus.UNAUTHORIZED);
        assertThat(result.error().code()).isEqualTo(ErrorCode.INVALID_CREDENTIALS);
        verify(userRepository, never()).findById(any());
    }
}
