package com.travelbook.servicenotification.messaging;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.ConstraintViolationException;
import jakarta.validation.Validator;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.dao.DataIntegrityViolationException;

import java.util.Set;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class NotificationKafkaListenerTest {

    @Mock
    private NotificationEventProcessor notificationEventProcessor;

    @Mock
    private Validator validator;

    @InjectMocks
    private NotificationKafkaListener notificationKafkaListener;

    private NotificationEmailRequestedEvent event() {
        return new NotificationEmailRequestedEvent(
                UUID.randomUUID(), 1, 4L, 12L,
                "test@example.com", "Paiement confirmé", "contenu");
    }

    private ConsumerRecord<String, NotificationEmailRequestedEvent> record(
            NotificationEmailRequestedEvent event) {
        return new ConsumerRecord<>(NotificationKafkaListener.TOPIC, 0, 0L,
                String.valueOf(event.reservationId()), event);
    }

    @Test
    void processesValidEvent() {
        NotificationEmailRequestedEvent event = event();
        when(validator.validate(event)).thenReturn(Set.of());

        notificationKafkaListener.recevoir(record(event));

        verify(notificationEventProcessor).traiter(event);
    }

    @Test
    void rejectsInvalidEventWithoutProcessing() {
        NotificationEmailRequestedEvent event = event();
        @SuppressWarnings("unchecked")
        ConstraintViolation<NotificationEmailRequestedEvent> violation =
                org.mockito.Mockito.mock(ConstraintViolation.class);
        when(validator.validate(event)).thenReturn(Set.of(violation));

        assertThrows(ConstraintViolationException.class,
                () -> notificationKafkaListener.recevoir(record(event)));
        verify(notificationEventProcessor, never()).traiter(event);
    }

    @Test
    void ignoresDuplicateEvent() {
        NotificationEmailRequestedEvent event = event();
        when(validator.validate(event)).thenReturn(Set.of());
        org.mockito.Mockito.doThrow(new DataIntegrityViolationException("duplicate"))
                .when(notificationEventProcessor).traiter(event);

        assertDoesNotThrow(() -> notificationKafkaListener.recevoir(record(event)));
        verify(notificationEventProcessor).traiter(event);
    }

    @Test
    void rejectsUnreadableMessage() {
        ConsumerRecord<String, NotificationEmailRequestedEvent> record =
                new ConsumerRecord<>(NotificationKafkaListener.TOPIC, 0, 0L, "12", null);

        assertThrows(IllegalStateException.class,
                () -> notificationKafkaListener.recevoir(record));
    }

    @Test
    void rejectsUnsupportedVersion() {
        NotificationEmailRequestedEvent event = new NotificationEmailRequestedEvent(
                UUID.randomUUID(), 99, 4L, 12L,
                "test@example.com", "Paiement confirmé", "contenu");

        assertThrows(IllegalArgumentException.class,
                () -> notificationKafkaListener.recevoir(record(event)));
        verify(notificationEventProcessor, never()).traiter(event);
    }
}
