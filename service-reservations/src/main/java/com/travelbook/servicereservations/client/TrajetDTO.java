package com.travelbook.servicereservations.client;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record TrajetDTO(
        Long id,
        Long voyageId,
        String villeDepart,
        String villeArrivee,
        LocalDateTime dateHeureDepart,
        LocalDateTime dateHeureArrivee,
        String moyenTransport,
        BigDecimal prix,
        Integer placesRestantes
) {}