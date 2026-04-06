package com.helpdesk.backend.controller;

import com.helpdesk.backend.model.AuditLogEntity;
import com.helpdesk.backend.repository.AuditLogRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api/audit-logs")
@RequiredArgsConstructor
public class AuditLogController {

    private final AuditLogRepository auditLogRepository;

    @GetMapping
    public List<AuditLogEntity> getLogs() {
        return auditLogRepository.findTop100ByOrderByTimestampDesc();
    }

    @GetMapping("/ticket/{ticketId}")
    public List<AuditLogEntity> getTicketLogs(@PathVariable Long ticketId) {
        return auditLogRepository.findByEntityTypeAndEntityId("TICKET", ticketId);
    }
}
