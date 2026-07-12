package com.modart00.cinema_reservation_system.service;

import com.modart00.cinema_reservation_system.dto.request.*;
import com.modart00.cinema_reservation_system.dto.response.*;
import com.modart00.cinema_reservation_system.entity.*;
import com.modart00.cinema_reservation_system.exception.ResourceNotFoundException;
import com.modart00.cinema_reservation_system.repository.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Optional;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CatalogServicesTest {
    @Mock HallRepository hallRepository;
    @Mock MovieRepository movieRepository;
    @Mock SeatRepository seatRepository;
    @Mock ScreeningRepository screeningRepository;
    @Mock ReservationRepository reservationRepository;
    @Mock ReservationSeatRepository reservationSeatRepository;
    @Mock PaymentRepository paymentRepository;
    @Mock TicketRepository ticketRepository;

    private HallService hallService;
    private MovieService movieService;
    private SeatService seatService;
    private ScreeningService screeningService;

    @BeforeEach
    void setUp() {
        hallService = new HallService(hallRepository, seatRepository);
        screeningService = new ScreeningService(
                screeningRepository,
                movieRepository,
                hallRepository,
                reservationRepository,
                reservationSeatRepository,
                paymentRepository,
                ticketRepository
        );
        movieService = new MovieService(movieRepository, screeningRepository, screeningService);
        seatService = new SeatService(seatRepository, hallRepository);
    }

    @Test
    void createHall_shouldSaveAndReturnResponse() {
        HallRequest request = new HallRequest();
        request.setName("Salon 1");
        request.setTotalRows(10);
        request.setSeatsPerRow(12);
        when(hallRepository.save(any(Hall.class))).thenAnswer(invocation -> {
            Hall hall = invocation.getArgument(0);
            hall.setId(1L);
            return hall;
        });

        HallResponse response = hallService.createHall(request);

        assertEquals(1L, response.getId());
        assertEquals("Salon 1", response.getName());
        assertEquals(10, response.getTotalRows());
        assertEquals(10, response.getSeatsPerRow());
        verify(hallRepository).save(any(Hall.class));
        verify(seatRepository).saveAll(argThat(seats -> {
            int count = 0;
            for (Seat ignored : seats) count++;
            return count == 100;
        }));
    }

    @Test
    void getHallById_whenMissing_shouldThrow() {
        when(hallRepository.findById(99L)).thenReturn(Optional.empty());
        assertThrows(ResourceNotFoundException.class, () -> hallService.getHallById(99L));
    }

    @Test
    void createMovie_shouldMapAllFields() {
        MovieRequest request = new MovieRequest();
        request.setTitle("Film");
        request.setDescription("Açıklama");
        request.setDurationMinutes(120);
        request.setGenre("Drama");
        request.setAgeRestriction(13);
        request.setReleaseDate(LocalDate.of(2026, 7, 12));
        request.setPosterUrl("poster.jpg");
        request.setStatus(MovieStatus.ACTIVE);
        when(movieRepository.save(any(Movie.class))).thenAnswer(invocation -> {
            Movie movie = invocation.getArgument(0);
            movie.setId(2L);
            return movie;
        });

        MovieResponse response = movieService.createMovie(request);

        assertEquals(2L, response.getId());
        assertEquals("Film", response.getTitle());
        assertEquals(MovieStatus.ACTIVE, response.getStatus());
    }

    @Test
    void createSeat_shouldConnectHall() {
        Hall hall = new Hall();
        hall.setId(3L);
        SeatRequest request = new SeatRequest();
        request.setHallId(3L);
        request.setRowNumber(2);
        request.setSeatNumber(5);
        request.setSeatType(SeatType.VIP);
        when(hallRepository.findById(3L)).thenReturn(Optional.of(hall));
        when(seatRepository.save(any(Seat.class))).thenAnswer(invocation -> {
            Seat seat = invocation.getArgument(0);
            seat.setId(4L);
            return seat;
        });

        SeatResponse response = seatService.createSeat(request);

        assertEquals(4L, response.getId());
        assertEquals(3L, response.getHallId());
        assertEquals(SeatType.VIP, response.getSeatType());
    }

    @Test
    void createScreening_shouldConnectMovieAndHall() {
        Movie movie = new Movie();
        movie.setId(5L);
        Hall hall = new Hall();
        hall.setId(6L);
        ScreeningRequest request = new ScreeningRequest();
        request.setMovieId(5L);
        request.setHallId(6L);
        request.setStartTime(LocalDateTime.of(2026, 7, 12, 18, 30));
        request.setEndTime(LocalDateTime.of(2026, 7, 12, 20, 30));
        request.setPrice(BigDecimal.valueOf(250));
        request.setStatus(ScreeningStatus.ACTIVE);
        when(movieRepository.findById(5L)).thenReturn(Optional.of(movie));
        when(hallRepository.findById(6L)).thenReturn(Optional.of(hall));
        when(screeningRepository.save(any(Screening.class))).thenAnswer(invocation -> {
            Screening screening = invocation.getArgument(0);
            screening.setId(7L);
            return screening;
        });

        ScreeningResponse response = screeningService.createScreening(request);

        assertEquals(7L, response.getId());
        assertEquals(5L, response.getMovieId());
        assertEquals(6L, response.getHallId());
    }

    @Test
    void deleteScreening_shouldDeleteDependentRecordsInOrder() {
        Screening screening = new Screening();
        screening.setId(8L);
        Reservation reservation = new Reservation();
        reservation.setId(9L);
        when(screeningRepository.findById(8L)).thenReturn(Optional.of(screening));
        when(reservationRepository.findAllByScreeningId(8L)).thenReturn(List.of(reservation));

        screeningService.deleteScreening(8L);

        verify(paymentRepository).deleteByReservationId(9L);
        verify(ticketRepository).deleteByReservationId(9L);
        verify(reservationSeatRepository).deleteAllByReservationId(9L);
        verify(reservationRepository).delete(reservation);
        verify(screeningRepository).delete(screening);
    }
}
