package com.travelbook.servicepaiements.messaging;

import com.travelbook.servicepaiements.config.RabbitConfiguration;
import com.travelbook.servicepaiements.dto.NotificationEmailDto;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Service;

@Service
public class NotificationPublisher {

    private final RabbitTemplate rabbitTemplate;

    public NotificationPublisher(RabbitTemplate rabbitTemplate) {
        this.rabbitTemplate = rabbitTemplate;
    }

    public void envoyer(NotificationEmailDto notification) {
        rabbitTemplate.convertAndSend(RabbitConfiguration.EMAIL_QUEUE, notification);
    }
}
