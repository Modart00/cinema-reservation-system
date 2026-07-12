package com.modart00.cinema_reservation_system.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.ManyToOne;
import lombok.Data;

import java.time.LocalDate;

@Entity
@Data
public class Reservation {
    @Id
    private Long id;
    private String reservationCode;
    private ReservationStatus status;
    private LocalDate createdAt;
    private LocalDate expiresAt;
    private double totalPrice;

    @ManyToOne
    private User user;

    @ManyToOne
    private Screening screening;
}
