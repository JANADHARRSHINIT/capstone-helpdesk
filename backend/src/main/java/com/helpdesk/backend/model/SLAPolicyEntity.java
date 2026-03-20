package com.helpdesk.backend.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "sla_policies")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SLAPolicyEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, unique = true)
    private TicketPriority priority;

    @Column(nullable = false)
    private Integer responseTimeHours;

    @Column(nullable = false)
    private Integer resolutionTimeHours;

    @Column(nullable = false)
    @Builder.Default
    private Boolean active = true;
}
