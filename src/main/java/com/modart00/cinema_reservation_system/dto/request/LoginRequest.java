package com.modart00.cinema_reservation_system.dto.request;

import lombok.Data;

@Data
public class LoginRequest {
    private String email;
    private String password;
}
