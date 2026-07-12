package com.modart00.cinema_reservation_system.dto.response;

import lombok.Data;

import java.time.LocalDate;

@Data
public class TicketResponse {
    private Long id;
    private String ticketCode;
    private LocalDate createdAt;
    private Long reservationId;
}
