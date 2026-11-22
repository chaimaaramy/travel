package com.travelbook.servicenotification.messaging;

import com.travelbook.servicenotification.config.RabbitConfiguration;
import com.travelbook.servicenotification.dto.NotificationEmailDto;
import com.travelbook.servicenotification.service.EmailNotificationService;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.ConstraintViolationException;
import jakarta.validation.Validator;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

import java.util.Set;

@Component
public class NotificationListener {

    private final EmailNotificationService emailNotificationService;
    private final Validator validator;

    public NotificationListener(
            EmailNotificationService emailNotificationService,
            Validator validator) {
        this.emailNotificationService = emailNotificationService;
        this.validator = validator;
    }

    @RabbitListener(queues = RabbitConfiguration.EMAIL_QUEUE)
    public void recevoir(NotificationEmailDto notification) {
        Set<ConstraintViolation<NotificationEmailDto>> violations =
                validator.validate(notification);
        if (!violations.isEmpty()) {
            throw new ConstraintViolationException(violations);
        }
        emailNotificationService.envoyerEmail(notification);
    }
}
