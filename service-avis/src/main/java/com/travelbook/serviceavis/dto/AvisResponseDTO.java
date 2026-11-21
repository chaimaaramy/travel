package com.travelbook.serviceavis.dto;

import java.time.LocalDateTime;

public record AvisResponseDTO(
        Long id,
        Long userId,
        Long voyageId,
        Integer note,
        String commentaire,
        LocalDateTime dateCreation
) {
}
