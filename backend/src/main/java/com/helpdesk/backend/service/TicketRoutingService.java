package com.helpdesk.backend.service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;
import org.springframework.stereotype.Service;
import com.helpdesk.backend.model.AuditLogEntity;
import com.helpdesk.backend.model.EmployeeAvailabilityStatus;
import com.helpdesk.backend.model.ExperienceLevel;
import com.helpdesk.backend.model.IssueType;
import com.helpdesk.backend.model.Role;
import com.helpdesk.backend.model.Team;
import com.helpdesk.backend.model.TicketAssignmentStatus;
import com.helpdesk.backend.model.TicketEntity;
import com.helpdesk.backend.model.TicketPriority;
import com.helpdesk.backend.model.TicketStatus;
import com.helpdesk.backend.model.UserEntity;
import com.helpdesk.backend.repository.AuditLogRepository;
import com.helpdesk.backend.repository.TicketRepository;
import com.helpdesk.backend.repository.UserRepository;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class TicketRoutingService {

    private static final Map<Team, String> PRIMARY_TEAM_EMPLOYEE_EMAILS = Map.of(
            Team.NETWORK, "john@helpdesk.com",
            Team.SOFTWARE, "jane@helpdesk.com",
            Team.HARDWARE, "mike@helpdesk.com",
            Team.SECURITY, "sara@helpdesk.com",
            Team.HR, "helen@helpdesk.com"
    );

    private final UserRepository userRepository;
    private final TicketRepository ticketRepository;
    private final AuditLogRepository auditLogRepository;
    private final NotificationService notificationService;

    public RoutingDecision routeTicket(TicketEntity ticket) {
        return routeTicket(ticket, null, false);
    }

    public RoutingDecision rerouteTicket(TicketEntity ticket, UserEntity currentAssignee) {
        return routeTicket(ticket, currentAssignee, true);
    }

    private RoutingDecision routeTicket(TicketEntity ticket, UserEntity excludedEmployee, boolean reassignment) {
        Team team = Team.valueOf(ticket.getIssueType().name());
        List<UserEntity> experts = userRepository.findByRoleAndTeamAndAvailabilityStatusNot(
                Role.EMPLOYEE, team, EmployeeAvailabilityStatus.OFFLINE);

        if (excludedEmployee != null) {
            experts = experts.stream()
                    .filter(employee -> !Objects.equals(employee.getId(), excludedEmployee.getId()))
                    .toList();
        }

        if (experts.isEmpty()) {
            return pendingQueueDecision(ticket, team, "No online employee matched the ticket category");
        }

        List<CandidateScore> scored = experts.stream()
                .map(employee -> scoreCandidate(employee, ticket))
                .sorted(candidateComparator())
                .toList();

        CandidateScore winner = scored.get(0);
        UserEntity selected = winner.employee();
        LocalDateTime now = LocalDateTime.now();

        ticket.setAssignedEmployee(selected);
        ticket.setAssignedAt(now);
        ticket.setAssignmentStatus(reassignment ? TicketAssignmentStatus.REASSIGNED : TicketAssignmentStatus.ASSIGNED);
        selected.setLastAssignedAt(now);
        userRepository.save(selected);

        String reason = buildReason(winner, scored.size(), ticket.getPriority(), reassignment);
        auditLogRepository.save(AuditLogEntity.builder()
                .action(reassignment ? "TICKET_REASSIGNED" : "TICKET_ROUTED")
                .entityType("TICKET")
                .entityId(ticket.getId())
                .performedBy("ROUTING_ENGINE")
                .details(reason)
                .build());

        notificationService.createNotification(
                selected,
                reassignment ? "Ticket Reassigned" : "New Ticket Assigned",
                "Ticket #" + ticket.getId() + " was routed to you. " + reason,
                ticket.getId());

        return new RoutingDecision(selected, now, ticket.getEstimatedResolutionAt(), reason, false);
    }

    private RoutingDecision pendingQueueDecision(TicketEntity ticket, Team team, String reason) {
        ticket.setAssignedEmployee(null);
        ticket.setAssignedAt(LocalDateTime.now());
        ticket.setAssignmentStatus(TicketAssignmentStatus.PENDING_ASSIGNMENT);

        String fullReason = reason + "; routed to " + team + " pending queue";
        auditLogRepository.save(AuditLogEntity.builder()
                .action("TICKET_PENDING_ASSIGNMENT")
                .entityType("TICKET")
                .entityId(ticket.getId())
                .performedBy("ROUTING_ENGINE")
                .details(fullReason)
                .build());

        return new RoutingDecision(null, ticket.getAssignedAt(), ticket.getEstimatedResolutionAt(), fullReason, true);
    }

    private CandidateScore scoreCandidate(UserEntity employee, TicketEntity ticket) {
        long workload = ticketRepository.countByAssignedEmployeeIdAndStatusIn(
                employee.getId(),
                List.of(TicketStatus.OPEN, TicketStatus.IN_PROGRESS));

        int availabilityScore = switch (employee.getAvailabilityStatus()) {
            case AVAILABLE -> 3;
            case BUSY -> 1;
            case OFFLINE -> -100;
        };

        int skillScore = scoreSkills(employee.getSkillTags(), ticket.getDescription(), ticket.getIssueType());
        int experienceScore = switch (employee.getExperienceLevel()) {
            case SENIOR -> 3;
            case MID -> 2;
            case JUNIOR -> 1;
        };

        int primaryOwnerBoost = isPrimaryTeamOwner(employee, ticket.getIssueType()) ? 20 : 0;

        boolean urgentTicket = ticket.getPriority() == TicketPriority.CRITICAL || ticket.getPriority() == TicketPriority.HIGH;
        int urgencyBoost = urgentTicket
                ? employee.getExperienceLevel() == ExperienceLevel.SENIOR ? 6 : employee.getExperienceLevel() == ExperienceLevel.MID ? 2 : 0
                : 0;

        int totalScore = availabilityScore * 10 + skillScore * 4 + experienceScore * 3 + primaryOwnerBoost + urgencyBoost - (int) workload * 2;

        return new CandidateScore(employee, workload, availabilityScore, skillScore, experienceScore, totalScore);
    }

    private boolean isPrimaryTeamOwner(UserEntity employee, IssueType issueType) {
        if (issueType == null) {
            return false;
        }
        String preferredEmail = PRIMARY_TEAM_EMPLOYEE_EMAILS.get(Team.valueOf(issueType.name()));
        return preferredEmail != null && preferredEmail.equalsIgnoreCase(employee.getEmail());
    }

    private Comparator<CandidateScore> candidateComparator() {
        return Comparator.comparingInt(CandidateScore::totalScore).reversed()
                .thenComparingLong(CandidateScore::workload)
                .thenComparing(candidate -> candidate.employee().getLastAssignedAt(),
                        Comparator.nullsFirst(Comparator.naturalOrder()))
                .thenComparing(candidate -> candidate.employee().getId());
    }

    private int scoreSkills(String skillTags, String description, IssueType issueType) {
        Set<String> skillSet = tokenize(skillTags);
        Set<String> ticketTerms = tokenize(description);
        ticketTerms.add(issueType.name().toLowerCase(Locale.ENGLISH));

        int matches = (int) skillSet.stream().filter(ticketTerms::contains).count();
        if (matches == 0 && skillSet.contains(issueType.name().toLowerCase(Locale.ENGLISH))) {
            return 1;
        }
        return matches;
    }

    private Set<String> tokenize(String value) {
        if (value == null || value.isBlank()) {
            return new HashSet<>();
        }
        return Arrays.stream(value.toLowerCase(Locale.ENGLISH).split("[,\\s]+"))
                .map(String::trim)
                .filter(token -> token.length() > 1)
                .collect(Collectors.toCollection(HashSet::new));
    }

    private String buildReason(CandidateScore winner, int candidates, TicketPriority priority, boolean reassignment) {
        List<String> reasons = new ArrayList<>();
        reasons.add("category expertise matched " + winner.employee().getTeam());
        reasons.add("availability=" + winner.employee().getAvailabilityStatus());
        reasons.add("active workload=" + winner.workload());
        if (winner.skillScore() > 0) {
            reasons.add("skill match score=" + winner.skillScore());
        }
        reasons.add("experience=" + winner.employee().getExperienceLevel());
        if (priority == TicketPriority.CRITICAL || priority == TicketPriority.HIGH) {
            reasons.add("priority-aware routing enabled");
        }
        if (winner.employee().getLastAssignedAt() != null) {
            reasons.add("tie-break used least recently assigned");
        }
        reasons.add("candidate pool size=" + candidates);
        if (reassignment) {
            reasons.add("ticket was reassigned after acceptance timeout");
        }
        return String.join(", ", reasons);
    }

    public record RoutingDecision(
            UserEntity assignedEmployee,
            LocalDateTime assignmentTimestamp,
            LocalDateTime estimatedResolutionAt,
            String reason,
            boolean pendingQueue
    ) {}

    private record CandidateScore(
            UserEntity employee,
            long workload,
            int availabilityScore,
            int skillScore,
            int experienceScore,
            int totalScore
    ) {}
}
