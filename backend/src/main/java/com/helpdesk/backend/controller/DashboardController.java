package com.helpdesk.backend.controller;

import static com.helpdesk.backend.dto.TicketDtos.AnalyticsResponse;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import com.helpdesk.backend.model.IssueType;
import com.helpdesk.backend.model.TicketEntity;
import com.helpdesk.backend.model.TicketStatus;
import com.helpdesk.backend.repository.TicketRepository;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/dashboard")
@RequiredArgsConstructor
public class DashboardController {

    private final TicketRepository ticketRepository;

    @GetMapping("/analytics")
    public AnalyticsResponse analytics() {
        List<TicketEntity> tickets = ticketRepository.findAll();
        long total = tickets.size();
        long open = tickets.stream().filter(t -> t.getStatus() == TicketStatus.OPEN).count();
        long inProgress = tickets.stream().filter(t -> t.getStatus() == TicketStatus.IN_PROGRESS).count();
        long closed = tickets.stream().filter(t -> t.getStatus() == TicketStatus.RESOLVED || t.getStatus() == TicketStatus.CLOSED).count();

        List<Map<String, Object>> byStatus = List.of(
                Map.of("name", "Open", "value", open),
                Map.of("name", "In Progress", "value", inProgress),
                Map.of("name", "Resolved/Closed", "value", closed)
        );

        List<Map<String, Object>> byCategory = new ArrayList<>();
        Arrays.stream(IssueType.values()).forEach(type -> {
            Map<String, Object> row = new HashMap<>();
            row.put("name", prettify(type.name()));
            row.put("value", tickets.stream().filter(ticket -> ticket.getIssueType() == type).count());
            byCategory.add(row);
        });

        return new AnalyticsResponse(total, open, inProgress, closed, byStatus, byCategory);
    }

    private String prettify(String value) {
        return value.substring(0, 1) + value.substring(1).toLowerCase();
    }
}
