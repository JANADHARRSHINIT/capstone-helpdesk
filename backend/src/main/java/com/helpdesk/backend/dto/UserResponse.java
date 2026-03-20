package com.helpdesk.backend.dto;

import com.helpdesk.backend.model.Role;
import com.helpdesk.backend.model.Team;

public record UserResponse(
        Long id,
        String name,
        String email,
        Role role,
        Team team
) {}
