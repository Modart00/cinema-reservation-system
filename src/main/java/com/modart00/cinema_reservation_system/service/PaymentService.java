package com.modart00.cinema_reservation_system.service;

import com.modart00.cinema_reservation_system.dto.response.PaymentResponse;
import com.modart00.cinema_reservation_system.entity.*;
import com.modart00.cinema_reservation_system.exception.ConflictException;
import com.modart00.cinema_reservation_system.exception.InvalidRequestException;
import com.modart00.cinema_reservation_system.exception.ResourceNotFoundException;
import com.modart00.cinema_reservation_system.repository.PaymentRepository;
import com.modart00.cinema_reservation_system.repository.ReservationRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class PaymentService {
    private final PaymentRepository paymentRepository;
    private final ReservationRepository reservationRepository;
    private final TicketService ticketService;

    public PaymentResponse toResponse(Payment payment){
        return new PaymentResponse(payment.getId(),
                payment.getPaymentCode(),
                payment.getAmount(),
                payment.getStatus(),
                payment.getPaymentDate(),
                payment.getReservation().getId());
    }

    @Transactional
    public PaymentResponse makePayment(Long reservationId, Authentication authentication){
        log.info("User {} Payment attempt",((User) authentication.getPrincipal()).getUsername());
        User user = (User) authentication.getPrincipal();
        Reservation reservation = reservationRepository.findByIdAndUser(reservationId,user).orElse(null);

        if (reservation == null){
            log.warn("Reservation not found");
            throw new ResourceNotFoundException("Rezervasyon bulunamadı");
        }

        if (reservation.getStatus() == ReservationStatus.PAID) {
            log.warn("Payment attempt to already paid reservation");
            throw new ConflictException(
                    "Bu rezervasyonun ödemesi zaten yapılmış"
            );
        }

        if (reservation.getStatus() == ReservationStatus.CANCELLED) {
            log.warn("Payment Attempt to cancelled reservation");
            throw new ConflictException(
                    "İptal edilmiş rezervasyon için ödeme yapılamaz"
            );
        }

        if (reservation.getStatus() == ReservationStatus.EXPIRED) {
            log.warn("Payment attempt to expired reservation");
            throw new ConflictException(
                    "Rezervasyon süresi dolduğu için ödeme yapılamaz"
            );
        }

        if (reservation.getExpiresAt().isBefore(LocalDateTime.now())){
            log.warn("Reservation is expired");
            throw new InvalidRequestException("Rezervasyonunuzun süresi sona ermiştir.");
        }

        Payment payment = new Payment();
        payment.setAmount(reservation.getTotalPrice());
        payment.setPaymentCode(UUID.randomUUID().toString());
        payment.setPaymentDate(LocalDateTime.now());
        payment.setReservation(reservation);
        payment.setStatus(PaymentStatus.SUCCESS);

        reservation.setStatus(ReservationStatus.PAID);

        reservationRepository.save(reservation);
        Payment savedPayment = paymentRepository.save(payment);

        ticketService.createTicket(reservationId);

        log.info(
                "Payment {} completed successfully for reservation {} by user {}",
                savedPayment.getPaymentCode(),
                reservationId,
                user.getId()
        );

        return toResponse(savedPayment);



    }


}
