package com.modart00.cinema_reservation_system.dto.response;

import com.modart00.cinema_reservation_system.entity.ScreeningStatus;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.io.Serializable;

@Data
public class ScreeningResponse implements Serializable {
    private Long id;
    private LocalDateTime startTime;
    private LocalDateTime endTime;
    private BigDecimal price;
    private ScreeningStatus status;
    private Long movieId;
    private Long hallId;
}
