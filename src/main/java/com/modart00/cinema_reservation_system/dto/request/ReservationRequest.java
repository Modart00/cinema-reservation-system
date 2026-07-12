package com.modart00.cinema_reservation_system.dto.request;

import lombok.Data;

import java.util.List;

@Data
public class ReservationRequest {
    private Long screeningId;
    private List<Long> seatIds;
}
