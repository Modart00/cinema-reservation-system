package com.modart00.cinema_reservation_system.repository;

import com.modart00.cinema_reservation_system.entity.Payment;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PaymentRepository extends JpaRepository<Payment, Long> {
}
