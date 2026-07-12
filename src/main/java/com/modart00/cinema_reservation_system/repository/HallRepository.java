package com.modart00.cinema_reservation_system.repository;

import com.modart00.cinema_reservation_system.entity.Hall;
import org.springframework.data.jpa.repository.JpaRepository;

public interface HallRepository extends JpaRepository<Hall, Long> {
}
