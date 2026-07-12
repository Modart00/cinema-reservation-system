package com.modart00.cinema_reservation_system.dto.response;

import lombok.Data;

@Data
public class ReservationSeatResponse {
    private Long id;
    private double price;
    private Long reservationId;
    private Long seatId;
}
