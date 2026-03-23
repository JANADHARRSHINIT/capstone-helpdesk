package com.helpdesk.backend.controller;

import java.util.List;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import com.helpdesk.backend.dto.UserResponse;
import com.helpdesk.backend.model.Role;
import com.helpdesk.backend.repository.UserRepository;
import com.helpdesk.backend.security.RequestAuthorizer;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
public class UserController {

    private final UserRepository userRepository;
    private final RequestAuthorizer authorizer;

    @GetMapping
    public List<UserResponse> listUsers(@RequestParam(required = false) Role role, HttpServletRequest request) {
        authorizer.requireAnyRole(request, Role.ADMIN);
        if (role == null) {
            return userRepository.findAll().stream().map(this::toResponse).toList();
        }
        return userRepository.findByRole(role).stream().map(this::toResponse).toList();
    }

    private UserResponse toResponse(com.helpdesk.backend.model.UserEntity user) {
        return new UserResponse(user.getId(), user.getName(), user.getEmail(), user.getRole(), user.getTeam());
    }
}
