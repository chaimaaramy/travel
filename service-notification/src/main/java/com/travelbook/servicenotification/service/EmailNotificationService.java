package com.travelbook.servicenotification.service;

import com.travelbook.servicenotification.dto.NotificationEmailDto;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

@Service
public class EmailNotificationService {

    private final JavaMailSender mailSender;

    public EmailNotificationService(JavaMailSender mailSender) {
        this.mailSender = mailSender;
    }

    public void envoyerEmail(NotificationEmailDto notification) {
        SimpleMailMessage message = new SimpleMailMessage();
        message.setFrom("noreply@travelbook.com");
        message.setTo(notification.destinataire());
        message.setSubject(notification.objet());
        message.setText(notification.contenu());
        mailSender.send(message);
    }
}
