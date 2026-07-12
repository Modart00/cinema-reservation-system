package com.modart00.cinema_reservation_system.controller;

import com.modart00.cinema_reservation_system.dto.request.SeatRequest;
import com.modart00.cinema_reservation_system.dto.response.SeatResponse;
import com.modart00.cinema_reservation_system.service.SeatService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
public class SeatController {
    private final SeatService seatService;

    @GetMapping("/api/seats/{id}")
    public ResponseEntity<SeatResponse> getSeatById(@PathVariable Long id) {
        return ResponseEntity.ok(seatService.getSeatById(id));
    }

    @GetMapping("/api/seats")
    public ResponseEntity<Page<SeatResponse>> getAllSeats(Pageable pageable) {
        return ResponseEntity.ok(seatService.getAllSeats(pageable));
    }

    @PostMapping("/api/admin/seats")
    public ResponseEntity<SeatResponse> createSeat(@Valid @RequestBody SeatRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(seatService.createSeat(request));
    }

    @PutMapping("/api/admin/seats/{id}")
    public ResponseEntity<SeatResponse> updateSeat(
            @PathVariable Long id,
            @Valid @RequestBody SeatRequest request
    ) {
        return ResponseEntity.ok(seatService.updateSeat(id, request));
    }

    @DeleteMapping("/api/admin/seats/{id}")
    public ResponseEntity<Void> deleteSeat(@PathVariable Long id) {
        seatService.deleteSeat(id);
        return ResponseEntity.noContent().build();
    }
}
