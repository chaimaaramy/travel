
package com.travelbook.servicepaiements.dto;

import com.travelbook.servicepaiements.entity.ModePaiement;
import com.travelbook.servicepaiements.entity.StatutPaiement;

import jakarta.validation.constraints.*;
import java.math.BigDecimal;

public record PaiementRequestDTO(

        @NotNull
        @Positive
        Long reservationId,

        @NotNull
        @DecimalMin(value = "0.0", inclusive = false)
        BigDecimal montant,

        @NotNull
        ModePaiement modePaiement,

        @NotNull
        StatutPaiement statut

) {
}
