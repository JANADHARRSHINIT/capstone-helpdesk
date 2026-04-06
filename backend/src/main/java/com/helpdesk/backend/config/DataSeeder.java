package com.helpdesk.backend.config;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;
import com.helpdesk.backend.model.EmployeeAvailabilityStatus;
import com.helpdesk.backend.model.ExperienceLevel;
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
import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class DataSeeder implements CommandLineRunner {

    private static final Map<String, String> SEEDED_CREDENTIALS = Map.ofEntries(
            Map.entry("admin@helpdesk.com", "admin123"),
            Map.entry("john@helpdesk.com", "employee123"),
            Map.entry("jane@helpdesk.com", "employee123"),
            Map.entry("mike@helpdesk.com", "employee123"),
            Map.entry("priya.network@helpdesk.com", "employee123"),
            Map.entry("arun.software@helpdesk.com", "employee123"),
            Map.entry("neha.hardware@helpdesk.com", "employee123"),
            Map.entry("sara@helpdesk.com", "employee123"),
            Map.entry("helen@helpdesk.com", "employee123"),
            Map.entry("alice@company.com", "user123"),
            Map.entry("bob@company.com", "user123")
    );

    private final UserRepository userRepository;
    private final TicketRepository ticketRepository;
    private final TicketCommentRepository commentRepository;
    private final ModulePermissionRepository permissionRepository;
    private final SLAPolicyRepository slaPolicyRepository;

    @Override
    public void run(String... args) {
        upsertBasicUser("Admin User", "admin@helpdesk.com", Role.ADMIN);

        UserEntity employeeOne = upsertEmployee(
                "John Employee", "john@helpdesk.com", "E-NET-101", Team.NETWORK,
                ExperienceLevel.SENIOR, EmployeeAvailabilityStatus.AVAILABLE,
                "network,vpn,wifi,router,firewall,switch,lan");

        upsertEmployee(
                "Priya Network", "priya.network@helpdesk.com", "E-NET-106", Team.NETWORK,
                ExperienceLevel.MID, EmployeeAvailabilityStatus.AVAILABLE,
                "network,wan,vpn,router,connectivity,dns");

        UserEntity employeeTwo = upsertEmployee(
                "Jane Employee", "jane@helpdesk.com", "E-SW-102", Team.SOFTWARE,
                ExperienceLevel.MID, EmployeeAvailabilityStatus.AVAILABLE,
                "software,java,email,outlook,database,application,login");

        upsertEmployee(
                "Arun Software", "arun.software@helpdesk.com", "E-SW-107", Team.SOFTWARE,
                ExperienceLevel.SENIOR, EmployeeAvailabilityStatus.AVAILABLE,
                "software,api,java,spring,database,browser,erp");

        UserEntity employeeThree = upsertEmployee(
                "Mike Employee", "mike@helpdesk.com", "E-HW-103", Team.HARDWARE,
                ExperienceLevel.SENIOR, EmployeeAvailabilityStatus.AVAILABLE,
                "hardware,laptop,printer,keyboard,monitor,desktop");

        upsertEmployee(
                "Neha Hardware", "neha.hardware@helpdesk.com", "E-HW-108", Team.HARDWARE,
                ExperienceLevel.MID, EmployeeAvailabilityStatus.AVAILABLE,
                "hardware,printer,scanner,laptop,display,device");

        UserEntity securityEmployee = upsertEmployee(
                "Sara Security", "sara@helpdesk.com", "E-SEC-104", Team.SECURITY,
                ExperienceLevel.SENIOR, EmployeeAvailabilityStatus.AVAILABLE,
                "security,iam,access,phishing,firewall,compliance");

        UserEntity hrEmployee = upsertEmployee(
                "Helen HR", "helen@helpdesk.com", "E-HR-105", Team.HR,
                ExperienceLevel.MID, EmployeeAvailabilityStatus.AVAILABLE,
                "hr,payroll,attendance,leave,policy,onboarding");

        UserEntity userOne = upsertBasicUser("Alice User", "alice@company.com", Role.USER);
        UserEntity userTwo = upsertBasicUser("Bob User", "bob@company.com", Role.USER);

        seedPermissions(Role.USER, true, true, false, false, false);
        seedPermissions(Role.EMPLOYEE, true, false, true, false, true);
        seedPermissions(Role.ADMIN, true, false, false, true, true);
        seedSLAPolicies();
        restoreSeededCredentials();

        if (ticketRepository.count() == 0) {
            seedSampleTickets(userOne, userTwo, employeeOne, employeeTwo, employeeThree, securityEmployee, hrEmployee);
        }

        ensureDemoTicketAssignments(userOne, employeeOne, employeeTwo, employeeThree, securityEmployee, hrEmployee);
    }

    private void seedSLAPolicies() {
        if (slaPolicyRepository.count() == 0) {
            slaPolicyRepository.saveAll(List.of(
                SLAPolicyEntity.builder()
                    .priority(TicketPriority.CRITICAL)
                    .responseTimeHours(1)
                    .resolutionTimeHours(2)
                    .active(true)
                    .build(),
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
                userRepository.save(user);
            }
        });
    }

    private UserEntity upsertBasicUser(String name, String email, Role role) {
        Optional<UserEntity> existing = userRepository.findByEmail(email);
        UserEntity user = existing.orElseGet(UserEntity::new);
        user.setName(name);
        user.setEmail(email);
        user.setPassword(SEEDED_CREDENTIALS.getOrDefault(email, "user123"));
        user.setSeeded(true);
        user.setRole(role);
        return userRepository.save(user);
    }

    private UserEntity upsertEmployee(String name, String email, String employeeId, Team team,
                                      ExperienceLevel experienceLevel, EmployeeAvailabilityStatus availabilityStatus,
                                      String skillTags) {
        Optional<UserEntity> existing = userRepository.findByEmail(email);
        UserEntity user = existing.orElseGet(UserEntity::new);
        user.setName(name);
        user.setEmail(email);
        user.setPassword(SEEDED_CREDENTIALS.getOrDefault(email, "employee123"));
        user.setEmployeeId(employeeId);
        user.setSeeded(true);
        user.setRole(Role.EMPLOYEE);
        user.setTeam(team);
        user.setExperienceLevel(experienceLevel);
        user.setAvailabilityStatus(availabilityStatus);
        user.setSkillTags(skillTags);
        return userRepository.save(user);
    }

    private void seedSampleTickets(UserEntity userOne, UserEntity userTwo, UserEntity networkEmployee,
                                   UserEntity softwareEmployee, UserEntity hardwareEmployee,
                                   UserEntity securityEmployee, UserEntity hrEmployee) {
        TicketEntity ticketOne = ticketRepository.save(TicketEntity.builder()
                .requester(userOne)
                .assignedEmployee(softwareEmployee)
                .issueType(IssueType.SOFTWARE)
                .description("Unable to access email client. Getting authentication error.")
                .priority(TicketPriority.HIGH)
                .status(TicketStatus.IN_PROGRESS)
                .build());

        TicketEntity ticketTwo = ticketRepository.save(TicketEntity.builder()
                .requester(userTwo)
                .assignedEmployee(hardwareEmployee)
                .issueType(IssueType.HARDWARE)
                .description("Printer not working in office")
                .priority(TicketPriority.MEDIUM)
                .status(TicketStatus.OPEN)
                .build());

        TicketEntity ticketThree = ticketRepository.save(TicketEntity.builder()
                .requester(userOne)
                .assignedEmployee(networkEmployee)
                .issueType(IssueType.NETWORK)
                .description("Slow internet connection")
                .priority(TicketPriority.LOW)
                .status(TicketStatus.RESOLVED)
                .build());

        TicketEntity ticketFour = ticketRepository.save(TicketEntity.builder()
                .requester(userTwo)
                .assignedEmployee(securityEmployee)
                .issueType(IssueType.SECURITY)
                .description("Suspicious phishing email received by finance team")
                .priority(TicketPriority.CRITICAL)
                .status(TicketStatus.OPEN)
                .build());

        TicketEntity ticketFive = ticketRepository.save(TicketEntity.builder()
                .requester(userOne)
                .assignedEmployee(hrEmployee)
                .issueType(IssueType.HR)
                .description("Unable to access payroll portal")
                .priority(TicketPriority.MEDIUM)
                .status(TicketStatus.OPEN)
                .build());

        commentRepository.saveAll(List.of(
                TicketCommentEntity.builder()
                        .ticket(ticketOne)
                        .user(userOne)
                        .message("I cannot login to my email")
                        .build(),
                TicketCommentEntity.builder()
                        .ticket(ticketOne)
                        .user(softwareEmployee)
                        .message("Working on this issue")
                        .build(),
                TicketCommentEntity.builder()
                        .ticket(ticketTwo)
                        .user(userTwo)
                        .message("Please resolve quickly")
                        .build(),
                TicketCommentEntity.builder()
                        .ticket(ticketThree)
                        .user(networkEmployee)
                        .message("Investigating the network latency")
                        .build(),
                TicketCommentEntity.builder()
                        .ticket(ticketFour)
                        .user(securityEmployee)
                        .message("Security team is reviewing the message headers")
                        .build(),
                TicketCommentEntity.builder()
                        .ticket(ticketFive)
                        .user(hrEmployee)
                        .message("HR portal access is being checked")
                        .build()
        ));
    }

    private void ensureDemoTicketAssignments(
            UserEntity requester,
            UserEntity networkEmployee,
            UserEntity softwareEmployee,
            UserEntity hardwareEmployee,
            UserEntity securityEmployee,
            UserEntity hrEmployee
    ) {
        ensureAssignedDemoTicket(requester, networkEmployee, IssueType.NETWORK,
                "Office wifi drops every few minutes for my laptop", TicketPriority.MEDIUM);
        ensureAssignedDemoTicket(requester, softwareEmployee, IssueType.SOFTWARE,
                "Email application closes unexpectedly while opening messages", TicketPriority.HIGH);
        ensureAssignedDemoTicket(requester, hardwareEmployee, IssueType.HARDWARE,
                "Laptop keyboard is not responding and needs hardware support", TicketPriority.MEDIUM);
        ensureAssignedDemoTicket(requester, securityEmployee, IssueType.SECURITY,
                "Suspicious account access alert needs security review", TicketPriority.HIGH);
        ensureAssignedDemoTicket(requester, hrEmployee, IssueType.HR,
                "Unable to access payroll details in the HR portal", TicketPriority.MEDIUM);
    }

    private void ensureAssignedDemoTicket(
            UserEntity requester,
            UserEntity employee,
            IssueType issueType,
            String description,
            TicketPriority priority
    ) {
        boolean alreadyAssigned = ticketRepository.findByAssignedEmployeeId(employee.getId()).stream()
                .anyMatch(ticket -> ticket.getIssueType() == issueType);
        if (alreadyAssigned) {
            return;
        }

        ticketRepository.save(TicketEntity.builder()
                .requester(requester)
                .assignedEmployee(employee)
                .issueType(issueType)
                .description(description)
                .priority(priority)
                .status(TicketStatus.OPEN)
                .build());
    }

    private void seedPermissions(Role role, boolean raiseTicket, boolean selfServiceTools,
                                 boolean smartSuggestions, boolean modelRetraining, boolean fraudAlerts) {
        if (!permissionRepository.findByRole(role).isEmpty()) {
            return;
        }
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
