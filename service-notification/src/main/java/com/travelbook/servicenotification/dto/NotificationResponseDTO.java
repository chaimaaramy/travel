package com.travelbook.servicenotification.dto;



import com.travelbook.servicenotification.entity.TypeNotification;

import java.time.LocalDateTime;

public record NotificationResponseDTO(
        Long id,
        Long userId,
        String titre,
        String message,
        TypeNotification type,
        Boolean lu,
        LocalDateTime dateCreation
) {
}

