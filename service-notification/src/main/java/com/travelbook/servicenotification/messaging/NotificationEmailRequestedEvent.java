package com.travelbook.servicenotification.messaging;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.util.UUID;

public record NotificationEmailRequestedEvent(
        @NotNull UUID eventId,
        int eventVersion,
        @NotNull Long paiementId,
        @NotNull Long reservationId,
        @NotBlank @Email String destinataire,
        @NotBlank String objet,
        @NotBlank String contenu
) {
    public static final int CURRENT_VERSION = 1;
}
