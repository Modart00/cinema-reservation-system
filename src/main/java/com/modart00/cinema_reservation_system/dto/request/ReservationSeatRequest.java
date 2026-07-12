package com.modart00.cinema_reservation_system.dto.request;

import lombok.Data;

@Data
public class ReservationSeatRequest {
    private double price;
    private Long reservationId;
    private Long seatId;
}
