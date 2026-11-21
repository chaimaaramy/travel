package com.travelbook.serviceutilisateurs.dto;

public record UserResponseDTO(
        Long id,
        String nom,
        String email,
        String role,
        Boolean actif
) {
}
