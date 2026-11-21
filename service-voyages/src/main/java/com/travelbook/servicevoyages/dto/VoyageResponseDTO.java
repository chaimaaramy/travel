
package com.travelbook.servicevoyages.dto;

import java.math.BigDecimal;
import java.time.LocalDate;

public record VoyageResponseDTO(
        Long id,
        String destination,
        String description,
        LocalDate dateDepart,
        LocalDate dateRetour,
        BigDecimal prix
) {
}
