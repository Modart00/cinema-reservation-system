package com.modart00.cinema_reservation_system.service;

import com.modart00.cinema_reservation_system.dto.response.TicketResponse;
import com.modart00.cinema_reservation_system.entity.Reservation;
import com.modart00.cinema_reservation_system.entity.Ticket;
import com.modart00.cinema_reservation_system.entity.User;
import com.modart00.cinema_reservation_system.exception.ConflictException;
import com.modart00.cinema_reservation_system.exception.InvalidRequestException;
import com.modart00.cinema_reservation_system.exception.ResourceNotFoundException;
import com.modart00.cinema_reservation_system.repository.ReservationRepository;
import com.modart00.cinema_reservation_system.repository.TicketRepository;
import com.modart00.cinema_reservation_system.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class TicketService {
    private final TicketRepository ticketRepository;
    private final ReservationRepository reservationRepository;
    private final UserRepository userRepository;

    @Transactional
    public TicketResponse createTicket(Long reservationId) {
        log.info("Admin/system attempting to create ticket for reservation ID: {}", reservationId);
        Reservation reservation = reservationRepository.findById(reservationId)
                .orElseThrow(() -> new ResourceNotFoundException("Rezervasyon bulunamadı"));
        if (ticketRepository.existsByReservationId(reservationId)) {
            throw new ConflictException("Bu rezervasyon için bilet zaten oluşturulmuş");
        }

        Ticket ticket = new Ticket();
        ticket.setTicketCode(UUID.randomUUID().toString());
        ticket.setCreatedAt(LocalDateTime.now());
        ticket.setReservation(reservation);
        Ticket savedTicket = ticketRepository.save(ticket);
        log.info("Admin/system created ticket with ID: {}", savedTicket.getId());
        return toResponse(savedTicket);
    }

    @Transactional(readOnly = true)
    public TicketResponse getCurrentUserTicket(Long id, Authentication authentication) {
        User user = getAuthenticatedUser(authentication);
        log.info("User {} requested ticket with ID: {}", user.getUsername(), id);
        Ticket ticket = ticketRepository.findByIdAndReservationUserId(id, user.getId())
                .orElseThrow(() -> new ResourceNotFoundException("Bilet bulunamadı"));
        return toResponse(ticket);
    }

    @Transactional(readOnly = true)
    public Page<TicketResponse> getCurrentUserTickets(Pageable pageable, Authentication authentication) {
        User user = getAuthenticatedUser(authentication);
        log.info("User {} requested own tickets", user.getUsername());
        return ticketRepository.findAllByReservationUserId(user.getId(), pageable).map(this::toResponse);
    }

    @Transactional(readOnly = true)
    public TicketResponse getTicketById(Long id) {
        log.info("Admin requested ticket with ID: {}", id);
        return toResponse(findTicket(id));
    }

    @Transactional(readOnly = true)
    public Page<TicketResponse> getAllTickets(Pageable pageable) {
        log.info("Admin requested all tickets");
        return ticketRepository.findAll(pageable).map(this::toResponse);
    }

    @Transactional
    public void deleteTicket(Long id) {
        log.info("Admin attempting to delete ticket with ID: {}", id);
        ticketRepository.delete(findTicket(id));
        log.info("Admin deleted ticket with ID: {}", id);
    }

    private Ticket findTicket(Long id) {
        return ticketRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Bilet bulunamadı"));
    }

    private User getAuthenticatedUser(Authentication authentication) {
        if (authentication == null || !(authentication.getPrincipal() instanceof User principal)) {
            throw new InvalidRequestException("Geçerli bir kullanıcı oturumu bulunamadı");
        }
        return userRepository.findById(principal.getId())
                .orElseThrow(() -> new ResourceNotFoundException("Kullanıcı bulunamadı"));
    }

    private TicketResponse toResponse(Ticket ticket) {
        TicketResponse response = new TicketResponse();
        response.setId(ticket.getId());
        response.setTicketCode(ticket.getTicketCode());
        response.setCreatedAt(ticket.getCreatedAt());
        response.setReservationId(ticket.getReservation().getId());
        return response;
    }
}
