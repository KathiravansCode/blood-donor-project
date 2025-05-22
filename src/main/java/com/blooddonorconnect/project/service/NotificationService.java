package com.blooddonorconnect.project.service;

import com.blooddonorconnect.project.dto.NotificationDTO;
import com.blooddonorconnect.project.model.Notification;
import com.blooddonorconnect.project.model.User;
import com.blooddonorconnect.project.repository.NotificationRepository;
import com.blooddonorconnect.project.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class NotificationService {

    @Autowired
    private NotificationRepository notificationRepository;

    @Autowired
    private UserRepository userRepository;

    public NotificationDTO createNotification(Long userId, Notification.NotificationType type,
                                              Long relatedId, String message) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));

        Notification notification = new Notification();
        notification.setUser(user);
        notification.setType(type);
        notification.setRelatedId(relatedId);
        notification.setMessage(message);
        notification.setRead(false);

        Notification savedNotification = notificationRepository.save(notification);
        return convertToNotificationDTO(savedNotification);
    }

    public List<NotificationDTO> getUserNotifications(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));

        List<Notification> notifications = notificationRepository.findByUserOrderByCreatedAtDesc(user);

        return notifications.stream()
                .map(this::convertToNotificationDTO)
                .collect(Collectors.toList());
    }

    public List<NotificationDTO> getUnreadNotifications(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));

        List<Notification> notifications = notificationRepository
                .findByUserAndIsReadOrderByCreatedAtDesc(user, false);

        return notifications.stream()
                .map(this::convertToNotificationDTO)
                .collect(Collectors.toList());
    }

    public long getUnreadNotificationCount(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));

        return notificationRepository.countByUserAndIsRead(user, false);
    }

    public NotificationDTO markAsRead(Long notificationId, Long userId) {
        Notification notification = notificationRepository.findById(notificationId)
                .orElseThrow(() -> new RuntimeException("Notification not found"));

        // Verify the notification belongs to the user
        if (!notification.getUser().getId().equals(userId)) {
            throw new RuntimeException("Notification does not belong to the user");
        }

        notification.setRead(true);
        Notification updatedNotification = notificationRepository.save(notification);

        return convertToNotificationDTO(updatedNotification);
    }

    public void markAllAsRead(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));

        List<Notification> unreadNotifications = notificationRepository
                .findByUserAndIsReadOrderByCreatedAtDesc(user, false);

        for (Notification notification : unreadNotifications) {
            notification.setRead(true);
        }

        notificationRepository.saveAll(unreadNotifications);
    }

    public void deleteNotification(Long notificationId, Long userId) {
        Notification notification = notificationRepository.findById(notificationId)
                .orElseThrow(() -> new RuntimeException("Notification not found"));

        // Verify the notification belongs to the user
        if (!notification.getUser().getId().equals(userId)) {
            throw new RuntimeException("Notification does not belong to the user");
        }

        notificationRepository.delete(notification);
    }

    private NotificationDTO convertToNotificationDTO(Notification notification) {
        NotificationDTO dto = new NotificationDTO();
        dto.setId(notification.getId());
        dto.setUserId(notification.getUser().getId());
        dto.setType(notification.getType());
        dto.setRelatedId(notification.getRelatedId());
        dto.setMessage(notification.getMessage());
        dto.setRead(notification.isRead());
        dto.setCreatedAt(notification.getCreatedAt());
        return dto;
    }
}