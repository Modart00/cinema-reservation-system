package com.modart00.cinema_reservation_system.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.OneToOne;
import lombok.Data;

import java.time.LocalDate;

@Entity
@Data
public class Ticket {
    @Id
    private Long id;
    private String ticketCode;
    private LocalDate createdAt;

    @OneToOne
    private Reservation reservation;

}
