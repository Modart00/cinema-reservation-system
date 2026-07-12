package com.modart00.cinema_reservation_system.dto.response;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class TicketResponse {
    private Long id;
    private String ticketCode;
    private LocalDateTime createdAt;
    private Long reservationId;
}
