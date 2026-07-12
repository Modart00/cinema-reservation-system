package com.modart00.cinema_reservation_system.repository;

import com.modart00.cinema_reservation_system.entity.Ticket;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface TicketRepository extends JpaRepository<Ticket, Long> {
    boolean existsByReservationId(Long reservationId);
    Page<Ticket> findAllByReservationUserId(Long userId, Pageable pageable);
    Optional<Ticket> findByIdAndReservationUserId(Long id, Long userId);
    void deleteByReservationId(Long reservationId);
}
