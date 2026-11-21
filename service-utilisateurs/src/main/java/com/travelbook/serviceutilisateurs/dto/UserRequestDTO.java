package com.travelbook.serviceutilisateurs.dto;

import jakarta.persistence.Entity;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import org.aspectj.weaver.ast.Not;

public record UserRequestDTO(
        @NotBlank(message="le nom est obligatoire")
        String nom,
        @NotBlank(message="L'email est obligatoire")
        @Email(message="Email invalide")
String email,
        @NotBlank(message = "le role est obligatoire")
        String role,
        @NotNull(message = "le statut actif est obligatoire")
        Boolean actif
) {
}
