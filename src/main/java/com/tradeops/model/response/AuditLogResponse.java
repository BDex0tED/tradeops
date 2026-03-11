package com.tradeops.model.response;

import com.tradeops.model.entity.ActorType;
import java.time.LocalDateTime;

public record AuditLogResponse(
        Long id,
        ActorType actorType,
        Long actorId,
        String action,
        String entityType,
        Long entityId,
        String diffJson,
        LocalDateTime createdAt
) {}