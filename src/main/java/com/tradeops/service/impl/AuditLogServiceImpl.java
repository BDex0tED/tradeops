package com.tradeops.service.impl;

import com.tradeops.mapper.AuditLogMapper;
import com.tradeops.model.entity.AuditLog;
import com.tradeops.model.response.AuditLogResponse;
import com.tradeops.repo.AuditLogRepo;
import com.tradeops.service.AuditLogService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class AuditLogServiceImpl implements AuditLogService {

    private final AuditLogRepo auditLogRepo;
    private final AuditLogMapper auditLogMapper;

    @Override
    @Transactional(readOnly = true)
    public Page<AuditLogResponse> getAuditLogs(Pageable pageable) {
        Page<AuditLog> auditLogs = auditLogRepo.findAll(pageable);
        return auditLogs.map(auditLogMapper::toAuditLogResponse);
    }
}