package com.modart00.cinema_reservation_system.controller;

import com.modart00.cinema_reservation_system.dto.request.HallRequest;
import com.modart00.cinema_reservation_system.dto.response.HallResponse;
import com.modart00.cinema_reservation_system.service.HallService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
public class HallController {
    private final HallService hallService;

    @GetMapping("/api/halls/{id}")
    public ResponseEntity<HallResponse> getHallById(@PathVariable Long id) {
        return ResponseEntity.ok(hallService.getHallById(id));
    }

    @GetMapping("/api/halls")
    public ResponseEntity<Page<HallResponse>> getAllHalls(Pageable pageable) {
        return ResponseEntity.ok(hallService.getAllHalls(pageable));
    }

    @PostMapping("/api/admin/halls")
    public ResponseEntity<HallResponse> createHall(@Valid @RequestBody HallRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(hallService.createHall(request));
    }

    @PutMapping("/api/admin/halls/{id}")
    public ResponseEntity<HallResponse> updateHall(
            @PathVariable Long id,
            @Valid @RequestBody HallRequest request
    ) {
        return ResponseEntity.ok(hallService.updateHall(id, request));
    }

    @DeleteMapping("/api/admin/halls/{id}")
    public ResponseEntity<Void> deleteHall(@PathVariable Long id) {
        hallService.deleteHall(id);
        return ResponseEntity.noContent().build();
    }
}
