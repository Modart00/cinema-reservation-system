package com.modart00.cinema_reservation_system.service;

import com.modart00.cinema_reservation_system.dto.response.ReservationSeatResponse;
import com.modart00.cinema_reservation_system.dto.response.TicketResponse;
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
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class TicketAndReservationSeatServiceTest {
    @Mock TicketRepository ticketRepository;
    @Mock ReservationRepository reservationRepository;
    @Mock ReservationSeatRepository reservationSeatRepository;
    @Mock UserRepository userRepository;
    @Mock Authentication authentication;

    private TicketService ticketService;
    private ReservationSeatService reservationSeatService;

    @BeforeEach
    void setUp() {
        ticketService = new TicketService(ticketRepository, reservationRepository, userRepository);
        reservationSeatService = new ReservationSeatService(reservationSeatRepository, userRepository);
    }

    @Test
    void createTicket_shouldSaveGeneratedTicket() {
        Reservation reservation = new Reservation();
        reservation.setId(1L);
        when(reservationRepository.findById(1L)).thenReturn(Optional.of(reservation));
        when(ticketRepository.existsByReservationId(1L)).thenReturn(false);
        when(ticketRepository.save(any(Ticket.class))).thenAnswer(invocation -> {
            Ticket ticket = invocation.getArgument(0);
            ticket.setId(2L);
            return ticket;
        });

        TicketResponse response = ticketService.createTicket(1L);

        assertEquals(2L, response.getId());
        assertNotNull(response.getTicketCode());
    }

    @Test
    void createTicket_whenTicketExists_shouldThrow() {
        Reservation reservation = new Reservation();
        reservation.setId(1L);
        when(reservationRepository.findById(1L)).thenReturn(Optional.of(reservation));
        when(ticketRepository.existsByReservationId(1L)).thenReturn(true);

        assertThrows(ConflictException.class, () -> ticketService.createTicket(1L));
    }

    @Test
    void getCurrentUserReservationSeat_shouldReturnOwnedSeat() {
        User user = new User();
        user.setId(3L);
        user.setUsername("ali");
        Reservation reservation = new Reservation();
        reservation.setId(4L);
        Seat seat = new Seat();
        seat.setId(5L);
        Screening screening = new Screening();
        screening.setId(6L);
        ReservationSeat reservationSeat = new ReservationSeat();
        reservationSeat.setId(7L);
        reservationSeat.setPrice(BigDecimal.TEN);
        reservationSeat.setReservation(reservation);
        reservationSeat.setSeat(seat);
        reservationSeat.setScreening(screening);
        when(authentication.getPrincipal()).thenReturn(user);
        when(userRepository.findById(3L)).thenReturn(Optional.of(user));
        when(reservationSeatRepository.findByIdAndReservationUserId(7L, 3L))
                .thenReturn(Optional.of(reservationSeat));

        ReservationSeatResponse response = reservationSeatService
                .getCurrentUserReservationSeat(7L, authentication);

        assertEquals(4L, response.getReservationId());
        assertEquals(5L, response.getSeatId());
    }
}
