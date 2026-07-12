package com.modart00.cinema_reservation_system.repository;

import com.modart00.cinema_reservation_system.entity.Reservation;
import com.modart00.cinema_reservation_system.entity.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface ReservationRepository extends JpaRepository<Reservation, Long> {
    boolean existsByUserId(Long userId);
    Page<Reservation> findAllByUserId(Long userId, Pageable pageable);
    Optional<Reservation> findByIdAndUserId(Long id, Long userId);
    Optional<Reservation> findByIdAndUser(Long id, User user);
}
