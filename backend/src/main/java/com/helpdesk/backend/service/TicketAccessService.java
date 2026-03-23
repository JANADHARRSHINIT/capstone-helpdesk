package com.helpdesk.backend.service;

import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;
import com.helpdesk.backend.model.Role;
import com.helpdesk.backend.model.TicketEntity;
import com.helpdesk.backend.model.Team;
import com.helpdesk.backend.model.UserEntity;
import com.helpdesk.backend.repository.UserRepository;
import com.helpdesk.backend.security.RequestAuthorizer;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class TicketAccessService {

    private final UserRepository userRepository;

    public UserEntity requireActor(RequestAuthorizer.RequestUser requestUser) {
        return userRepository.findById(requestUser.id())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "User not found"));
    }

    public List<TicketEntity> filterVisibleTickets(UserEntity actor, List<TicketEntity> tickets) {
        return tickets.stream().filter(ticket -> canViewTicket(actor, ticket)).toList();
    }

    public boolean canViewTicket(UserEntity actor, TicketEntity ticket) {
        if (actor.getRole() == Role.ADMIN) {
            return true;
        }
        if (actor.getRole() == Role.USER) {
            return ticket.getRequester().getId().equals(actor.getId());
        }
        if (actor.getRole() == Role.EMPLOYEE) {
            if (isRelevantEmployeeAssignment(actor, ticket)) {
                return true;
            }
            Team actorTeam = actor.getTeam();
            Team routingTeam = resolveRoutingTeam(ticket);
            return actorTeam != null && routingTeam != null && actorTeam == routingTeam;
        }
        return false;
    }

    private boolean isRelevantEmployeeAssignment(UserEntity actor, TicketEntity ticket) {
        if (ticket.getAssignedEmployee() == null || !ticket.getAssignedEmployee().getId().equals(actor.getId())) {
            return false;
        }
        Team actorTeam = actor.getTeam();
        Team routingTeam = resolveRoutingTeam(ticket);
        return actorTeam != null && routingTeam != null && actorTeam == routingTeam;
    }

    private Team resolveRoutingTeam(TicketEntity ticket) {
        if (ticket.getRoutingTeam() != null) {
            return ticket.getRoutingTeam();
        }
        if (ticket.getIssueType() == null) {
            return null;
        }
        return switch (ticket.getIssueType()) {
            case HARDWARE -> Team.HARDWARE;
            case SOFTWARE -> Team.SOFTWARE;
            case NETWORK -> Team.NETWORK;
        };
    }

    public void ensureCanViewTicket(UserEntity actor, TicketEntity ticket) {
        if (!canViewTicket(actor, ticket)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "You are not authorized to access this ticket");
        }
    }

    public void ensureCanUpdateStatus(UserEntity actor, TicketEntity ticket) {
        if (actor.getRole() == Role.ADMIN) {
            return;
        }
        if (actor.getRole() == Role.EMPLOYEE && canViewTicket(actor, ticket)) {
            return;
        }
        throw new ResponseStatusException(HttpStatus.FORBIDDEN, "You are not authorized to update this ticket");
    }
}
