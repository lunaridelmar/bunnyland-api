package fur.bunnyland.bunnylandapi.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import fur.bunnyland.bunnylandapi.domain.User;

public interface UserRepository extends JpaRepository<User, Long> {
    boolean existsByEmail(String email);
    Optional<User> findByEmail(String email);
}

