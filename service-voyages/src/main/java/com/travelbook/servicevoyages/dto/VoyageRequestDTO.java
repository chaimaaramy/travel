package com.travelbook.servicevoyages.dto;

import jakarta.validation.constraints.AssertTrue;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;
import java.time.LocalDate;

public record VoyageRequestDTO(
        @NotBlank(message = "la destination est obligatoire")
        String destination,
        @NotBlank(message = "la description est obligatoire")
        String description,
        @NotBlank(message = "la date de départ est obligatoire")
        LocalDate dateDepart,
        @NotBlank(message = "la date de retour est obligatoire")
        LocalDate dateRetour,
        @NotNull(message = "le prix est obligatoire")
        @DecimalMin(value = "0.0",inclusive = false)
        BigDecimal prix


) {
    @AssertTrue(message = "la date de retour doit etre apres la date de depart")
    public boolean isDatesValides(){
        return dateDepart==null
                || dateRetour==null
                || dateRetour.isAfter(dateDepart);

    }
}
