package com.travelbook.servicetrajets.dto;

import com.travelbook.servicetrajets.entity.MoyenTransport;

import jakarta.validation.constraints.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;

public record TrajetRequestDTO(

        @NotNull
        @Positive
        Long voyageId,

        @NotBlank
        String villeDepart,

        @NotBlank
        String villeArrivee,

        @NotNull
        LocalDateTime dateHeureDepart,

        @NotNull
        LocalDateTime dateHeureArrivee,

        @NotNull
        MoyenTransport moyenTransport,

        @NotNull
        @DecimalMin(value = "0.0", inclusive = true)
        BigDecimal prix,

        @NotNull
        @PositiveOrZero
        Integer placesRestantes

) {
    @AssertTrue(message = "La date d'arrivee doit etre apres le depart")
    public boolean isDatesValides() {
        return dateHeureDepart == null
                || dateHeureArrivee == null
                || dateHeureArrivee.isAfter(dateHeureDepart);
    }
}
