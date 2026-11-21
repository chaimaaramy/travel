
package com.travelbook.servicereservations.dto;

import com.travelbook.servicereservations.entity.StatutReservation;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

public record ReservationRequestDTO(

        @NotNull
        @Positive
        Long userId,

        @NotNull
        @Positive
        Long voyageId,

        @NotNull
        @Positive
        Integer nombrePersonnes,

        @NotNull
        StatutReservation statut

) {
}
