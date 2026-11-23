package com.travelbook.servicepaiements.messaging;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

@Service
public class NotificationPublisher {

    private static final Logger log = LoggerFactory.getLogger(NotificationPublisher.class);

    public static final String TOPIC = "notification.email.requested";

    private final KafkaTemplate<String, NotificationEmailRequestedEvent> kafkaTemplate;

    public NotificationPublisher(
            KafkaTemplate<String, NotificationEmailRequestedEvent> kafkaTemplate) {
        this.kafkaTemplate = kafkaTemplate;
    }

    public void envoyer(NotificationEmailRequestedEvent event) {
        kafkaTemplate
                .send(TOPIC, String.valueOf(event.reservationId()), event)
                .whenComplete((result, ex) -> {
                    if (ex != null) {
                        log.error("Publication Kafka impossible pour le paiement {} "
                                + "(réservation {})", event.paiementId(),
                                event.reservationId(), ex);
                    }
                });
    }
}
