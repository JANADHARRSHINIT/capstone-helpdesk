package com.helpdesk.backend.dto;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import com.helpdesk.backend.model.IssueType;
import com.helpdesk.backend.model.TicketPriority;
import com.helpdesk.backend.model.TicketStatus;

public class TicketDtos {
    public record TicketSummaryResponse(
            Long id,
            Long customerId,
            String customerName,
            Long assignedEmployeeId,
            String assignedEmployeeName,
            IssueType issueType,
            String description,
            TicketPriority priority,
            TicketStatus status,
            LocalDateTime createdAt,
            LocalDateTime updatedAt
    ) {}

    public record TicketCommentResponse(
            Long id,
            Long userId,
            String userName,
            String message,
            LocalDateTime timestamp
    ) {}

    public record TicketDetailResponse(
            Long id,
            Long customerId,
            String customerName,
            Long assignedEmployeeId,
            String assignedEmployeeName,
            IssueType issueType,
            String description,
            TicketPriority priority,
            TicketStatus status,
            LocalDateTime createdAt,
            LocalDateTime updatedAt,
            List<TicketCommentResponse> comments
    ) {}

    public record AddCommentRequest(
            Long userId,
            String message
    ) {}

    public record UpdateStatusRequest(
            TicketStatus status
    ) {}

    public record UpdatePriorityRequest(
            TicketPriority priority
    ) {}

    public record AssignTicketRequest(
            Long employeeId
    ) {}

    public record CreateTicketRequest(
            Long requesterId,
            IssueType issueType,
            String description,
            TicketPriority priority
    ) {}

    public record AnalyticsResponse(
            long totalTickets,
            long openTickets,
            long inProgressTickets,
            long closedTickets,
            List<Map<String, Object>> ticketsByStatus,
            List<Map<String, Object>> ticketsByCategory
    ) {}
}
