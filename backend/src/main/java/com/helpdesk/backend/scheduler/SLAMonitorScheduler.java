package com.helpdesk.backend.scheduler;

import com.helpdesk.backend.model.AuditLogEntity;
import com.helpdesk.backend.model.TicketEntity;
import com.helpdesk.backend.model.TicketStatus;
import com.helpdesk.backend.repository.AuditLogRepository;
import com.helpdesk.backend.repository.TicketRepository;
import com.helpdesk.backend.service.NotificationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import java.time.LocalDateTime;
import java.util.List;

@Component
@RequiredArgsConstructor
@Slf4j
public class SLAMonitorScheduler {

    private final TicketRepository ticketRepository;
    private final AuditLogRepository auditLogRepository;
    private final NotificationService notificationService;

    @Scheduled(fixedRate = 300000)
    public void monitorSLABreaches() {
        log.info("Running SLA breach monitoring...");
        
        List<TicketEntity> activeTickets = ticketRepository.findByStatusIn(
            List.of(TicketStatus.OPEN, TicketStatus.IN_PROGRESS)
        );

        LocalDateTime now = LocalDateTime.now();
        
        for (TicketEntity ticket : activeTickets) {
            if (ticket.getSlaDeadline() != null && now.isAfter(ticket.getSlaDeadline())) {
                ticket.setEscalationLevel(ticket.getEscalationLevel() + 1);
                ticketRepository.save(ticket);
                
                auditLogRepository.save(AuditLogEntity.builder()
                    .action("SLA_BREACH")
                    .entityType("TICKET")
                    .entityId(ticket.getId())
                    .performedBy("SYSTEM")
                    .details("SLA breached. Escalation level: " + ticket.getEscalationLevel())
                    .build());
                
                if (ticket.getAssignedEmployee() != null) {
                    notificationService.createNotification(
                        ticket.getAssignedEmployee(),
                        "SLA Breach Alert",
                        "Ticket #" + ticket.getId() + " has breached SLA deadline",
                        ticket.getId()
                    );
                }
                
                log.warn("SLA breach detected for ticket #{}", ticket.getId());
            }
        }
    }
}
