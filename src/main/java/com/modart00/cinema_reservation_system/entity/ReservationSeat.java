package com.modart00.cinema_reservation_system.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.ManyToOne;
import lombok.Data;

@Entity
@Data
public class ReservationSeat {
    @Id
    private Long id;
    private double price;

    @ManyToOne
    private Reservation reservation;

    @ManyToOne
    private Seat seat;

}
