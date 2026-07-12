package com.modart00.cinema_reservation_system.service;

import com.modart00.cinema_reservation_system.dto.request.ReservationRequest;
import com.modart00.cinema_reservation_system.dto.response.ReservationResponse;
import com.modart00.cinema_reservation_system.entity.*;
import com.modart00.cinema_reservation_system.exception.ConflictException;
import com.modart00.cinema_reservation_system.exception.InvalidRequestException;
import com.modart00.cinema_reservation_system.exception.ResourceNotFoundException;
import com.modart00.cinema_reservation_system.repository.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class ReservationService {
    private final ReservationRepository reservationRepository;
    private final ReservationSeatRepository reservationSeatRepository;
    private final ScreeningRepository screeningRepository;
    private final SeatRepository seatRepository;
    private final UserRepository userRepository;

    @Transactional
    public ReservationResponse createReservation(ReservationRequest request, Authentication authentication) {
        User user = getAuthenticatedUser(authentication);
        log.info("User {} attempting to create reservation for screening ID: {}",
                user.getUsername(), request.getScreeningId());

        Screening screening = screeningRepository.findById(request.getScreeningId())
                .orElseThrow(() -> new ResourceNotFoundException("Seans bulunamadı"));
        if (request.getSeatIds() == null || request.getSeatIds().isEmpty()) {
            throw new InvalidRequestException("En az bir koltuk seçilmelidir");
        }

        List<Seat> seats = new ArrayList<>();
        for (Long seatId : request.getSeatIds()) {
            if (seatId == null) {
                throw new InvalidRequestException("Koltuk kimliği boş olamaz");
            }
            Seat seat = seatRepository.findById(seatId)
                    .orElseThrow(() -> new ResourceNotFoundException("Seçilen koltuk bulunamadı"));
            boolean alreadySelected = false;
            for (Seat selectedSeat : seats) {
                if (selectedSeat.getId().equals(seat.getId())) {
                    alreadySelected = true;
                    break;
                }
            }
            if (alreadySelected) {
                throw new InvalidRequestException("Aynı koltuk birden fazla kez seçilemez");
            }
            seats.add(seat);
        }

        for (Seat seat : seats) {
            if (!seat.getHall().getId().equals(screening.getHall().getId())) {
                throw new InvalidRequestException("Seçilen koltuk seansın salonuna ait değil");
            }
            if (reservationSeatRepository.existsByScreeningIdAndSeatId(screening.getId(), seat.getId())) {
                throw new ConflictException("Seçilen koltuklardan biri daha önce rezerve edilmiş");
            }
        }

        Reservation reservation = new Reservation();
        reservation.setReservationCode(UUID.randomUUID().toString());
        reservation.setStatus(ReservationStatus.PENDING);
        reservation.setCreatedAt(LocalDateTime.now());
        reservation.setExpiresAt(LocalDateTime.now().plusMinutes(10));
        reservation.setTotalPrice(screening.getPrice().multiply(BigDecimal.valueOf(seats.size())));
        reservation.setUser(user);
        reservation.setScreening(screening);
        Reservation savedReservation = reservationRepository.save(reservation);

        List<ReservationSeat> reservationSeats = new ArrayList<>();
        for (Seat seat : seats) {
            ReservationSeat reservationSeat = new ReservationSeat();
            reservationSeat.setPrice(screening.getPrice());
            reservationSeat.setReservation(savedReservation);
            reservationSeat.setSeat(seat);
            reservationSeat.setScreening(screening);
            reservationSeats.add(reservationSeat);
        }
        reservationSeatRepository.saveAll(reservationSeats);

        log.info("User {} created reservation with ID: {}", user.getUsername(), savedReservation.getId());
        return toResponse(savedReservation);
    }

    @Transactional(readOnly = true)
    public ReservationResponse getCurrentUserReservation(Long id, Authentication authentication) {
        User user = getAuthenticatedUser(authentication);
        log.info("User {} requested reservation with ID: {}", user.getUsername(), id);
        Reservation reservation = reservationRepository.findByIdAndUserId(id, user.getId())
                .orElseThrow(() -> new ResourceNotFoundException("Rezervasyon bulunamadı"));
        return toResponse(reservation);
    }

    @Transactional(readOnly = true)
    public Page<ReservationResponse> getCurrentUserReservations(Pageable pageable, Authentication authentication) {
        User user = getAuthenticatedUser(authentication);
        log.info("User {} requested own reservations", user.getUsername());
        return reservationRepository.findAllByUserId(user.getId(), pageable).map(this::toResponse);
    }

    @Transactional(readOnly = true)
    public ReservationResponse getReservationById(Long id) {
        log.info("Admin requested reservation with ID: {}", id);
        return toResponse(findReservation(id));
    }

    @Transactional(readOnly = true)
    public Page<ReservationResponse> getAllReservations(Pageable pageable) {
        log.info("Admin requested all reservations");
        return reservationRepository.findAll(pageable).map(this::toResponse);
    }

    @Transactional
    public void deleteReservation(Long id) {
        log.info("Admin attempting to delete reservation with ID: {}", id);
        Reservation reservation = findReservation(id);
        reservationSeatRepository.deleteAllByReservationId(id);
        reservationRepository.delete(reservation);
        log.info("Admin deleted reservation with ID: {}", id);
    }

    private Reservation findReservation(Long id) {
        return reservationRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Rezervasyon bulunamadı"));
    }

    private User getAuthenticatedUser(Authentication authentication) {
        if (authentication == null || !(authentication.getPrincipal() instanceof User principal)) {
            throw new InvalidRequestException("Geçerli bir kullanıcı oturumu bulunamadı");
        }
        return userRepository.findById(principal.getId())
                .orElseThrow(() -> new ResourceNotFoundException("Kullanıcı bulunamadı"));
    }

    private ReservationResponse toResponse(Reservation reservation) {
        ReservationResponse response = new ReservationResponse();
        response.setId(reservation.getId());
        response.setReservationCode(reservation.getReservationCode());
        response.setStatus(reservation.getStatus());
        response.setCreatedAt(reservation.getCreatedAt());
        response.setExpiresAt(reservation.getExpiresAt());
        response.setTotalPrice(reservation.getTotalPrice());
        response.setUserId(reservation.getUser().getId());
        response.setScreeningId(reservation.getScreening().getId());
        return response;
    }
}
