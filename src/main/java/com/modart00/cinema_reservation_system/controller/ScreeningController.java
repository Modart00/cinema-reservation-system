package com.modart00.cinema_reservation_system.controller;

import com.modart00.cinema_reservation_system.dto.request.ScreeningRequest;
import com.modart00.cinema_reservation_system.dto.response.ScreeningResponse;
import com.modart00.cinema_reservation_system.service.ScreeningService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
public class ScreeningController {
    private final ScreeningService screeningService;

    @GetMapping("/api/screenings/{id}")
    public ResponseEntity<ScreeningResponse> getScreeningById(@PathVariable Long id) {
        return ResponseEntity.ok(screeningService.getScreeningById(id));
    }

    @GetMapping("/api/screenings")
    public ResponseEntity<Page<ScreeningResponse>> getAllScreenings(Pageable pageable) {
        return ResponseEntity.ok(screeningService.getAllScreenings(pageable));
    }

    @PostMapping("/api/admin/screenings")
    public ResponseEntity<ScreeningResponse> createScreening(@Valid @RequestBody ScreeningRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(screeningService.createScreening(request));
    }

    @PutMapping("/api/admin/screenings/{id}")
    public ResponseEntity<ScreeningResponse> updateScreening(
            @PathVariable Long id,
            @Valid @RequestBody ScreeningRequest request
    ) {
        return ResponseEntity.ok(screeningService.updateScreening(id, request));
    }

    @DeleteMapping("/api/admin/screenings/{id}")
    public ResponseEntity<Void> deleteScreening(@PathVariable Long id) {
        screeningService.deleteScreening(id);
        return ResponseEntity.noContent().build();
    }
}
