package com.modart00.cinema_reservation_system.repository;

import com.modart00.cinema_reservation_system.entity.Seat;
import org.springframework.data.jpa.repository.JpaRepository;

public interface SeatRepository extends JpaRepository<Seat, Long> {
}
