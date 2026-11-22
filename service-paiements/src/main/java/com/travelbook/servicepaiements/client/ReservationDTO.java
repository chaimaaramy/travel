package com.travelbook.servicepaiements.client;

import java.math.BigDecimal;

public record ReservationDTO(
        Long id,
        Long userId,
        Long voyageId,
        Integer nombrePersonnes,
        String statut,
        BigDecimal montant
) {
}
