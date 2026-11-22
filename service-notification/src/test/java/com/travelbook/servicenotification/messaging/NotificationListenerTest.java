package com.travelbook.servicenotification.messaging;

import com.travelbook.servicenotification.dto.NotificationEmailDto;
import com.travelbook.servicenotification.service.EmailNotificationService;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.ConstraintViolationException;
import jakarta.validation.Validator;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class NotificationListenerTest {

    @Mock
    private EmailNotificationService emailNotificationService;

    @Mock
    private Validator validator;

    @InjectMocks
    private NotificationListener notificationListener;

    @Test
    void validatesAndSendsReceivedEmail() {
        NotificationEmailDto notification = new NotificationEmailDto(
                "traveler@example.com", "Paiement confirmé", "Votre réservation est confirmée.");
        when(validator.validate(notification)).thenReturn(Set.of());

        notificationListener.recevoir(notification);

        verify(emailNotificationService).envoyerEmail(notification);
    }

    @Test
    void rejectsInvalidRabbitMessageWithoutSendingEmail() {
        NotificationEmailDto notification = new NotificationEmailDto(
                "adresse-invalide", "", "contenu");
        @SuppressWarnings("unchecked")
        ConstraintViolation<NotificationEmailDto> violation =
                org.mockito.Mockito.mock(ConstraintViolation.class);
        when(validator.validate(notification)).thenReturn(Set.of(violation));

        assertThrows(ConstraintViolationException.class,
                () -> notificationListener.recevoir(notification));
        verify(emailNotificationService, never()).envoyerEmail(notification);
    }
}
