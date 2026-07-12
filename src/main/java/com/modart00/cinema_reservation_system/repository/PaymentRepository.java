package com.modart00.cinema_reservation_system.repository;

import com.modart00.cinema_reservation_system.entity.Payment;
import com.modart00.cinema_reservation_system.entity.Reservation;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PaymentRepository extends JpaRepository<Payment, Long> {
    boolean existsByReservation(Reservation reservation);
    void deleteByReservationId(Long reservationId);
}
