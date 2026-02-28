package com.helpdesk.backend.controller;

import java.util.Map;
import java.util.stream.Collectors;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import com.helpdesk.backend.dto.PermissionResponse;
import com.helpdesk.backend.model.Role;
import com.helpdesk.backend.repository.ModulePermissionRepository;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/permissions")
@RequiredArgsConstructor
public class PermissionController {

    private final ModulePermissionRepository permissionRepository;

    @GetMapping("/{role}")
    public PermissionResponse byRole(@PathVariable Role role) {
        Map<String, Boolean> modules = permissionRepository.findByRole(role).stream()
                .collect(Collectors.toMap(
                        permission -> permission.getModule().name(),
                        permission -> permission.isAllowed()
                ));
        return new PermissionResponse(role, modules);
    }
}
