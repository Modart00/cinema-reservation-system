package com.modart00.cinema_reservation_system.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.ManyToOne;
import lombok.Data;

import java.time.LocalDate;

@Entity
@Data
public class Screening {
    @Id
    private Long id;
    private LocalDate startTime;
    private LocalDate endTime;
    private double price;
    private ScreeningStatus status;

    @ManyToOne
    private Movie movie;

    @ManyToOne
    private Hall hall;
}
