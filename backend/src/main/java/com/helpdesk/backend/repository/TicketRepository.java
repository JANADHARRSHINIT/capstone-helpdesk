package com.helpdesk.backend.repository;

import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import com.helpdesk.backend.model.TicketEntity;
import com.helpdesk.backend.model.TicketPriority;
import com.helpdesk.backend.model.TicketStatus;

public interface TicketRepository extends JpaRepository<TicketEntity, Long> {
    List<TicketEntity> findByStatus(TicketStatus status);
    List<TicketEntity> findByStatusIn(List<TicketStatus> statuses);
    List<TicketEntity> findByPriority(TicketPriority priority);
    List<TicketEntity> findByStatusAndPriority(TicketStatus status, TicketPriority priority);
}
