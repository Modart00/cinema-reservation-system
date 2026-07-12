package com.modart00.cinema_reservation_system.controller;

import com.modart00.cinema_reservation_system.dto.request.*;
import com.modart00.cinema_reservation_system.dto.response.*;
import com.modart00.cinema_reservation_system.service.*;
import org.junit.jupiter.api.Test;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class ControllerDelegationTest {

    @Test
    void authController_registerShouldDelegate() {
        AuthService service = mock(AuthService.class);
        RegisterRequest request = new RegisterRequest();
        RegisterResponse expected = new RegisterResponse();
        when(service.register(request)).thenReturn(expected);

        var response = new AuthController(service).register(request);

        assertEquals(HttpStatus.CREATED, response.getStatusCode());
        assertSame(expected, response.getBody());
    }

    @Test
    void userController_getCurrentUserShouldDelegate() {
        UserService service = mock(UserService.class);
        Authentication authentication = mock(Authentication.class);
        UserResponse expected = mock(UserResponse.class);
        when(service.getCurrentUser(authentication)).thenReturn(expected);

        var response = new UserController(service).getCurrentUser(authentication);

        assertSame(expected, response.getBody());
    }

    @Test
    void hallController_getAllShouldDelegate() {
        HallService service = mock(HallService.class);
        Pageable pageable = mock(Pageable.class);
        Page<HallResponse> expected = new PageImpl<>(List.of());
        when(service.getAllHalls(pageable)).thenReturn(expected);
        assertSame(expected, new HallController(service).getAllHalls(pageable).getBody());
    }

    @Test
    void movieController_createShouldDelegate() {
        MovieService service = mock(MovieService.class);
        MovieRequest request = new MovieRequest();
        MovieResponse expected = new MovieResponse();
        when(service.createMovie(request)).thenReturn(expected);
        var response = new MovieController(service).createMovie(request);
        assertEquals(HttpStatus.CREATED, response.getStatusCode());
        assertSame(expected, response.getBody());
    }

    @Test
    void seatController_deleteShouldDelegate() {
        SeatService service = mock(SeatService.class);
        var response = new SeatController(service).deleteSeat(1L);
        assertEquals(HttpStatus.NO_CONTENT, response.getStatusCode());
        verify(service).deleteSeat(1L);
    }

    @Test
    void screeningController_getByIdShouldDelegate() {
        ScreeningService service = mock(ScreeningService.class);
        ScreeningResponse expected = new ScreeningResponse();
        when(service.getScreeningById(1L)).thenReturn(expected);
        assertSame(expected, new ScreeningController(service).getScreeningById(1L).getBody());
    }

    @Test
    void reservationController_createShouldDelegate() {
        ReservationService service = mock(ReservationService.class);
        ReservationRequest request = new ReservationRequest();
        Authentication authentication = mock(Authentication.class);
        ReservationResponse expected = new ReservationResponse();
        when(service.createReservation(request, authentication)).thenReturn(expected);
        var response = new ReservationController(service).createReservation(request, authentication);
        assertEquals(HttpStatus.CREATED, response.getStatusCode());
        assertSame(expected, response.getBody());
    }

    @Test
    void reservationSeatController_getByIdShouldDelegate() {
        ReservationSeatService service = mock(ReservationSeatService.class);
        Authentication authentication = mock(Authentication.class);
        ReservationSeatResponse expected = new ReservationSeatResponse();
        when(service.getCurrentUserReservationSeat(1L, authentication)).thenReturn(expected);
        assertSame(expected, new ReservationSeatController(service)
                .getCurrentUserReservationSeat(1L, authentication).getBody());
    }

    @Test
    void ticketController_getByIdShouldDelegate() {
        TicketService service = mock(TicketService.class);
        Authentication authentication = mock(Authentication.class);
        TicketResponse expected = new TicketResponse();
        when(service.getCurrentUserTicket(1L, authentication)).thenReturn(expected);
        assertSame(expected, new TicketController(service).getCurrentUserTicket(1L, authentication).getBody());
    }

    @Test
    void paymentController_makePaymentShouldDelegate() {
        PaymentService service = mock(PaymentService.class);
        Authentication authentication = mock(Authentication.class);
        PaymentResponse expected = mock(PaymentResponse.class);
        when(service.makePayment(1L, authentication)).thenReturn(expected);
        assertSame(expected, new PaymentController(service).makePayment(1L, authentication).getBody());
    }
}
