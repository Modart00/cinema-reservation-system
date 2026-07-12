package com.modart00.cinema_reservation_system.dto.response;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class ReservationSeatResponse {
    private Long id;
    private BigDecimal price;
    private Long reservationId;
    private Long seatId;
    private Long screeningId;
}
