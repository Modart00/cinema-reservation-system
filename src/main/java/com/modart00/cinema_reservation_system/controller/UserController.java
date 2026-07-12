package com.modart00.cinema_reservation_system.controller;

import com.modart00.cinema_reservation_system.dto.request.UserUpdateEmailRequest;
import com.modart00.cinema_reservation_system.dto.request.UserUpdatePasswordRequest;
import com.modart00.cinema_reservation_system.dto.request.UserUpdateUsernameRequest;
import com.modart00.cinema_reservation_system.dto.response.UpdateEmailResponse;
import com.modart00.cinema_reservation_system.dto.response.UserResponse;
import com.modart00.cinema_reservation_system.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
public class UserController {
    private final UserService userService;

    @GetMapping("/api/users/me")
    public ResponseEntity<UserResponse> getCurrentUser(Authentication authentication) {
        return ResponseEntity.ok(userService.getCurrentUser(authentication));
    }

    @PatchMapping("/api/users/me/username")
    public ResponseEntity<Void> updateUsername(
            @Valid @RequestBody UserUpdateUsernameRequest request,
            Authentication authentication
    ) {
        userService.updateUserName(request, authentication);
        return ResponseEntity.noContent().build();
    }

    @PatchMapping("/api/users/me/email")
    public ResponseEntity<UpdateEmailResponse> updateEmail(
            @Valid @RequestBody UserUpdateEmailRequest request,
            Authentication authentication
    ) {
        return ResponseEntity.ok(userService.updateEmail(request, authentication));
    }

    @PatchMapping("/api/users/me/password")
    public ResponseEntity<Void> updatePassword(
            @Valid @RequestBody UserUpdatePasswordRequest request,
            Authentication authentication
    ) {
        userService.updatePassword(request, authentication);
        return ResponseEntity.noContent().build();
    }

    @DeleteMapping("/api/users/me")
    public ResponseEntity<Void> deleteCurrentUser(Authentication authentication) {
        userService.deleteCurrentUser(authentication);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/api/admin/users/{id}")
    public ResponseEntity<UserResponse> getUserById(@PathVariable Long id) {
        return ResponseEntity.ok(userService.getUserById(id));
    }

    @GetMapping("/api/admin/users")
    public ResponseEntity<Page<UserResponse>> getAllUsers(Pageable pageable) {
        return ResponseEntity.ok(userService.getAllUsers(pageable));
    }

    @DeleteMapping("/api/admin/users/{id}")
    public ResponseEntity<Void> deleteUser(@PathVariable Long id) {
        userService.deleteAnyUserById(id);
        return ResponseEntity.noContent().build();
    }
}
