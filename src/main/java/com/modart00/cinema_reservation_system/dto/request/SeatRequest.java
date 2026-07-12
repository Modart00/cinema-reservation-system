package com.modart00.cinema_reservation_system.dto.request;

import com.modart00.cinema_reservation_system.entity.SeatType;
import lombok.Data;

@Data
public class SeatRequest {
    private int rowNumber;
    private int seatNumber;
    private SeatType seatType;
    private Long hallId;
}
