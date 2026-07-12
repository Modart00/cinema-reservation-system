package com.modart00.cinema_reservation_system.repository;

import com.modart00.cinema_reservation_system.entity.Screening;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ScreeningRepository extends JpaRepository<Screening, Long> {
}
