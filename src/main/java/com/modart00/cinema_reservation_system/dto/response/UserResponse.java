package com.modart00.cinema_reservation_system.dto.response;

import com.modart00.cinema_reservation_system.entity.Role;
import lombok.Data;

import java.time.LocalDate;

@Data
public class UserResponse {
    private Long id;
    private String username;
    private String email;
    private Role role;
    private LocalDate createdAt;
}
