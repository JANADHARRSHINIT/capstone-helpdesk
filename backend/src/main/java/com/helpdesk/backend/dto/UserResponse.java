package com.helpdesk.backend.dto;

import com.helpdesk.backend.model.Role;

public record UserResponse(
        Long id,
        String name,
        String email,
        Role role
) {}
