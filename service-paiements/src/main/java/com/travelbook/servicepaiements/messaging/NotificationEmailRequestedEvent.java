package com.travelbook.servicepaiements.messaging;

import java.util.UUID;

public record NotificationEmailRequestedEvent(
        UUID eventId,
        int eventVersion,
        Long paiementId,
        Long reservationId,
        String destinataire,
        String objet,
        String contenu
) {
    public static final int CURRENT_VERSION = 1;
}
