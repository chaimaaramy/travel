
package com.travelbook.servicereservations.dto;

import com.travelbook.servicereservations.entity.StatutReservation;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record ReservationResponseDTO(
        Long id,
        Long userId,
        Long voyageId,
        Integer nombrePersonnes,
        LocalDateTime dateReservation,
        StatutReservation statut,
        BigDecimal montant
) {}
