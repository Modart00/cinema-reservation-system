package com.modart00.cinema_reservation_system.dto.response;

import com.modart00.cinema_reservation_system.entity.ScreeningStatus;
import lombok.Data;

import java.time.LocalDate;

@Data
public class ScreeningResponse {
    private Long id;
    private LocalDate startTime;
    private LocalDate endTime;
    private double price;
    private ScreeningStatus status;
    private Long movieId;
    private Long hallId;
}
