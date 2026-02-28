package com.helpdesk.backend.repository;

import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import com.helpdesk.backend.model.ModulePermissionEntity;
import com.helpdesk.backend.model.Role;

public interface ModulePermissionRepository extends JpaRepository<ModulePermissionEntity, Long> {
    List<ModulePermissionEntity> findByRole(Role role);
}
