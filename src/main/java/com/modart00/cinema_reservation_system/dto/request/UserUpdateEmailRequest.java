package com.modart00.cinema_reservation_system.dto.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class UserUpdateEmailRequest {

    @NotBlank(message = "Yeni e-posta adresi boş olamaz")
    @Email(message = "Geçerli bir e-posta adresi girilmelidir")
    @Size(max = 254, message = "E-posta adresi en fazla 254 karakter olabilir")
    private String newEmail;

    @NotBlank(message = "Mevcut şifre boş olamaz")
    @Size(min = 8, max = 100, message = "Şifre 8 ile 100 karakter arasında olmalıdır")
    private String currentPassword;
}
