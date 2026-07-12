package com.modart00.cinema_reservation_system.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.OneToOne;
import lombok.Data;

import java.time.LocalDate;

@Entity
@Data
public class Payment {
    @Id
    private Long id;
    private String paymentCode;
    private double amount;
    private String status;
    private LocalDate paymentDate;

    @OneToOne
    private Reservation reservation;
}
