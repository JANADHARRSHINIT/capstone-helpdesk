package com.helpdesk.backend.controller;

import static com.helpdesk.backend.dto.TicketDtos.AnalyticsResponse;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import com.helpdesk.backend.model.Role;
import com.helpdesk.backend.model.IssueType;
import com.helpdesk.backend.model.TicketEntity;
import com.helpdesk.backend.model.TicketStatus;
import com.helpdesk.backend.model.UserEntity;
import com.helpdesk.backend.repository.TicketRepository;
import com.helpdesk.backend.security.RequestAuthorizer;
import com.helpdesk.backend.service.TicketAccessService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/dashboard")
@RequiredArgsConstructor
public class DashboardController {

    private final TicketRepository ticketRepository;
    private final RequestAuthorizer authorizer;
    private final TicketAccessService ticketAccessService;

    @GetMapping("/analytics")
    public AnalyticsResponse analytics(
            @RequestParam(defaultValue = "false") boolean assignedToMe,
            HttpServletRequest request
    ) {
        UserEntity actor = ticketAccessService.requireActor(authorizer.requireUser(request));
        List<TicketEntity> tickets = ticketAccessService.filterVisibleTickets(actor, ticketRepository.findAll()).stream()
                .filter(ticket -> !assignedToMe
                        || actor.getRole() != Role.EMPLOYEE
                        || ticket.getAssignedEmployee() != null && ticket.getAssignedEmployee().getId().equals(actor.getId()))
                .toList();
        long total = tickets.size();
        long open = 0;
        long inProgress = 0;
        long closed = 0;
        Map<IssueType, Long> categoryCounts = new HashMap<>();

        for (IssueType issueType : IssueType.values()) {
            categoryCounts.put(issueType, 0L);
        }

        for (TicketEntity ticket : tickets) {
            if (ticket.getStatus() == TicketStatus.OPEN) {
                open++;
            } else if (ticket.getStatus() == TicketStatus.IN_PROGRESS) {
                inProgress++;
            } else if (ticket.getStatus() == TicketStatus.RESOLVED || ticket.getStatus() == TicketStatus.CLOSED) {
                closed++;
            }
            categoryCounts.put(ticket.getIssueType(), categoryCounts.get(ticket.getIssueType()) + 1);
        }

        List<Map<String, Object>> byStatus = List.of(
                Map.of("name", "Open", "value", open),
                Map.of("name", "In Progress", "value", inProgress),
                Map.of("name", "Resolved/Closed", "value", closed)
        );

        List<Map<String, Object>> byCategory = new ArrayList<>();
        Arrays.stream(IssueType.values()).forEach(type -> {
            Map<String, Object> row = new HashMap<>();
            row.put("name", prettify(type.name()));
            row.put("value", categoryCounts.get(type));
            byCategory.add(row);
        });

        return new AnalyticsResponse(total, open, inProgress, closed, byStatus, byCategory);
    }

    private String prettify(String value) {
        return value.substring(0, 1) + value.substring(1).toLowerCase();
    }
}
