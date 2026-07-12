package com.modart00.cinema_reservation_system.service;

import com.modart00.cinema_reservation_system.dto.response.ReservationSeatResponse;
import com.modart00.cinema_reservation_system.entity.ReservationSeat;
import com.modart00.cinema_reservation_system.entity.User;
import com.modart00.cinema_reservation_system.exception.InvalidRequestException;
import com.modart00.cinema_reservation_system.exception.ResourceNotFoundException;
import com.modart00.cinema_reservation_system.repository.ReservationSeatRepository;
import com.modart00.cinema_reservation_system.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class ReservationSeatService {
    private final ReservationSeatRepository reservationSeatRepository;
    private final UserRepository userRepository;

    @Transactional(readOnly = true)
    public ReservationSeatResponse getCurrentUserReservationSeat(Long id, Authentication authentication) {
        User user = getAuthenticatedUser(authentication);
        log.info("User {} requested reservation seat with ID: {}", user.getUsername(), id);
        ReservationSeat reservationSeat = reservationSeatRepository
                .findByIdAndReservationUserId(id, user.getId())
                .orElseThrow(() -> new ResourceNotFoundException("Rezervasyon koltuğu bulunamadı"));
        return toResponse(reservationSeat);
    }

    @Transactional(readOnly = true)
    public Page<ReservationSeatResponse> getCurrentUserReservationSeats(
            Pageable pageable,
            Authentication authentication
    ) {
        User user = getAuthenticatedUser(authentication);
        log.info("User {} requested own reservation seats", user.getUsername());
        return reservationSeatRepository.findAllByReservationUserId(user.getId(), pageable)
                .map(this::toResponse);
    }

    @Transactional(readOnly = true)
    public ReservationSeatResponse getReservationSeatById(Long id) {
        log.info("Admin requested reservation seat with ID: {}", id);
        return toResponse(findReservationSeat(id));
    }

    @Transactional(readOnly = true)
    public Page<ReservationSeatResponse> getAllReservationSeats(Pageable pageable) {
        log.info("Admin requested all reservation seats");
        return reservationSeatRepository.findAll(pageable).map(this::toResponse);
    }

    @Transactional
    public void deleteReservationSeat(Long id) {
        log.info("Admin attempting to delete reservation seat with ID: {}", id);
        reservationSeatRepository.delete(findReservationSeat(id));
        log.info("Admin deleted reservation seat with ID: {}", id);
    }

    private ReservationSeat findReservationSeat(Long id) {
        return reservationSeatRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Rezervasyon koltuğu bulunamadı"));
    }

    private User getAuthenticatedUser(Authentication authentication) {
        if (authentication == null || !(authentication.getPrincipal() instanceof User principal)) {
            throw new InvalidRequestException("Geçerli bir kullanıcı oturumu bulunamadı");
        }
        return userRepository.findById(principal.getId())
                .orElseThrow(() -> new ResourceNotFoundException("Kullanıcı bulunamadı"));
    }

    private ReservationSeatResponse toResponse(ReservationSeat reservationSeat) {
        ReservationSeatResponse response = new ReservationSeatResponse();
        response.setId(reservationSeat.getId());
        response.setPrice(reservationSeat.getPrice());
        response.setReservationId(reservationSeat.getReservation().getId());
        response.setSeatId(reservationSeat.getSeat().getId());
        response.setScreeningId(reservationSeat.getScreening().getId());
        return response;
    }
}
