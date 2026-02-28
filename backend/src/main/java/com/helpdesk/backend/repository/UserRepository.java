package com.helpdesk.backend.repository;

import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import com.helpdesk.backend.model.Role;
import com.helpdesk.backend.model.UserEntity;

public interface UserRepository extends JpaRepository<UserEntity, Long> {
    Optional<UserEntity> findByEmail(String email);
    List<UserEntity> findByRole(Role role);
    List<UserEntity> findBySeededTrue();
}
