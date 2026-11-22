package com.travelbook.servicenotification.service;

import com.travelbook.servicenotification.dto.NotificationEmailDto;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class EmailNotificationServiceTest {

    @Mock
    private JavaMailSender mailSender;

    @InjectMocks
    private EmailNotificationService emailNotificationService;

    @Test
    void sendsTextEmailToRequestedRecipient() {
        emailNotificationService.envoyerEmail(new NotificationEmailDto(
                "traveler@example.com", "Réservation confirmée", "Votre paiement est confirmé."));

        ArgumentCaptor<SimpleMailMessage> message =
                ArgumentCaptor.forClass(SimpleMailMessage.class);
        verify(mailSender).send(message.capture());
        assertEquals("noreply@travelbook.com", message.getValue().getFrom());
        assertEquals("traveler@example.com", message.getValue().getTo()[0]);
        assertEquals("Réservation confirmée", message.getValue().getSubject());
        assertEquals("Votre paiement est confirmé.", message.getValue().getText());
    }
}
