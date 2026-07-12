package com.modart00.cinema_reservation_system.controller;

import com.modart00.cinema_reservation_system.dto.response.ReservationSeatResponse;
import com.modart00.cinema_reservation_system.service.ReservationSeatService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
public class ReservationSeatController {
    private final ReservationSeatService reservationSeatService;

    @GetMapping("/api/reservation-seats/{id}")
    public ResponseEntity<ReservationSeatResponse> getCurrentUserReservationSeat(
            @PathVariable Long id,
            Authentication authentication
    ) {
        return ResponseEntity.ok(
                reservationSeatService.getCurrentUserReservationSeat(id, authentication)
        );
    }

    @GetMapping("/api/reservation-seats")
    public ResponseEntity<Page<ReservationSeatResponse>> getCurrentUserReservationSeats(
            Pageable pageable,
            Authentication authentication
    ) {
        return ResponseEntity.ok(
                reservationSeatService.getCurrentUserReservationSeats(pageable, authentication)
        );
    }

    @GetMapping("/api/admin/reservation-seats/{id}")
    public ResponseEntity<ReservationSeatResponse> getReservationSeatById(@PathVariable Long id) {
        return ResponseEntity.ok(reservationSeatService.getReservationSeatById(id));
    }

    @GetMapping("/api/admin/reservation-seats")
    public ResponseEntity<Page<ReservationSeatResponse>> getAllReservationSeats(Pageable pageable) {
        return ResponseEntity.ok(reservationSeatService.getAllReservationSeats(pageable));
    }

    @DeleteMapping("/api/admin/reservation-seats/{id}")
    public ResponseEntity<Void> deleteReservationSeat(@PathVariable Long id) {
        reservationSeatService.deleteReservationSeat(id);
        return ResponseEntity.noContent().build();
    }
}
