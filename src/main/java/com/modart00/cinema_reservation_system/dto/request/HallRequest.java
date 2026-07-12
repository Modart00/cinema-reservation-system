package com.modart00.cinema_reservation_system.dto.request;

import lombok.Data;

@Data
public class HallRequest {
    private String name;
    private int totalRows;
    private int seatsPerRow;
}
