package com.helpdesk.backend.repository;

import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import com.helpdesk.backend.model.TicketCommentEntity;

public interface TicketCommentRepository extends JpaRepository<TicketCommentEntity, Long> {
    List<TicketCommentEntity> findByTicketIdOrderByTimestampAsc(Long ticketId);
}
