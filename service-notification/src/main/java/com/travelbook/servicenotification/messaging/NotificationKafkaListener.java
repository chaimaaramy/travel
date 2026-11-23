package com.travelbook.servicenotification.messaging;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.ConstraintViolationException;
import jakarta.validation.Validator;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

import java.util.Set;

@Component
public class NotificationKafkaListener {

    private static final Logger log =
            LoggerFactory.getLogger(NotificationKafkaListener.class);

    public static final String TOPIC = "notification.email.requested";
    public static final String GROUP_ID = "notification-service-group";

    private final NotificationEventProcessor notificationEventProcessor;
    private final Validator validator;

    public NotificationKafkaListener(
            NotificationEventProcessor notificationEventProcessor,
            Validator validator) {
        this.notificationEventProcessor = notificationEventProcessor;
        this.validator = validator;
    }

    @KafkaListener(topics = TOPIC, groupId = GROUP_ID,
            containerFactory = "kafkaListenerContainerFactory")
    public void recevoir(ConsumerRecord<String, NotificationEmailRequestedEvent> record) {
        NotificationEmailRequestedEvent event = record.value();
        if (event == null) {
            throw new IllegalStateException(
                    "Message Kafka illisible sur le topic " + TOPIC);
        }
        if (event.eventVersion() != NotificationEmailRequestedEvent.CURRENT_VERSION) {
            throw new IllegalArgumentException(
                    "Version d'événement non supportée : " + event.eventVersion());
        }

        Set<ConstraintViolation<NotificationEmailRequestedEvent>> violations =
                validator.validate(event);
        if (!violations.isEmpty()) {
            throw new ConstraintViolationException(violations);
        }

        try {
            notificationEventProcessor.traiter(event);
        } catch (DataIntegrityViolationException e) {
            log.info("Événement {} déjà traité, ignoré", event.eventId());
        }
    }
}
