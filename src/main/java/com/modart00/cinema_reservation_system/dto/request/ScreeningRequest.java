package com.modart00.cinema_reservation_system.dto.request;

import com.modart00.cinema_reservation_system.entity.ScreeningStatus;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
public class ScreeningRequest {
    private LocalDateTime startTime;
    private LocalDateTime endTime;
    private BigDecimal price;
    private ScreeningStatus status;
    private Long movieId;
    private Long hallId;
}
