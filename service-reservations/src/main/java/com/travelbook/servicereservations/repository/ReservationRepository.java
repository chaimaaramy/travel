
package com.travelbook.servicereservations.repository;

import com.travelbook.servicereservations.entity.Reservation;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ReservationRepository
        extends JpaRepository<Reservation, Long> {
}
