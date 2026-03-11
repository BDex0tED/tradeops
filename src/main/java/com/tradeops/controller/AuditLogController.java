package com.tradeops.controller;

import com.tradeops.model.response.AuditLogResponse;
import com.tradeops.service.AuditLogService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/admin/audit")
@RequiredArgsConstructor
public class AuditLogController {

    private final AuditLogService auditLogService;

    @PreAuthorize("hasAnyAuthority('SUPER_ADMIN', 'DISPATCHER', 'ROLE_SUPER_ADMIN', 'ROLE_DISPATCHER')")
    @GetMapping
    public ResponseEntity<Page<AuditLogResponse>> getAuditLogs(
            @PageableDefault(sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable) {
        return ResponseEntity.ok(auditLogService.getAuditLogs(pageable));
    }
}