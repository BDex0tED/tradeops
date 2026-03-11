package com.tradeops.mapper;

import com.tradeops.model.entity.AuditLog;
import com.tradeops.model.response.AuditLogResponse;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface AuditLogMapper {
    AuditLogResponse toAuditLogResponse(AuditLog auditLog);
}