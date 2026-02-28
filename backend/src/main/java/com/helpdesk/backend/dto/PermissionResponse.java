package com.helpdesk.backend.dto;

import java.util.Map;
import com.helpdesk.backend.model.Role;

public record PermissionResponse(
        Role role,
        Map<String, Boolean> modules
) {}
