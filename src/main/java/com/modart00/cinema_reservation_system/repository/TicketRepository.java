package com.modart00.cinema_reservation_system.repository;

import com.modart00.cinema_reservation_system.entity.Ticket;
import org.springframework.data.jpa.repository.JpaRepository;

public interface TicketRepository extends JpaRepository<Ticket, Long> {
}
