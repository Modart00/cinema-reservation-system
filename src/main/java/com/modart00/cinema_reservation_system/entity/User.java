package com.modart00.cinema_reservation_system.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import lombok.Data;

import java.time.LocalDate;

@Entity
@Data
public class User {
    @Id
    private Long id;
    private String username;
    private String email;
    private String password;
    private Role role;
    private LocalDate createdAt;
}
