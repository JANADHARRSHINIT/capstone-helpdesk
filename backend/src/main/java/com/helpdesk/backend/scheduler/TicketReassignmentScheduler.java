package com.helpdesk.backend.scheduler;

import java.time.LocalDateTime;
import java.util.List;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import com.helpdesk.backend.model.TicketAssignmentStatus;
import com.helpdesk.backend.model.TicketEntity;
import com.helpdesk.backend.model.TicketStatus;
import com.helpdesk.backend.repository.TicketRepository;
import com.helpdesk.backend.service.TicketRoutingService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Component
@RequiredArgsConstructor
@Slf4j
public class TicketReassignmentScheduler {

    private final TicketRepository ticketRepository;
    private final TicketRoutingService ticketRoutingService;

    @Value("${app.routing.accept-timeout-minutes:10}")
    private long acceptanceTimeoutMinutes;

    @Scheduled(fixedRateString = "${app.routing.reassignment-interval-ms:300000}")
    public void reassignUnacceptedTickets() {
        LocalDateTime threshold = LocalDateTime.now().minusMinutes(acceptanceTimeoutMinutes);
        List<TicketEntity> candidates = ticketRepository.findByAssignmentStatusIn(
                List.of(TicketAssignmentStatus.ASSIGNED, TicketAssignmentStatus.REASSIGNED));

        for (TicketEntity ticket : candidates) {
            boolean pendingAcceptance = ticket.getStatus() == TicketStatus.OPEN
                    && ticket.getAssignedAt() != null
                    && ticket.getAssignedAt().isBefore(threshold);
            if (!pendingAcceptance) {
                continue;
            }

            log.info("Reassigning ticket #{} after acceptance timeout", ticket.getId());
            ticketRoutingService.rerouteTicket(ticket, ticket.getAssignedEmployee());
            ticketRepository.save(ticket);
        }
    }
}
