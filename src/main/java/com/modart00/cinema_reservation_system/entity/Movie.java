package com.modart00.cinema_reservation_system.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import lombok.Data;

import java.time.LocalDate;

@Entity
@Data
public class Movie {
    @Id
    private Long id;
    private String title;
    private String description;
    private int durationMinutes;
    private String genre;
    private int ageRestriction;
    private LocalDate releaseDate;
    private String posterUrl;
    private String status;
}
