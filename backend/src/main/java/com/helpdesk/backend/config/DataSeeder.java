package com.helpdesk.backend.config;

import java.util.List;
import java.util.Map;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;
import com.helpdesk.backend.model.IssueType;
import com.helpdesk.backend.model.ModuleName;
import com.helpdesk.backend.model.ModulePermissionEntity;
import com.helpdesk.backend.model.Role;
import com.helpdesk.backend.model.Team;
import com.helpdesk.backend.model.TicketCommentEntity;
import com.helpdesk.backend.model.TicketEntity;
import com.helpdesk.backend.model.TicketPriority;
import com.helpdesk.backend.model.TicketStatus;
import com.helpdesk.backend.model.UserEntity;
import com.helpdesk.backend.model.SLAPolicyEntity;
import com.helpdesk.backend.repository.ModulePermissionRepository;
import com.helpdesk.backend.repository.TicketCommentRepository;
import com.helpdesk.backend.repository.TicketRepository;
import com.helpdesk.backend.repository.UserRepository;
import com.helpdesk.backend.repository.SLAPolicyRepository;
import com.helpdesk.backend.service.TicketIntelligenceService;
import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class DataSeeder implements CommandLineRunner {

    private static final Map<String, String> SEEDED_CREDENTIALS = Map.of(
            "admin@helpdesk.com", "admin123",
            "john@helpdesk.com", "employee123",
            "jane@helpdesk.com", "employee123",
            "mike@helpdesk.com", "employee123",
            "alice@company.com", "user123",
            "bob@company.com", "user123"
    );

    private final UserRepository userRepository;
    private final TicketRepository ticketRepository;
    private final TicketCommentRepository commentRepository;
    private final ModulePermissionRepository permissionRepository;
    private final SLAPolicyRepository slaPolicyRepository;
    private final TicketIntelligenceService ticketIntelligenceService;

    @Override
    public void run(String... args) {
        if (userRepository.count() > 0) {
            restoreSeededCredentials();
            restoreSeededAssignments();
            reclassifyExistingTickets();
            return;
        }

        UserEntity admin = userRepository.save(UserEntity.builder()
                .name("Admin User")
                .email("admin@helpdesk.com")
                .password(SEEDED_CREDENTIALS.get("admin@helpdesk.com"))
                .seeded(true)
                .role(Role.ADMIN)
                .build());

        UserEntity employeeOne = userRepository.save(UserEntity.builder()
                .name("John Employee")
                .email("john@helpdesk.com")
                .password(SEEDED_CREDENTIALS.get("john@helpdesk.com"))
                .seeded(true)
                .role(Role.EMPLOYEE)
                .team(Team.HARDWARE)
                .build());

        UserEntity employeeTwo = userRepository.save(UserEntity.builder()
                .name("Jane Employee")
                .email("jane@helpdesk.com")
                .password(SEEDED_CREDENTIALS.get("jane@helpdesk.com"))
                .seeded(true)
                .role(Role.EMPLOYEE)
                .team(Team.SOFTWARE)
                .build());

        UserEntity employeeThree = userRepository.save(UserEntity.builder()
                .name("Mike Employee")
                .email("mike@helpdesk.com")
                .password(SEEDED_CREDENTIALS.get("mike@helpdesk.com"))
                .seeded(true)
                .role(Role.EMPLOYEE)
                .team(Team.NETWORK)
                .build());

        UserEntity userOne = userRepository.save(UserEntity.builder()
                .name("Alice User")
                .email("alice@company.com")
                .password(SEEDED_CREDENTIALS.get("alice@company.com"))
                .seeded(true)
                .role(Role.USER)
                .build());

        UserEntity userTwo = userRepository.save(UserEntity.builder()
                .name("Bob User")
                .email("bob@company.com")
                .password(SEEDED_CREDENTIALS.get("bob@company.com"))
                .seeded(true)
                .role(Role.USER)
                .build());

        TicketEntity ticketOne = ticketRepository.save(TicketEntity.builder()
                .requester(userOne)
                .assignedEmployee(employeeTwo)
                .issueType(IssueType.SOFTWARE)
                .description("Unable to access email client. Getting authentication error.")
                .priority(TicketPriority.HIGH)
                .status(TicketStatus.IN_PROGRESS)
                .build());

        TicketEntity ticketTwo = ticketRepository.save(TicketEntity.builder()
                .requester(userTwo)
                .assignedEmployee(employeeOne)
                .issueType(IssueType.HARDWARE)
                .description("Printer not working in office")
                .priority(TicketPriority.MEDIUM)
                .status(TicketStatus.OPEN)
                .build());

        ticketRepository.saveAll(List.of(
                TicketEntity.builder()
                        .requester(userOne)
                        .assignedEmployee(employeeThree)
                        .issueType(IssueType.NETWORK)
                        .description("Slow internet connection")
                        .priority(TicketPriority.LOW)
                        .status(TicketStatus.RESOLVED)
                        .build(),
                TicketEntity.builder()
                        .requester(userTwo)
                        .issueType(IssueType.SOFTWARE)
                        .description("Need office suite installation")
                        .priority(TicketPriority.MEDIUM)
                        .status(TicketStatus.OPEN)
                        .build(),
                TicketEntity.builder()
                        .requester(userOne)
                        .assignedEmployee(employeeOne)
                        .issueType(IssueType.HARDWARE)
                        .description("Laptop screen flickering")
                        .priority(TicketPriority.HIGH)
                        .status(TicketStatus.IN_PROGRESS)
                        .build(),
                TicketEntity.builder()
                        .requester(userTwo)
                        .assignedEmployee(employeeThree)
                        .issueType(IssueType.NETWORK)
                        .description("Cannot connect to VPN")
                        .priority(TicketPriority.HIGH)
                        .status(TicketStatus.OPEN)
                        .build()
        ));

        commentRepository.saveAll(List.of(
                TicketCommentEntity.builder()
                        .ticket(ticketOne)
                        .user(userOne)
                        .message("I cannot login to my email")
                        .build(),
                TicketCommentEntity.builder()
                        .ticket(ticketOne)
                        .user(employeeOne)
                        .message("Working on this issue")
                        .build(),
                TicketCommentEntity.builder()
                        .ticket(ticketTwo)
                        .user(userTwo)
                        .message("Please resolve quickly")
                        .build()
        ));

        seedPermissions(Role.USER, true, true, false, false, false);
        seedPermissions(Role.EMPLOYEE, true, false, true, false, true);
        seedPermissions(Role.ADMIN, true, false, false, true, true);

        seedSLAPolicies();
        reclassifyExistingTickets();
    }

    private void seedSLAPolicies() {
        if (slaPolicyRepository.count() == 0) {
            slaPolicyRepository.saveAll(List.of(
                SLAPolicyEntity.builder()
                    .priority(TicketPriority.HIGH)
                    .responseTimeHours(1)
                    .resolutionTimeHours(4)
                    .active(true)
                    .build(),
                SLAPolicyEntity.builder()
                    .priority(TicketPriority.MEDIUM)
                    .responseTimeHours(4)
                    .resolutionTimeHours(24)
                    .active(true)
                    .build(),
                SLAPolicyEntity.builder()
                    .priority(TicketPriority.LOW)
                    .responseTimeHours(24)
                    .resolutionTimeHours(72)
                    .active(true)
                    .build()
            ));
        }
    }

    private void restoreSeededCredentials() {
        userRepository.findAll().forEach(user -> {
            String expectedPassword = SEEDED_CREDENTIALS.get(user.getEmail());
            if (expectedPassword != null) {
                user.setSeeded(true);
                user.setPassword(expectedPassword);
                if ("john@helpdesk.com".equals(user.getEmail())) {
                    user.setName("John Employee");
                    user.setRole(Role.EMPLOYEE);
                    user.setTeam(Team.HARDWARE);
                } else if ("jane@helpdesk.com".equals(user.getEmail())) {
                    user.setName("Jane Employee");
                    user.setRole(Role.EMPLOYEE);
                    user.setTeam(Team.SOFTWARE);
                } else if ("mike@helpdesk.com".equals(user.getEmail())) {
                    user.setName("Mike Employee");
                    user.setRole(Role.EMPLOYEE);
                    user.setTeam(Team.NETWORK);
                }
                userRepository.save(user);
            }
        });
    }

    private void restoreSeededAssignments() {
        UserEntity john = userRepository.findByEmail("john@helpdesk.com").orElse(null);
        UserEntity jane = userRepository.findByEmail("jane@helpdesk.com").orElse(null);
        UserEntity mike = userRepository.findByEmail("mike@helpdesk.com").orElse(null);
        if (john == null || jane == null || mike == null) {
            return;
        }

        ticketRepository.findAll().forEach(ticket -> {
            if ("Unable to access email client. Getting authentication error.".equals(ticket.getDescription())) {
                ticket.setAssignedEmployee(jane);
            } else if ("Printer not working in office".equals(ticket.getDescription())) {
                ticket.setAssignedEmployee(john);
            } else if ("Slow internet connection".equals(ticket.getDescription())
                    || "Cannot connect to VPN".equals(ticket.getDescription())) {
                ticket.setAssignedEmployee(mike);
            } else if ("Laptop screen flickering".equals(ticket.getDescription())) {
                ticket.setAssignedEmployee(john);
            } else if (ticket.getRequester() != null
                    && ticket.getRequester().isSeeded()
                    && ticket.getIssueType() != null
                    && ticket.getAssignedEmployee() != null
                    && ticket.getAssignedEmployee().isSeeded()
                    && ticket.getAssignedEmployee().getRole() == Role.EMPLOYEE
                    && !isSpecialistFor(ticket.getAssignedEmployee(), ticket.getIssueType())) {
                ticket.setAssignedEmployee(defaultAssigneeFor(ticket.getIssueType(), john, jane, mike));
            }
            ticketRepository.save(ticket);
        });
    }

    private boolean isSpecialistFor(UserEntity employee, IssueType issueType) {
        if (employee.getTeam() == null || issueType == null) {
            return false;
        }
        return switch (issueType) {
            case HARDWARE -> employee.getTeam() == Team.HARDWARE;
            case SOFTWARE -> employee.getTeam() == Team.SOFTWARE;
            case NETWORK -> employee.getTeam() == Team.NETWORK;
        };
    }

    private UserEntity defaultAssigneeFor(IssueType issueType, UserEntity john, UserEntity jane, UserEntity mike) {
        return switch (issueType) {
            case HARDWARE -> john;
            case SOFTWARE -> jane;
            case NETWORK -> mike;
        };
    }

    private void reclassifyExistingTickets() {
        ticketRepository.findAll().stream()
                .filter(ticket -> ticket.getDescription() != null && !ticket.getDescription().isBlank())
                .forEach(ticket -> {
                    TicketIntelligenceService.TicketDecision decision =
                            ticketIntelligenceService.analyze(ticket.getDescription());
                    boolean changed = false;

                    if (ticket.getIssueType() != decision.issueType()) {
                        ticket.setIssueType(decision.issueType());
                        changed = true;
                    }
                    if (ticket.getPriority() != decision.priority()) {
                        ticket.setPriority(decision.priority());
                        changed = true;
                    }
                    if (ticket.getRoutingTeam() != decision.routingTeam()) {
                        ticket.setRoutingTeam(decision.routingTeam());
                        changed = true;
                    }
                    if (ticket.getClassificationConfidence() == null
                            || !ticket.getClassificationConfidence().equals(decision.classificationConfidence())) {
                        ticket.setClassificationConfidence(decision.classificationConfidence());
                        changed = true;
                    }

                    Long currentAssigneeId = ticket.getAssignedEmployee() != null ? ticket.getAssignedEmployee().getId() : null;
                    Long predictedAssigneeId = decision.assignedEmployee() != null ? decision.assignedEmployee().getId() : null;
                    if ((currentAssigneeId == null && predictedAssigneeId != null)
                            || (currentAssigneeId != null && !currentAssigneeId.equals(predictedAssigneeId))) {
                        ticket.setAssignedEmployee(decision.assignedEmployee());
                        changed = true;
                    }

                    if (changed) {
                        ticketRepository.save(ticket);
                    }
                });
    }

    private void seedPermissions(Role role, boolean raiseTicket, boolean selfServiceTools,
                                 boolean smartSuggestions, boolean modelRetraining, boolean fraudAlerts) {
        permissionRepository.saveAll(List.of(
                permission(role, ModuleName.RAISE_TICKET, raiseTicket),
                permission(role, ModuleName.SELF_SERVICE_TOOLS, selfServiceTools),
                permission(role, ModuleName.SMART_SUGGESTIONS, smartSuggestions),
                permission(role, ModuleName.MODEL_RETRAINING, modelRetraining),
                permission(role, ModuleName.FRAUD_ALERTS, fraudAlerts)
        ));
    }

    private ModulePermissionEntity permission(Role role, ModuleName moduleName, boolean allowed) {
        return ModulePermissionEntity.builder()
                .role(role)
                .module(moduleName)
                .allowed(allowed)
                .build();
    }
}
