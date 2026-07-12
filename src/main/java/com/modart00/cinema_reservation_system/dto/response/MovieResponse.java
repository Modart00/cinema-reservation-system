package com.modart00.cinema_reservation_system.dto.response;

import com.modart00.cinema_reservation_system.entity.MovieStatus;
import lombok.Data;

import java.time.LocalDate;
import java.io.Serializable;

@Data
public class MovieResponse implements Serializable {
    private Long id;
    private String title;
    private String description;
    private int durationMinutes;
    private String genre;
    private int ageRestriction;
    private LocalDate releaseDate;
    private String posterUrl;
    private MovieStatus status;
}
