
package com.travelbook.servicenotification.controller;

import com.travelbook.servicenotification.dto.NotificationRequestDTO;
import com.travelbook.servicenotification.dto.NotificationResponseDTO;
import com.travelbook.servicenotification.service.NotificationService;

import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/notifications")
public class NotificationController {

    private final NotificationService notificationService;

    public NotificationController(NotificationService notificationService) {
        this.notificationService = notificationService;
    }

    @GetMapping
    public List<NotificationResponseDTO> getAllNotifications() {
        return notificationService.getAllNotifications();
    }

    @GetMapping("/{id}")
    public NotificationResponseDTO getNotificationById(
            @PathVariable Long id) {
        return notificationService.getNotificationById(id);
    }

    @GetMapping("/user/{userId}")
    public List<NotificationResponseDTO> getNotificationsByUserId(
            @PathVariable Long userId) {
        return notificationService.getNotificationsByUserId(userId);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public NotificationResponseDTO createNotification(
            @Valid @RequestBody NotificationRequestDTO dto) {
        return notificationService.createNotification(dto);
    }

    @PutMapping("/{id}")
    public ResponseEntity<NotificationResponseDTO> updateNotification(
            @PathVariable Long id,
            @Valid @RequestBody NotificationRequestDTO dto) {
        return ResponseEntity.ok(
                notificationService.updateNotification(id, dto)
        );
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteNotification(
            @PathVariable Long id) {
        notificationService.deleteNotification(id);
        return ResponseEntity.noContent().build();
    }
}
