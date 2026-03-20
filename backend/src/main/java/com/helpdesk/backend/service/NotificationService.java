package com.helpdesk.backend.service;

import com.helpdesk.backend.model.NotificationEntity;
import com.helpdesk.backend.model.UserEntity;
import com.helpdesk.backend.repository.NotificationRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class NotificationService {

    private final NotificationRepository notificationRepository;

    public void createNotification(UserEntity user, String title, String message, Long ticketId) {
        NotificationEntity notification = NotificationEntity.builder()
                .user(user)
                .title(title)
                .message(message)
                .relatedTicketId(ticketId)
                .isRead(false)
                .build();
        
        notificationRepository.save(notification);
    }
}
