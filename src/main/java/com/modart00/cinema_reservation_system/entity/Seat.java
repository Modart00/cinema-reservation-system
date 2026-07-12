package com.modart00.cinema_reservation_system.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(
        name = "seat",
        uniqueConstraints = @UniqueConstraint(
                name = "uk_hall_row_seat",
                columnNames = {"hall_id", "row_index", "seat_number"}
        )
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Seat {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "row_index", nullable = false)
    private int rowNumber;

    @Column(name = "seat_number", nullable = false)
    private int seatNumber;

    @Enumerated(EnumType.STRING)
    @Column(name = "seat_type", nullable = false)
    private SeatType seatType;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "hall_id", nullable = false)
    private Hall hall;
}
