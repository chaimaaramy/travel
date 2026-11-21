
package com.travelbook.servicepaiements.dto;

import com.travelbook.servicepaiements.entity.ModePaiement;
import com.travelbook.servicepaiements.entity.StatutPaiement;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record PaiementResponseDTO(
        Long id,
        Long reservationId,
        BigDecimal montant,
        ModePaiement modePaiement,
        StatutPaiement statut,
        LocalDateTime datePaiement
) {
}
