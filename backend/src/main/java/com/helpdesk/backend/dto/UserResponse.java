package com.helpdesk.backend.dto;

import com.helpdesk.backend.model.Role;
import com.helpdesk.backend.model.Team;
import com.helpdesk.backend.model.EmployeeAvailabilityStatus;
import com.helpdesk.backend.model.ExperienceLevel;

public record UserResponse(
        Long id,
        String name,
        String email,
        Role role,
        Team team,
        EmployeeAvailabilityStatus availabilityStatus,
        ExperienceLevel experienceLevel,
        String skillTags
) {}
