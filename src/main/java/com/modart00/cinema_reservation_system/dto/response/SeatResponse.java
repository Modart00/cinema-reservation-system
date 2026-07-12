package com.modart00.cinema_reservation_system.dto.response;

import com.modart00.cinema_reservation_system.entity.SeatType;
import lombok.Data;
import java.io.Serializable;

@Data
public class SeatResponse implements Serializable {
    private Long id;
    private int rowNumber;
    private int seatNumber;
    private SeatType seatType;
    private Long hallId;
}
