package com.travelbook.servicenotification.messaging;

import com.travelbook.servicenotification.dto.NotificationEmailDto;
import com.travelbook.servicenotification.entity.ProcessedEvent;
import com.travelbook.servicenotification.repository.ProcessedEventRepository;
import com.travelbook.servicenotification.service.EmailNotificationService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
public class NotificationEventProcessor {

    private final ProcessedEventRepository processedEventRepository;
    private final EmailNotificationService emailNotificationService;

    public NotificationEventProcessor(
            ProcessedEventRepository processedEventRepository,
            EmailNotificationService emailNotificationService) {
        this.processedEventRepository = processedEventRepository;
        this.emailNotificationService = emailNotificationService;
    }

    @Transactional
    public void traiter(NotificationEmailRequestedEvent event) {
        processedEventRepository.saveAndFlush(new ProcessedEvent(
                event.eventId().toString(), LocalDateTime.now()));
        emailNotificationService.envoyerEmail(new NotificationEmailDto(
                event.destinataire(), event.objet(), event.contenu()));
    }
}
