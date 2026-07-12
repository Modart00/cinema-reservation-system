package com.modart00.cinema_reservation_system.repository;

import com.modart00.cinema_reservation_system.entity.ReservationSeat;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ReservationSeatRepository extends JpaRepository<ReservationSeat, Long> {
}
