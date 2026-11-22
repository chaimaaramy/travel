package com.travelbook.servicereservations.repository;

import com.travelbook.servicereservations.entity.Ticket;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface TicketRepository extends JpaRepository<Ticket, Long> {
    List<Ticket> findByReservationId(Long reservationId);
}