package com.modart00.cinema_reservation_system.dto.response;

import lombok.Data;

@Data
public class HallResponse {
    private Long id;
    private String name;
    private int totalRows;
    private int seatsPerRow;
}
