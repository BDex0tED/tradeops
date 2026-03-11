package com.tradeops.service;

import com.tradeops.model.response.AuditLogResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface AuditLogService {
    Page<AuditLogResponse> getAuditLogs(Pageable pageable);
}