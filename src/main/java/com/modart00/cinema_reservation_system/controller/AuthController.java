package com.modart00.cinema_reservation_system.controller;

import com.modart00.cinema_reservation_system.dto.request.LoginRequest;
import com.modart00.cinema_reservation_system.dto.request.RegisterRequest;
import com.modart00.cinema_reservation_system.dto.response.LoginResponse;
import com.modart00.cinema_reservation_system.dto.response.RegisterResponse;
import com.modart00.cinema_reservation_system.service.AuthService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {
    private final AuthService authService;

    @PostMapping("/register")
    public ResponseEntity<RegisterResponse> register(@Valid @RequestBody RegisterRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(authService.register(request));
    }

    @PostMapping("/login")
    public ResponseEntity<LoginResponse> login(@Valid @RequestBody LoginRequest request) {
        return ResponseEntity.ok(authService.login(request));
    }

    @PostMapping("/refresh")
    public ResponseEntity<LoginResponse> refreshToken(@RequestParam String refreshToken) {
        return ResponseEntity.ok(authService.refreshToken(refreshToken));
    }

    @GetMapping("/verify")
    public ResponseEntity<String> verifyAccount(@RequestParam String token) {
        authService.verifyAccount(token);
        return ResponseEntity.ok("Hesap başarıyla doğrulandı");
    }
}
