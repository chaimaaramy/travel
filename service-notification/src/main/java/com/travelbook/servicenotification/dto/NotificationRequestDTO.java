package com.travelbook.servicenotification.dto;


import com.travelbook.servicenotification.entity.TypeNotification;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;

public record NotificationRequestDTO(

        @NotNull
        @Positive
        Long userId,

        @NotBlank
        String titre,

        @NotBlank
        @Size(max = 2000)
        String message,

        @NotNull
        TypeNotification type,

        @NotNull
        Boolean lu

) {
}

