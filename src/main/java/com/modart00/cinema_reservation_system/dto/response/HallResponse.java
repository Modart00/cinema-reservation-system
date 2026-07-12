package com.modart00.cinema_reservation_system.dto.response;

import lombok.Data;
import java.io.Serializable;

@Data
public class HallResponse implements Serializable {
    private Long id;
    private String name;
    private int totalRows;
    private int seatsPerRow;
}
