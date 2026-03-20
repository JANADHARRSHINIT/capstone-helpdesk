package com.helpdesk.backend.repository;

import com.helpdesk.backend.model.NotificationEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface NotificationRepository extends JpaRepository<NotificationEntity, Long> {
    List<NotificationEntity> findByUserIdOrderByCreatedAtDesc(Long userId);
    List<NotificationEntity> findByUserIdAndIsReadFalseOrderByCreatedAtDesc(Long userId);
    Long countByUserIdAndIsReadFalse(Long userId);
}
