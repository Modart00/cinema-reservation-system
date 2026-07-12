package com.modart00.cinema_reservation_system.service;

import com.modart00.cinema_reservation_system.dto.request.ScreeningRequest;
import com.modart00.cinema_reservation_system.dto.response.ScreeningResponse;
import com.modart00.cinema_reservation_system.entity.Hall;
import com.modart00.cinema_reservation_system.entity.Movie;
import com.modart00.cinema_reservation_system.entity.Screening;
import com.modart00.cinema_reservation_system.entity.Reservation;
import com.modart00.cinema_reservation_system.exception.ResourceNotFoundException;
import com.modart00.cinema_reservation_system.repository.HallRepository;
import com.modart00.cinema_reservation_system.repository.MovieRepository;
import com.modart00.cinema_reservation_system.repository.ScreeningRepository;
import com.modart00.cinema_reservation_system.repository.ReservationRepository;
import com.modart00.cinema_reservation_system.repository.ReservationSeatRepository;
import com.modart00.cinema_reservation_system.repository.PaymentRepository;
import com.modart00.cinema_reservation_system.repository.TicketRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.cache.annotation.Caching;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class ScreeningService {
    private final ScreeningRepository screeningRepository;
    private final MovieRepository movieRepository;
    private final HallRepository hallRepository;
    private final ReservationRepository reservationRepository;
    private final ReservationSeatRepository reservationSeatRepository;
    private final PaymentRepository paymentRepository;
    private final TicketRepository ticketRepository;

    @Transactional
    @CacheEvict(value = "screeningLists", allEntries = true)
    public ScreeningResponse createScreening(ScreeningRequest request) {
        log.info("Admin attempting to create screening for movie ID: {}", request.getMovieId());
        Movie movie = movieRepository.findById(request.getMovieId())
                .orElseThrow(() -> new ResourceNotFoundException("Film bulunamadı"));
        Hall hall = hallRepository.findById(request.getHallId())
                .orElseThrow(() -> new ResourceNotFoundException("Salon bulunamadı"));
        Screening screening = new Screening();
        screening.setStartTime(request.getStartTime());
        screening.setEndTime(request.getEndTime());
        screening.setPrice(request.getPrice());
        screening.setStatus(request.getStatus());
        screening.setMovie(movie);
        screening.setHall(hall);
        Screening savedScreening = screeningRepository.save(screening);
        log.info("Admin created screening with ID: {}", savedScreening.getId());
        return toResponse(savedScreening);
    }

    @Cacheable(value = "screenings", key = "#id")
    public ScreeningResponse getScreeningById(Long id) {
        log.info("User requested screening with ID: {}", id);
        Screening screening = screeningRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Seans bulunamadı"));
        return toResponse(screening);
    }

    @Cacheable(value = "screeningLists", key = "#pageable.pageNumber + '-' + #pageable.pageSize + '-' + #pageable.sort.toString()")
    public Page<ScreeningResponse> getAllScreenings(Pageable pageable) {
        log.info("User requested screening list");
        return screeningRepository.findAll(pageable).map(this::toResponse);
    }

    @Transactional(readOnly = true)
    public List<Long> getOccupiedSeatIds(Long screeningId) {
        screeningRepository.findById(screeningId)
                .orElseThrow(() -> new ResourceNotFoundException("Seans bulunamadÄ±"));
        return reservationSeatRepository.findAllByScreeningId(screeningId)
                .stream()
                .map(reservationSeat -> reservationSeat.getSeat().getId())
                .toList();
    }

    @Transactional
    @Caching(evict = {
            @CacheEvict(value = "screenings", key = "#id"),
            @CacheEvict(value = "screeningLists", allEntries = true)
    })
    public ScreeningResponse updateScreening(Long id, ScreeningRequest request) {
        log.info("Admin attempting to update screening with ID: {}", id);
        Screening screening = screeningRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Seans bulunamadı"));
        Movie movie = movieRepository.findById(request.getMovieId())
                .orElseThrow(() -> new ResourceNotFoundException("Film bulunamadı"));
        Hall hall = hallRepository.findById(request.getHallId())
                .orElseThrow(() -> new ResourceNotFoundException("Salon bulunamadı"));
        screening.setStartTime(request.getStartTime());
        screening.setEndTime(request.getEndTime());
        screening.setPrice(request.getPrice());
        screening.setStatus(request.getStatus());
        screening.setMovie(movie);
        screening.setHall(hall);
        log.info("Admin updated screening with ID: {}", id);
        return toResponse(screeningRepository.save(screening));
    }

    @Transactional
    @Caching(evict = {
            @CacheEvict(value = "screenings", key = "#id"),
            @CacheEvict(value = "screeningLists", allEntries = true)
    })
    public void deleteScreening(Long id) {
        log.info("Admin attempting to delete screening with ID: {}", id);
        Screening screening = screeningRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Seans bulunamadı"));

        for (Reservation reservation : reservationRepository.findAllByScreeningId(id)) {
            paymentRepository.deleteByReservationId(reservation.getId());
            ticketRepository.deleteByReservationId(reservation.getId());
            reservationSeatRepository.deleteAllByReservationId(reservation.getId());
            reservationRepository.delete(reservation);
        }
        reservationSeatRepository.deleteAllByScreeningId(id);
        screeningRepository.delete(screening);
        log.info("Admin deleted screening with ID: {}", id);
    }

    private ScreeningResponse toResponse(Screening screening) {
        ScreeningResponse response = new ScreeningResponse();
        response.setId(screening.getId());
        response.setStartTime(screening.getStartTime());
        response.setEndTime(screening.getEndTime());
        response.setPrice(screening.getPrice());
        response.setStatus(screening.getStatus());
        response.setMovieId(screening.getMovie().getId());
        response.setHallId(screening.getHall().getId());
        return response;
    }
}
