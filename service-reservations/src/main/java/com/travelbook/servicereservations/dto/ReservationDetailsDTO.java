package com.travelbook.servicereservations.dto;

import com.travelbook.servicereservations.client.TrajetDTO;
import com.travelbook.servicereservations.entity.Ticket;

import java.util.List;
import java.util.Map;

public record ReservationDetailsDTO(
        ReservationResponseDTO reservation,
        Map<String, Object> utilisateur,
        TrajetDTO trajet,
        List<Ticket> billets,
        Map<String, Object> paiement
) {}