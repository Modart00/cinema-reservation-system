package com.modart00.cinema_reservation_system.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.ManyToOne;
import lombok.Data;

@Entity
@Data
public class Seat {
    @Id
    private Long id;
    private int rowNumber;
    private int seatNumber;
    private SeatType seatType;

    @ManyToOne
    private Hall hall;

}
