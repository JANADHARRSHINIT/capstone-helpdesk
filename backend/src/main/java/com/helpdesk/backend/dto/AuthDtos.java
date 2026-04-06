package com.helpdesk.backend.dto;

import com.helpdesk.backend.model.Role;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public class AuthDtos {
    public record LoginRequest(
            @Email @NotBlank String email,
            @NotBlank String password
    ) {}

    public record SignupRequest(
            @NotBlank String name,
            @Email @NotBlank String email,
            @NotBlank @Size(min = 8, message = "Password must be at least 8 characters") String password,
            @NotBlank @Pattern(regexp = "^[0-9+\\-()\\s]{7,20}$", message = "Phone number is invalid") String phoneNumber,
            @NotNull Role role,
            String employeeId,
            String adminCode
    ) {}

    public record AuthResponse(
            Long id,
            String name,
            String email,
            Role role
    ) {}
}
