package com.modart00.cinema_reservation_system.repository;

import com.modart00.cinema_reservation_system.entity.Reservation;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ReservationRepository extends JpaRepository<Reservation, Long> {
}
