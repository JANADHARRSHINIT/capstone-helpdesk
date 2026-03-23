package com.helpdesk.backend.security;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ResponseStatusException;
import com.helpdesk.backend.model.Role;
import jakarta.servlet.http.HttpServletRequest;

@Component
public class RequestAuthorizer {

    private static final String USER_ID_HEADER = "X-User-Id";
    private static final String USER_ROLE_HEADER = "X-User-Role";

    public RequestUser requireUser(HttpServletRequest request) {
        String rawId = request.getHeader(USER_ID_HEADER);
        String rawRole = request.getHeader(USER_ROLE_HEADER);

        if (rawId == null || rawId.isBlank() || rawRole == null || rawRole.isBlank()) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Missing authentication headers");
        }

        try {
            return new RequestUser(Long.parseLong(rawId), Role.valueOf(rawRole.toUpperCase()));
        } catch (IllegalArgumentException ex) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Invalid authentication headers");
        }
    }

    public RequestUser requireAnyRole(HttpServletRequest request, Role... roles) {
        RequestUser user = requireUser(request);
        for (Role role : roles) {
            if (user.role() == role) {
                return user;
            }
        }
        throw new ResponseStatusException(HttpStatus.FORBIDDEN, "You are not authorized for this action");
    }

    public void requireSelfOrRole(HttpServletRequest request, Long resourceUserId, Role... elevatedRoles) {
        RequestUser user = requireUser(request);
        if (user.id().equals(resourceUserId)) {
            return;
        }
        for (Role role : elevatedRoles) {
            if (user.role() == role) {
                return;
            }
        }
        throw new ResponseStatusException(HttpStatus.FORBIDDEN, "You are not authorized for this resource");
    }

    public record RequestUser(Long id, Role role) {}
}
