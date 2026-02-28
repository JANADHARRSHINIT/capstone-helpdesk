package com.helpdesk.backend.controller;

import static com.helpdesk.backend.dto.AuthDtos.AuthResponse;
import static com.helpdesk.backend.dto.AuthDtos.LoginRequest;
import static com.helpdesk.backend.dto.AuthDtos.SignupRequest;
import java.util.Map;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;
import com.helpdesk.backend.model.Role;
import com.helpdesk.backend.model.UserEntity;
import com.helpdesk.backend.repository.UserRepository;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final UserRepository userRepository;

    @PostMapping("/login")
    public AuthResponse login(@Valid @RequestBody LoginRequest request) {
        UserEntity user = userRepository.findByEmail(request.email())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Invalid credentials"));

        if (!user.getPassword().equals(request.password())) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Invalid credentials");
        }
        return toAuthResponse(user);
    }

    @PostMapping("/signup")
    public AuthResponse signup(@Valid @RequestBody SignupRequest request) {
        userRepository.findByEmail(request.email()).ifPresent(existing -> {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Email already exists");
        });

        if (request.role() == Role.ADMIN) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Admin signup is restricted");
        }

        UserEntity saved = userRepository.save(UserEntity.builder()
                .name(request.name())
                .email(request.email())
                .password(request.password())
                .seeded(false)
                .role(request.role())
                .build());
        return toAuthResponse(saved);
    }

    @PostMapping("/demo/{role}")
    public AuthResponse demoLogin(@PathVariable String role) {
        Role parsedRole;
        try {
            parsedRole = Role.valueOf(role.toUpperCase());
        } catch (IllegalArgumentException ex) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Unsupported role");
        }
        UserEntity user = userRepository.findByRole(parsedRole).stream()
                .findFirst()
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "No demo user for role"));
        return toAuthResponse(user);
    }

    @PostMapping("/health")
    public Map<String, String> health() {
        return Map.of("status", "ok");
    }

    private AuthResponse toAuthResponse(UserEntity user) {
        return new AuthResponse(user.getId(), user.getName(), user.getEmail(), user.getRole());
    }
}
