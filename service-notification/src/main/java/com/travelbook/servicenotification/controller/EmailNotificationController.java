package com.travelbook.servicenotification.controller;

import com.travelbook.servicenotification.dto.NotificationEmailDto;
import com.travelbook.servicenotification.service.EmailNotificationService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/notifications")
public class EmailNotificationController {

    private final EmailNotificationService emailNotificationService;

    public EmailNotificationController(EmailNotificationService emailNotificationService) {
        this.emailNotificationService = emailNotificationService;
    }

    @PostMapping("/email")
    public ResponseEntity<Void> envoyer(
            @Valid @RequestBody NotificationEmailDto notification) {
        emailNotificationService.envoyerEmail(notification);
        return ResponseEntity.noContent().build();
    }
}
