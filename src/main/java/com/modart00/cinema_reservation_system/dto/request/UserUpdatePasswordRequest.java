package com.modart00.cinema_reservation_system.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class UserUpdatePasswordRequest {
    @NotBlank(message = "Mevcut şifre boş olamaz")
    @Size(min = 8, max = 100, message = "Mevcut şifre 8 ile 100 karakter arasında olmalıdır")
    private String currentPassword;

    @NotBlank(message = "Yeni şifre boş olamaz")
    @Size(min = 8, max = 100, message = "Yeni şifre 8 ile 100 karakter arasında olmalıdır")
    private String newPassword;
}
