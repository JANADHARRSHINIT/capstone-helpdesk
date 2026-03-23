package com.helpdesk.backend.controller;

import static com.helpdesk.backend.dto.TicketDtos.AddCommentRequest;
import static com.helpdesk.backend.dto.TicketDtos.AssignTicketRequest;
import static com.helpdesk.backend.dto.TicketDtos.CreateTicketRequest;
import static com.helpdesk.backend.dto.TicketDtos.TicketCommentResponse;
import static com.helpdesk.backend.dto.TicketDtos.TicketDetailResponse;
import static com.helpdesk.backend.dto.TicketDtos.TicketSummaryResponse;
import static com.helpdesk.backend.dto.TicketDtos.UpdateStatusRequest;
import static com.helpdesk.backend.dto.TicketDtos.UpdatePriorityRequest;
import java.time.LocalDateTime;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;
import com.helpdesk.backend.model.TicketCommentEntity;
import com.helpdesk.backend.model.TicketEntity;
import com.helpdesk.backend.model.TicketPriority;
import com.helpdesk.backend.model.Role;
import com.helpdesk.backend.model.TicketStatus;
import com.helpdesk.backend.model.UserEntity;
import com.helpdesk.backend.model.AuditLogEntity;
import com.helpdesk.backend.model.SLAPolicyEntity;
import com.helpdesk.backend.repository.TicketCommentRepository;
import com.helpdesk.backend.repository.TicketRepository;
import com.helpdesk.backend.repository.UserRepository;
import com.helpdesk.backend.repository.AuditLogRepository;
import com.helpdesk.backend.repository.SLAPolicyRepository;
import com.helpdesk.backend.security.RequestAuthorizer;
import com.helpdesk.backend.service.TicketAccessService;
import com.helpdesk.backend.service.NotificationService;
import com.helpdesk.backend.service.TicketIntelligenceService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/tickets")
@RequiredArgsConstructor
public class TicketController {

    private final TicketRepository ticketRepository;
    private final TicketCommentRepository commentRepository;
    private final UserRepository userRepository;
    private final AuditLogRepository auditLogRepository;
    private final SLAPolicyRepository slaPolicyRepository;
    private final NotificationService notificationService;
    private final RequestAuthorizer authorizer;
    private final TicketIntelligenceService ticketIntelligenceService;
    private final TicketAccessService ticketAccessService;

    @PostMapping
    public TicketSummaryResponse createTicket(@RequestBody CreateTicketRequest request, HttpServletRequest httpRequest) {
        RequestAuthorizer.RequestUser actor = authorizer.requireUser(httpRequest);
        if (request.requesterId() == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Requester is required");
        }
        if (request.description() == null || request.description().isBlank()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Description is required");
        }
        if (actor.role() == Role.USER && !actor.id().equals(request.requesterId())) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Users can only create tickets for themselves");
        }
        UserEntity requester = userRepository.findById(request.requesterId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found"));
        TicketIntelligenceService.TicketDecision decision =
                ticketIntelligenceService.analyze(request.description().trim());

        LocalDateTime slaDeadline = calculateSLADeadline(decision.priority());

        TicketEntity ticket = TicketEntity.builder()
                .requester(requester)
                .assignedEmployee(decision.assignedEmployee())
                .issueType(decision.issueType())
                .description(request.description().trim())
                .priority(decision.priority())
                .status(TicketStatus.OPEN)
                .routingTeam(decision.routingTeam())
                .classificationConfidence(decision.classificationConfidence())
                .slaDeadline(slaDeadline)
                .escalationLevel(0)
                .build();
        TicketEntity savedTicket = ticketRepository.save(ticket);

        if (decision.assignedEmployee() != null) {
            notificationService.createNotification(
                    decision.assignedEmployee(),
                    "New Ticket Auto-Routed",
                    "Ticket #" + savedTicket.getId() + " was classified as " + decision.issueType()
                            + " with " + decision.priority() + " priority and assigned to you",
                    savedTicket.getId()
            );
        }

        auditLogRepository.save(AuditLogEntity.builder()
                .action("AUTO_CLASSIFICATION")
                .entityType("TICKET")
                .entityId(savedTicket.getId())
                .performedBy("ML_ROUTER")
                .details("Predicted issueType=" + decision.issueType()
                        + ", priority=" + decision.priority()
                        + ", team=" + decision.routingTeam()
                        + ", confidence=" + decision.classificationConfidence()
                        + ", assignee=" + (decision.assignedEmployee() != null ? decision.assignedEmployee().getName() : "UNASSIGNED"))
                .build());

        return toSummary(savedTicket);
    }

    private LocalDateTime calculateSLADeadline(TicketPriority priority) {
        return slaPolicyRepository.findByPriorityAndActiveTrue(priority)
                .map(policy -> LocalDateTime.now().plusHours(policy.getResolutionTimeHours()))
                .orElse(LocalDateTime.now().plusDays(3));
    }

    @GetMapping
    public List<TicketSummaryResponse> listTickets(
            @RequestParam(required = false) TicketStatus status,
            @RequestParam(required = false) TicketPriority priority,
            @RequestParam(required = false) String search,
            @RequestParam(defaultValue = "false") boolean assignedToMe,
            HttpServletRequest httpRequest
    ) {
        UserEntity actor = ticketAccessService.requireActor(authorizer.requireUser(httpRequest));
        List<TicketEntity> tickets;
        if (status != null && priority != null) {
            tickets = ticketRepository.findByStatusAndPriority(status, priority);
        } else if (status != null) {
            tickets = ticketRepository.findByStatus(status);
        } else if (priority != null) {
            tickets = ticketRepository.findByPriority(priority);
        } else {
            tickets = ticketRepository.findAll();
        }

        return ticketAccessService.filterVisibleTickets(actor, tickets).stream()
                .filter(ticket -> !assignedToMe || ticket.getAssignedEmployee() != null && ticket.getAssignedEmployee().getId().equals(actor.getId()))
                .filter(ticket -> matchesSearch(ticket, search))
                .map(this::toSummary)
                .toList();
    }

    @GetMapping("/{id}")
    public TicketDetailResponse getTicket(@PathVariable Long id, HttpServletRequest request) {
        UserEntity actor = ticketAccessService.requireActor(authorizer.requireUser(request));
        TicketEntity ticket = findTicket(id);
        ticketAccessService.ensureCanViewTicket(actor, ticket);
        List<TicketCommentResponse> comments = commentRepository.findByTicketIdOrderByTimestampAsc(id)
                .stream()
                .map(this::toCommentResponse)
                .toList();

        return new TicketDetailResponse(
                ticket.getId(),
                ticket.getRequester().getId(),
                ticket.getRequester().getName(),
                ticket.getAssignedEmployee() != null ? ticket.getAssignedEmployee().getId() : null,
                ticket.getAssignedEmployee() != null ? ticket.getAssignedEmployee().getName() : null,
                ticket.getIssueType(),
                ticket.getDescription(),
                ticket.getPriority(),
                ticket.getRoutingTeam(),
                ticket.getClassificationConfidence(),
                ticket.getStatus(),
                ticket.getCreatedAt(),
                ticket.getUpdatedAt(),
                comments
        );
    }

    @PutMapping("/{id}/status")
    public TicketSummaryResponse updateStatus(@PathVariable Long id, @RequestBody UpdateStatusRequest request, HttpServletRequest httpRequest) {
        UserEntity actor = ticketAccessService.requireActor(authorizer.requireAnyRole(httpRequest, Role.ADMIN, Role.EMPLOYEE));
        TicketEntity ticket = findTicket(id);
        ticketAccessService.ensureCanUpdateStatus(actor, ticket);
        String oldStatus = ticket.getStatus().toString();
        ticket.setStatus(request.status());
        
        auditLogRepository.save(AuditLogEntity.builder()
                .action("STATUS_CHANGE")
                .entityType("TICKET")
                .entityId(id)
                .performedBy(actor.getRole().name())
                .details("Status changed from " + oldStatus + " to " + request.status())
                .build());
        
        return toSummary(ticketRepository.save(ticket));
    }

    @PutMapping("/{id}/priority")
    public TicketSummaryResponse updatePriority(@PathVariable Long id, @RequestBody UpdatePriorityRequest request, HttpServletRequest httpRequest) {
        UserEntity actor = ticketAccessService.requireActor(authorizer.requireAnyRole(httpRequest, Role.ADMIN));
        TicketEntity ticket = findTicket(id);
        String oldPriority = ticket.getPriority().toString();
        ticket.setPriority(request.priority());
        
        auditLogRepository.save(AuditLogEntity.builder()
                .action("PRIORITY_CHANGE")
                .entityType("TICKET")
                .entityId(id)
                .performedBy(actor.getRole().name())
                .details("Priority changed from " + oldPriority + " to " + request.priority())
                .build());
        
        return toSummary(ticketRepository.save(ticket));
    }

    @PutMapping("/{id}/assign")
    public TicketSummaryResponse assignTicket(@PathVariable Long id, @RequestBody AssignTicketRequest request, HttpServletRequest httpRequest) {
        UserEntity actor = ticketAccessService.requireActor(authorizer.requireAnyRole(httpRequest, Role.ADMIN));
        TicketEntity ticket = findTicket(id);
        UserEntity employee = null;
        String oldAssignment = ticket.getAssignedEmployee() != null ? ticket.getAssignedEmployee().getName() : "Unassigned";
        
        if (request.employeeId() != null) {
            employee = userRepository.findById(request.employeeId())
                    .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Employee not found"));
            if (employee.getRole() != Role.EMPLOYEE) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Only employees can be assigned to tickets");
            }
        }
        ticket.setAssignedEmployee(employee);
        
        String newAssignment = employee != null ? employee.getName() : "Unassigned";
        auditLogRepository.save(AuditLogEntity.builder()
                .action("TICKET_ASSIGNMENT")
                .entityType("TICKET")
                .entityId(id)
                .performedBy(actor.getRole().name())
                .details("Ticket reassigned from " + oldAssignment + " to " + newAssignment)
                .build());
        
        if (employee != null) {
            notificationService.createNotification(
                employee,
                "New Ticket Assigned",
                "Ticket #" + id + " has been assigned to you",
                id
            );
        }
        
        return toSummary(ticketRepository.save(ticket));
    }

    @PostMapping("/{id}/comments")
    public TicketCommentResponse addComment(@PathVariable Long id, @RequestBody AddCommentRequest request, HttpServletRequest httpRequest) {
        UserEntity actor = ticketAccessService.requireActor(authorizer.requireUser(httpRequest));
        TicketEntity ticket = findTicket(id);
        ticketAccessService.ensureCanViewTicket(actor, ticket);
        if (!actor.getId().equals(request.userId())) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "You can only comment as the authenticated user");
        }
        UserEntity user = userRepository.findById(request.userId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found"));

        TicketCommentEntity comment = TicketCommentEntity.builder()
                .ticket(ticket)
                .user(user)
                .message(request.message())
                .build();
        return toCommentResponse(commentRepository.save(comment));
    }

    private TicketEntity findTicket(Long id) {
        return ticketRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Ticket not found"));
    }

    private boolean matchesSearch(TicketEntity ticket, String search) {
        if (search == null || search.isBlank()) {
            return true;
        }
        String normalized = search.toLowerCase();
        return ticket.getId().toString().contains(normalized)
                || ticket.getRequester().getName().toLowerCase().contains(normalized);
    }

    private TicketSummaryResponse toSummary(TicketEntity ticket) {
        return new TicketSummaryResponse(
                ticket.getId(),
                ticket.getRequester().getId(),
                ticket.getRequester().getName(),
                ticket.getAssignedEmployee() != null ? ticket.getAssignedEmployee().getId() : null,
                ticket.getAssignedEmployee() != null ? ticket.getAssignedEmployee().getName() : null,
                ticket.getIssueType(),
                ticket.getDescription(),
                ticket.getPriority(),
                ticket.getRoutingTeam(),
                ticket.getClassificationConfidence(),
                ticket.getStatus(),
                ticket.getCreatedAt(),
                ticket.getUpdatedAt()
        );
    }

    private TicketCommentResponse toCommentResponse(TicketCommentEntity comment) {
        return new TicketCommentResponse(
                comment.getId(),
                comment.getUser().getId(),
                comment.getUser().getName(),
                comment.getMessage(),
                comment.getTimestamp()
        );
    }
}
