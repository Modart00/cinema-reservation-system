package com.modart00.cinema_reservation_system.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Movie {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String title;
    private String description;
    private int durationMinutes;
    private String genre;
    private int ageRestriction;
    private LocalDate releaseDate;
    private String posterUrl;
    @Enumerated(EnumType.STRING)
    private MovieStatus status;
}
