package com.modart00.cinema_reservation_system.dto.response;

import lombok.Data;

import java.time.LocalDate;

@Data
public class MovieResponse {
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
