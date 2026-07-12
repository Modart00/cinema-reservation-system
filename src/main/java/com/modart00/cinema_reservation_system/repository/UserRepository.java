package com.modart00.cinema_reservation_system.repository;

import com.modart00.cinema_reservation_system.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {

    Optional<User> findByEmail(String email);
    boolean existsByEmail(String email);
    boolean existsByUsername(String username);
    Optional<User> findByRefreshToken(String refreshToken);
    Optional<User> findByToken_Token(String token);

}
