package com.modart00.cinema_reservation_system.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import lombok.Data;

@Entity
@Data
public class Hall {
    @Id
    private Long id;
    private String name;
    private int totalRows;
    private int seatsPerRow;
}
