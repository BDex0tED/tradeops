package com.tradeops.model.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Getter
@Setter
@ToString(onlyExplicitlyIncluded = true)
@AllArgsConstructor
@NoArgsConstructor
@Entity
@Table(name = "audit_logs")
public class AuditLog {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @ToString.Include
    private Long id;

    @Enumerated(EnumType.STRING)
    @Column(name = "actor_type", nullable = false)
    @ToString.Include
    private ActorType actorType;

    @ToString.Include
    @Column(name = "actor_id", nullable = false)
    private Long actorId;

    @Column(nullable = false)
    @ToString.Include
    private String action;

    @Column(name = "entity_type", nullable = false)
    @ToString.Include
    private String entityType;

    @Column(name = "entity_id")
    @ToString.Include
    private Long entityId;

    @Column(name = "diff_json", columnDefinition = "TEXT")
    @ToString.Include
    private String diffJson;

    @Column(name ="created_at", nullable = false)
    @ToString.Include
    private LocalDateTime createdAt;

    @PrePersist
    void onCreate(){
        createdAt = LocalDateTime.now();
    }
}
