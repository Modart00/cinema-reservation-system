package com.modart00.cinema_reservation_system.dto.request;

import lombok.Data;

@Data
public class ReservationRequest {
    private Long userId;
    private Long screeningId;
}
