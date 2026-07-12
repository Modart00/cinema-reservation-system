package com.modart00.cinema_reservation_system.repository;

import com.modart00.cinema_reservation_system.entity.Movie;
import org.springframework.data.jpa.repository.JpaRepository;

public interface MovieRepository extends JpaRepository<Movie, Long> {
}
