package fur.bunnyland.bunnylandapi.service;

import fur.bunnyland.bunnylandapi.api.dto.RegisterRequest;
import fur.bunnyland.bunnylandapi.api.dto.RegisterResponse;
import fur.bunnyland.bunnylandapi.api.dto.Role;
import fur.bunnyland.bunnylandapi.domain.ResponseObject;
import fur.bunnyland.bunnylandapi.domain.User;
import fur.bunnyland.bunnylandapi.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;


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
}
