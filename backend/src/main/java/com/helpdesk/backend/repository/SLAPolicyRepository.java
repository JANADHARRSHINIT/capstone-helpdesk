package com.helpdesk.backend.repository;

import com.helpdesk.backend.model.SLAPolicyEntity;
import com.helpdesk.backend.model.TicketPriority;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.Optional;

@Repository
public interface SLAPolicyRepository extends JpaRepository<SLAPolicyEntity, Long> {
    Optional<SLAPolicyEntity> findByPriorityAndActiveTrue(TicketPriority priority);
}
