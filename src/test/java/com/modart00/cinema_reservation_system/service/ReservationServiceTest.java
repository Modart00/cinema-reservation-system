package com.modart00.cinema_reservation_system.service;

import com.modart00.cinema_reservation_system.dto.request.ReservationRequest;
import com.modart00.cinema_reservation_system.dto.response.ReservationResponse;
import com.modart00.cinema_reservation_system.entity.*;
import com.modart00.cinema_reservation_system.exception.ConflictException;
import com.modart00.cinema_reservation_system.repository.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.Authentication;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ReservationServiceTest {
    @Mock ReservationRepository reservationRepository;
    @Mock ReservationSeatRepository reservationSeatRepository;
    @Mock ScreeningRepository screeningRepository;
    @Mock SeatRepository seatRepository;
    @Mock UserRepository userRepository;
    @Mock Authentication authentication;

    private ReservationService reservationService;
    private User user;
    private Screening screening;
    private Seat seat;

    @BeforeEach
    void setUp() {
        reservationService = new ReservationService(
                reservationRepository,
                reservationSeatRepository,
                screeningRepository,
                seatRepository,
                userRepository
        );
        user = new User();
        user.setId(1L);
        user.setUsername("ali");
        Hall hall = new Hall();
        hall.setId(2L);
        screening = new Screening();
        screening.setId(3L);
        screening.setHall(hall);
        screening.setPrice(BigDecimal.valueOf(100));
        seat = new Seat();
        seat.setId(4L);
        seat.setHall(hall);
    }

    @Test
    void createReservation_shouldCreateReservationAndSeats() {
        ReservationRequest request = new ReservationRequest();
        request.setScreeningId(3L);
        request.setSeatIds(List.of(4L));
        when(authentication.getPrincipal()).thenReturn(user);
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(screeningRepository.findById(3L)).thenReturn(Optional.of(screening));
        when(seatRepository.findById(4L)).thenReturn(Optional.of(seat));
        when(reservationSeatRepository.existsByScreeningIdAndSeatId(3L, 4L)).thenReturn(false);
        when(reservationRepository.save(any(Reservation.class))).thenAnswer(invocation -> {
            Reservation reservation = invocation.getArgument(0);
            reservation.setId(5L);
            return reservation;
        });

        ReservationResponse response = reservationService.createReservation(request, authentication);

        assertEquals(5L, response.getId());
        assertEquals(ReservationStatus.PENDING, response.getStatus());
        assertEquals(BigDecimal.valueOf(100), response.getTotalPrice());
        verify(reservationSeatRepository).saveAll(anyList());
    }

    @Test
    void createReservation_whenSeatTaken_shouldThrow() {
        ReservationRequest request = new ReservationRequest();
        request.setScreeningId(3L);
        request.setSeatIds(List.of(4L));
        when(authentication.getPrincipal()).thenReturn(user);
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(screeningRepository.findById(3L)).thenReturn(Optional.of(screening));
        when(seatRepository.findById(4L)).thenReturn(Optional.of(seat));
        when(reservationSeatRepository.existsByScreeningIdAndSeatId(3L, 4L)).thenReturn(true);

        assertThrows(ConflictException.class,
                () -> reservationService.createReservation(request, authentication));
        verify(reservationRepository, never()).save(any());
    }
}
