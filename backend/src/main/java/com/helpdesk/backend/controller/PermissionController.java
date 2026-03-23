package com.helpdesk.backend.controller;

import java.util.Map;
import java.util.stream.Collectors;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;
import com.helpdesk.backend.dto.PermissionResponse;
import com.helpdesk.backend.model.Role;
import com.helpdesk.backend.repository.ModulePermissionRepository;
import com.helpdesk.backend.security.RequestAuthorizer;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/permissions")
@RequiredArgsConstructor
public class PermissionController {

    private final ModulePermissionRepository permissionRepository;
    private final RequestAuthorizer authorizer;

    @GetMapping("/{role}")
    public PermissionResponse byRole(@PathVariable Role role, HttpServletRequest request) {
        RequestAuthorizer.RequestUser user = authorizer.requireUser(request);
        if (user.role() != Role.ADMIN && user.role() != role) {
            throw new ResponseStatusException(
                    org.springframework.http.HttpStatus.FORBIDDEN,
                    "You are not authorized to view these permissions"
            );
        }
        Map<String, Boolean> modules = permissionRepository.findByRole(role).stream()
                .collect(Collectors.toMap(
                        permission -> permission.getModule().name(),
                        permission -> permission.isAllowed()
                ));
        return new PermissionResponse(role, modules);
    }
}
