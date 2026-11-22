package com.travelbook.servicepaiements.client;

public record UtilisateurDTO(
        Long id,
        String nom,
        String email,
        String role,
        Boolean actif
) {
}
