package com.helpdesk.backend.controller;

import com.helpdesk.backend.model.NotificationEntity;
import com.helpdesk.backend.repository.NotificationRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/notifications")
@RequiredArgsConstructor
public class NotificationController {

    private final NotificationRepository notificationRepository;

    @GetMapping("/{userId}")
    public List<NotificationResponse> getUserNotifications(@PathVariable Long userId) {
        return notificationRepository.findByUserIdOrderByCreatedAtDesc(userId)
                .stream()
                .map(this::toResponse)
                .toList();
    }

    @GetMapping("/{userId}/unread-count")
    public Map<String, Long> getUnreadCount(@PathVariable Long userId) {
        return Map.of("count", notificationRepository.countByUserIdAndIsReadFalse(userId));
    }

    @PutMapping("/{id}/read")
    public void markAsRead(@PathVariable Long id) {
        notificationRepository.findById(id).ifPresent(notification -> {
            notification.setIsRead(true);
            notificationRepository.save(notification);
        });
    }

    private NotificationResponse toResponse(NotificationEntity notification) {
        return new NotificationResponse(
            notification.getId(),
            notification.getTitle(),
            notification.getMessage(),
            notification.getIsRead(),
            notification.getRelatedTicketId(),
            notification.getCreatedAt()
        );
    }

    record NotificationResponse(
        Long id,
        String title,
        String message,
        Boolean isRead,
        Long relatedTicketId,
        java.time.LocalDateTime createdAt
    ) {}
}
