package com.modart00.cinema_reservation_system.dto.response;

import com.modart00.cinema_reservation_system.entity.SeatType;
import lombok.Data;

@Data
public class SeatResponse {
    private Long id;
    private int rowNumber;
    private int seatNumber;
    private SeatType seatType;
    private Long hallId;
}
