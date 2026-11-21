
package com.travelbook.servicenotification.service;

import com.travelbook.servicenotification.dto.NotificationRequestDTO;
import com.travelbook.servicenotification.dto.NotificationResponseDTO;
import com.travelbook.servicenotification.entity.Notification;
import com.travelbook.servicenotification.repository.NotificationRepository;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class NotificationService {

    private final NotificationRepository notificationRepository;

    public NotificationService(
            NotificationRepository notificationRepository) {
        this.notificationRepository = notificationRepository;
    }

    public List<NotificationResponseDTO> getAllNotifications() {
        return notificationRepository.findAll()
                .stream()
                .map(this::toDTO)
                .toList();
    }

    public NotificationResponseDTO getNotificationById(Long id) {
        return toDTO(findNotification(id));
    }

    public List<NotificationResponseDTO> getNotificationsByUserId(
            Long userId) {

        return notificationRepository.findByUserId(userId)
                .stream()
                .map(this::toDTO)
                .toList();
    }

    public NotificationResponseDTO createNotification(
            NotificationRequestDTO dto) {

        Notification notification = new Notification();

        notification.setUserId(dto.userId());
        notification.setTitre(dto.titre());
        notification.setMessage(dto.message());
        notification.setType(dto.type());
        notification.setLu(false);
        notification.setDateCreation(LocalDateTime.now());

        return toDTO(notificationRepository.save(notification));
    }

    public NotificationResponseDTO updateNotification(
            Long id, NotificationRequestDTO dto) {

        Notification notification = findNotification(id);

        notification.setUserId(dto.userId());
        notification.setTitre(dto.titre());
        notification.setMessage(dto.message());
        notification.setType(dto.type());
        notification.setLu(dto.lu());

        return toDTO(notificationRepository.save(notification));
    }

    public void deleteNotification(Long id) {
        Notification notification = findNotification(id);
        notificationRepository.delete(notification);
    }

    private Notification findNotification(Long id) {
        return notificationRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "Notification introuvable : " + id
                ));
    }

    private NotificationResponseDTO toDTO(Notification notification) {
        return new NotificationResponseDTO(
                notification.getId(),
                notification.getUserId(),
                notification.getTitre(),
                notification.getMessage(),
                notification.getType(),
                notification.getLu(),
                notification.getDateCreation()
        );
    }
}
