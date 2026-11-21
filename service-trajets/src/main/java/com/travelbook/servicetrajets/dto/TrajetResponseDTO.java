package com.travelbook.servicetrajets.dto;



import com.travelbook.servicetrajets.entity.MoyenTransport;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record TrajetResponseDTO(
        Long id,
        Long voyageId,
        String villeDepart,
        String villeArrivee,
        LocalDateTime dateHeureDepart,
        LocalDateTime dateHeureArrivee,
        MoyenTransport moyenTransport,
        BigDecimal prix
) {
}
