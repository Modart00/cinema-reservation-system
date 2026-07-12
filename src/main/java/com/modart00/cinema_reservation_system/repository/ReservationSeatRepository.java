package com.modart00.cinema_reservation_system.repository;

import com.modart00.cinema_reservation_system.entity.ReservationSeat;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface ReservationSeatRepository extends JpaRepository<ReservationSeat, Long> {
    boolean existsByScreeningIdAndSeatId(Long screeningId, Long seatId);
    Page<ReservationSeat> findAllByReservationUserId(Long userId, Pageable pageable);
    Optional<ReservationSeat> findByIdAndReservationUserId(Long id, Long userId);
    List<ReservationSeat> findAllByScreeningId(Long screeningId);
    void deleteAllByReservationId(Long reservationId);
    void deleteAllByScreeningId(Long screeningId);
}
