package com.helpdesk.backend.controller;

import java.util.List;
import java.util.Map;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import com.helpdesk.backend.model.NotificationEntity;
import com.helpdesk.backend.model.Role;
import com.helpdesk.backend.repository.NotificationRepository;
import com.helpdesk.backend.security.RequestAuthorizer;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/notifications")
@RequiredArgsConstructor
public class NotificationController {

    private final NotificationRepository notificationRepository;
    private final RequestAuthorizer authorizer;

    @GetMapping("/{userId}")
    public List<NotificationResponse> getUserNotifications(@PathVariable Long userId, HttpServletRequest request) {
        authorizer.requireSelfOrRole(request, userId, Role.ADMIN);
        return notificationRepository.findByUserIdOrderByCreatedAtDesc(userId)
                .stream()
                .map(this::toResponse)
                .toList();
    }

    @GetMapping("/{userId}/unread-count")
    public Map<String, Long> getUnreadCount(@PathVariable Long userId, HttpServletRequest request) {
        authorizer.requireSelfOrRole(request, userId, Role.ADMIN);
        return Map.of("count", notificationRepository.countByUserIdAndIsReadFalse(userId));
    }

    @PutMapping("/{id}/read")
    public void markAsRead(@PathVariable Long id, HttpServletRequest request) {
        notificationRepository.findById(id).ifPresent(notification -> {
            authorizer.requireSelfOrRole(request, notification.getUser().getId(), Role.ADMIN);
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
