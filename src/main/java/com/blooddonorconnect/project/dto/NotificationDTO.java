package com.blooddonorconnect.project.dto;

import com.blooddonorconnect.project.model.Notification;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class NotificationDTO {
    private Long id;
    private Long userId;
    private Notification.NotificationType type;
    private Long relatedId;
    private String message;
    private boolean isRead;
    private LocalDateTime createdAt;
}