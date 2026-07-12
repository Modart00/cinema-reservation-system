package com.modart00.cinema_reservation_system.controller;

import com.modart00.cinema_reservation_system.dto.request.ReservationRequest;
import com.modart00.cinema_reservation_system.dto.response.ReservationResponse;
import com.modart00.cinema_reservation_system.service.ReservationService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
public class ReservationController {
    private final ReservationService reservationService;

    @PostMapping("/api/reservations")
    public ResponseEntity<ReservationResponse> createReservation(
            @Valid @RequestBody ReservationRequest request,
            Authentication authentication
    ) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(reservationService.createReservation(request, authentication));
    }

    @GetMapping("/api/reservations/{id}")
    public ResponseEntity<ReservationResponse> getCurrentUserReservation(
            @PathVariable Long id,
            Authentication authentication
    ) {
        return ResponseEntity.ok(reservationService.getCurrentUserReservation(id, authentication));
    }

    @GetMapping("/api/reservations")
    public ResponseEntity<Page<ReservationResponse>> getCurrentUserReservations(
            Pageable pageable,
            Authentication authentication
    ) {
        return ResponseEntity.ok(reservationService.getCurrentUserReservations(pageable, authentication));
    }

    @GetMapping("/api/admin/reservations/{id}")
    public ResponseEntity<ReservationResponse> getReservationById(@PathVariable Long id) {
        return ResponseEntity.ok(reservationService.getReservationById(id));
    }

    @GetMapping("/api/admin/reservations")
    public ResponseEntity<Page<ReservationResponse>> getAllReservations(Pageable pageable) {
        return ResponseEntity.ok(reservationService.getAllReservations(pageable));
    }

    @DeleteMapping("/api/admin/reservations/{id}")
    public ResponseEntity<Void> deleteReservation(@PathVariable Long id) {
        reservationService.deleteReservation(id);
        return ResponseEntity.noContent().build();
    }
}
