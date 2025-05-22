package com.blooddonorconnect.project.controller;

import com.blooddonorconnect.project.dto.NotificationDTO;
import com.blooddonorconnect.project.model.User;
import com.blooddonorconnect.project.repository.UserRepository;
import com.blooddonorconnect.project.service.NotificationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/notifications")
@CrossOrigin(origins = "http://localhost:3000")
public class NotificationController {

    @Autowired
    private NotificationService notificationService;

    @Autowired
    private UserRepository userRepository;

    @GetMapping
    public ResponseEntity<?> getUserNotifications(Authentication authentication) {
        try {
            Long userId = getCurrentUserId(authentication);
            List<NotificationDTO> notifications = notificationService.getUserNotifications(userId);
            return ResponseEntity.ok(createSuccessResponse("Notifications retrieved successfully", notifications));
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                    .body(createErrorResponse("Failed to retrieve notifications", e.getMessage()));
        }
    }

    @GetMapping("/unread")
    public ResponseEntity<?> getUnreadNotifications(Authentication authentication) {
        try {
            Long userId = getCurrentUserId(authentication);
            List<NotificationDTO> notifications = notificationService.getUnreadNotifications(userId);
            return ResponseEntity.ok(createSuccessResponse("Unread notifications retrieved successfully", notifications));
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                    .body(createErrorResponse("Failed to retrieve unread notifications", e.getMessage()));
        }
    }

    @GetMapping("/unread/count")
    public ResponseEntity<?> getUnreadNotificationCount(Authentication authentication) {
        try {
            Long userId = getCurrentUserId(authentication);
            long count = notificationService.getUnreadNotificationCount(userId);

            Map<String, Object> countData = new HashMap<>();
            countData.put("unreadCount", count);

            return ResponseEntity.ok(createSuccessResponse("Unread notification count retrieved successfully", countData));
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                    .body(createErrorResponse("Failed to retrieve unread notification count", e.getMessage()));
        }
    }

    @PutMapping("/{notificationId}/read")
    public ResponseEntity<?> markNotificationAsRead(@PathVariable Long notificationId,
                                                    Authentication authentication) {
        try {
            Long userId = getCurrentUserId(authentication);
            NotificationDTO notification = notificationService.markAsRead(notificationId, userId);
            return ResponseEntity.ok(createSuccessResponse("Notification marked as read", notification));
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                    .body(createErrorResponse("Failed to mark notification as read", e.getMessage()));
        }
    }

    @PutMapping("/read-all")
    public ResponseEntity<?> markAllNotificationsAsRead(Authentication authentication) {
        try {
            Long userId = getCurrentUserId(authentication);
            notificationService.markAllAsRead(userId);
            return ResponseEntity.ok(createSuccessResponse("All notifications marked as read", null));
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                    .body(createErrorResponse("Failed to mark all notifications as read", e.getMessage()));
        }
    }

    @DeleteMapping("/{notificationId}")
    public ResponseEntity<?> deleteNotification(@PathVariable Long notificationId,
                                                Authentication authentication) {
        try {
            Long userId = getCurrentUserId(authentication);
            notificationService.deleteNotification(notificationId, userId);
            return ResponseEntity.ok(createSuccessResponse("Notification deleted successfully", null));
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                    .body(createErrorResponse("Failed to delete notification", e.getMessage()));
        }
    }

    @GetMapping("/summary")
    public ResponseEntity<?> getNotificationSummary(Authentication authentication) {
        try {
            Long userId = getCurrentUserId(authentication);
            List<NotificationDTO> allNotifications = notificationService.getUserNotifications(userId);
            long unreadCount = notificationService.getUnreadNotificationCount(userId);

            Map<String, Object> summary = new HashMap<>();
            summary.put("totalNotifications", allNotifications.size());
            summary.put("unreadNotifications", unreadCount);
            summary.put("readNotifications", allNotifications.size() - unreadCount);

            return ResponseEntity.ok(createSuccessResponse("Notification summary retrieved successfully", summary));
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                    .body(createErrorResponse("Failed to retrieve notification summary", e.getMessage()));
        }
    }

    // Helper methods
    private Long getCurrentUserId(Authentication authentication) {
        String contactNumber = authentication.getName();
        User user = userRepository.findByContactNumber(contactNumber)
                .orElseThrow(() -> new RuntimeException("User not found"));
        return user.getId();
    }

    private Map<String, Object> createSuccessResponse(String message, Object data) {
        Map<String, Object> response = new HashMap<>();
        response.put("success", true);
        response.put("message", message);
        response.put("data", data);
        return response;
    }

    private Map<String, Object> createErrorResponse(String message, String error) {
        Map<String, Object> response = new HashMap<>();
        response.put("success", false);
        response.put("message", message);
        response.put("error", error);
        return response;
    }
}