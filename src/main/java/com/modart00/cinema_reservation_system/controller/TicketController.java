package com.modart00.cinema_reservation_system.controller;

import com.modart00.cinema_reservation_system.dto.response.TicketResponse;
import com.modart00.cinema_reservation_system.service.TicketService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
public class TicketController {
    private final TicketService ticketService;

    @GetMapping("/api/tickets/{id}")
    public ResponseEntity<TicketResponse> getCurrentUserTicket(
            @PathVariable Long id,
            Authentication authentication
    ) {
        return ResponseEntity.ok(ticketService.getCurrentUserTicket(id, authentication));
    }

    @GetMapping("/api/tickets")
    public ResponseEntity<Page<TicketResponse>> getCurrentUserTickets(
            Pageable pageable,
            Authentication authentication
    ) {
        return ResponseEntity.ok(ticketService.getCurrentUserTickets(pageable, authentication));
    }

    @PostMapping("/api/admin/tickets/reservation/{reservationId}")
    public ResponseEntity<TicketResponse> createTicket(@PathVariable Long reservationId) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ticketService.createTicket(reservationId));
    }

    @GetMapping("/api/admin/tickets/{id}")
    public ResponseEntity<TicketResponse> getTicketById(@PathVariable Long id) {
        return ResponseEntity.ok(ticketService.getTicketById(id));
    }

    @GetMapping("/api/admin/tickets")
    public ResponseEntity<Page<TicketResponse>> getAllTickets(Pageable pageable) {
        return ResponseEntity.ok(ticketService.getAllTickets(pageable));
    }

    @DeleteMapping("/api/admin/tickets/{id}")
    public ResponseEntity<Void> deleteTicket(@PathVariable Long id) {
        ticketService.deleteTicket(id);
        return ResponseEntity.noContent().build();
    }
}
