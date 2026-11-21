package com.travelbook.serviceavis.dto;

import jakarta.validation.constraints.*;

public record AvisRequestDTO(

        @NotNull
        @Positive
        Long userId,

        @NotNull
        @Positive
        Long voyageId,

        @NotNull
        @Min(1)
        @Max(5)
        Integer note,

        @NotBlank
        @Size(max = 2000)
        String commentaire

) {
}
