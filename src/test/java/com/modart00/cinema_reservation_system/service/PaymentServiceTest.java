package com.modart00.cinema_reservation_system.service;

import com.modart00.cinema_reservation_system.dto.response.PaymentResponse;
import com.modart00.cinema_reservation_system.entity.*;
import com.modart00.cinema_reservation_system.exception.ConflictException;
import com.modart00.cinema_reservation_system.repository.PaymentRepository;
import com.modart00.cinema_reservation_system.repository.ReservationRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.Authentication;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PaymentServiceTest {
    @Mock PaymentRepository paymentRepository;
    @Mock ReservationRepository reservationRepository;
    @Mock TicketService ticketService;
    @Mock Authentication authentication;

    private PaymentService paymentService;
    private User user;
    private Reservation reservation;

    @BeforeEach
    void setUp() {
        paymentService = new PaymentService(paymentRepository, reservationRepository, ticketService);
        user = new User();
        user.setId(1L);
        user.setUsername("ali");
        reservation = new Reservation();
        reservation.setId(2L);
        reservation.setUser(user);
        reservation.setStatus(ReservationStatus.PENDING);
        reservation.setTotalPrice(BigDecimal.valueOf(300));
        reservation.setExpiresAt(LocalDateTime.now().plusMinutes(5));
    }

    @Test
    void makePayment_shouldMarkReservationPaidAndCreateTicket() {
        when(authentication.getPrincipal()).thenReturn(user);
        when(reservationRepository.findByIdAndUser(2L, user)).thenReturn(Optional.of(reservation));
        when(paymentRepository.save(any(Payment.class))).thenAnswer(invocation -> {
            Payment payment = invocation.getArgument(0);
            payment.setId(3L);
            return payment;
        });

        PaymentResponse response = paymentService.makePayment(2L, authentication);

        assertEquals(PaymentStatus.SUCCESS, response.getStatus());
        assertEquals(ReservationStatus.PAID, reservation.getStatus());
        verify(ticketService).createTicket(2L);
    }

    @Test
    void makePayment_whenAlreadyPaid_shouldThrow() {
        reservation.setStatus(ReservationStatus.PAID);
        when(authentication.getPrincipal()).thenReturn(user);
        when(reservationRepository.findByIdAndUser(2L, user)).thenReturn(Optional.of(reservation));

        assertThrows(ConflictException.class, () -> paymentService.makePayment(2L, authentication));
        verify(paymentRepository, never()).save(any());
    }
}
