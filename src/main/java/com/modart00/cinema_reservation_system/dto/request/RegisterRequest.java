package com.modart00.cinema_reservation_system.dto.request;

import lombok.Data;

@Data
public class RegisterRequest {
    private String username;
    private String email;
    private String password;
}
