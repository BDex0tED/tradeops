package com.tradeops.controller;
 
import com.tradeops.exceptions.ResourceNotFoundException;

import com.tradeops.model.entity.PackageArtifact;
import com.tradeops.model.entity.TraderUser;
import com.tradeops.model.request.TraderRequests.CreatePersonnelRequest;
import com.tradeops.model.request.TraderRequests.ThemeConfigRequest;
import com.tradeops.model.response.TraderUserResponse;
import com.tradeops.service.impl.TraderInfrastructureServiceImpl;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@Slf4j
@RestController
@RequestMapping("/api/v1/trader/{traderId}")
@RequiredArgsConstructor
public class TraderInfrastructureController {

    private final TraderInfrastructureServiceImpl infrastructureService;
    private final com.tradeops.repo.PackageArtifactRepo packageArtifactRepo;
    private final com.tradeops.repo.TraderRepo traderRepo;
    private final com.tradeops.service.PackageBuildService packageBuildService;

    @PostMapping("/personnel")
    @PreAuthorize("hasAnyAuthority('ROLE_TRADER_ADMIN', 'ROLE_SUPER_ADMIN')")
    public ResponseEntity<TraderUserResponse> addPersonnel(
            @PathVariable Long traderId,
            @Valid @RequestBody CreatePersonnelRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(infrastructureService.addPersonnel(traderId, request));
    }

    @PatchMapping("/settings/theme")
    @PreAuthorize("hasAnyAuthority('ROLE_TRADER_ADMIN', 'ROLE_SUPER_ADMIN')")
    public ResponseEntity<Void> updateTheme(
            @PathVariable Long traderId,
            @Valid @RequestBody ThemeConfigRequest request) {
        infrastructureService.updateTheme(traderId, request);
        return ResponseEntity.ok().build();
    }

    @PostMapping(value = "/settings/ssl", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @PreAuthorize("hasAnyAuthority('ROLE_DEVOPS_SYSADMIN', 'ROLE_SUPER_ADMIN')")
    public ResponseEntity<String> uploadSsl(
            @PathVariable Long traderId,
            @RequestParam("certFile") MultipartFile certFile,
            @RequestParam("keyFile") MultipartFile keyFile) {
        infrastructureService.uploadSslCertificate(traderId, certFile, keyFile);
        return ResponseEntity.ok("SSL Certificate accepted for processing");
    }

    @PostMapping("/build")
    @PreAuthorize("hasAnyAuthority('ROLE_DEVOPS_SYSADMIN', 'ROLE_SUPER_ADMIN')")
    public ResponseEntity<String> triggerBuild(@PathVariable Long traderId) {
        infrastructureService.triggerFrontendBuild(traderId);
        return ResponseEntity.status(HttpStatus.ACCEPTED)
                .body("Build triggered successfully. It will complete in background.");
    }

    @GetMapping("/package/download")
    @PreAuthorize("hasAnyAuthority('ROLE_MODERATOR', 'ROLE_SUPER_ADMIN', 'ROLE_DEVOPS_SYSADMIN', 'ROLE_TRADER_ADMIN')")
    public ResponseEntity<Resource> downloadLatestPackage(
            @PathVariable Long traderId) throws java.io.IOException {
        
        // 1. Verify trader exists
        if (!traderRepo.existsById(traderId)) {
            throw new ResourceNotFoundException("Trader with ID " + traderId + " not found");
        }

        // 2. Check for pre-built successful artifact
        List<PackageArtifact> artifacts = packageArtifactRepo.findByTraderIdOrderByCreatedAtDesc(traderId);
        if (!artifacts.isEmpty() && artifacts.get(0).getBuildStatus() == com.tradeops.model.entity.PackageArtifact.BuildStatus.SUCCESS) {
            java.nio.file.Path filePath = java.nio.file.Paths.get(artifacts.get(0).getArtifactFilePath());
            org.springframework.core.io.Resource resource = new org.springframework.core.io.UrlResource(filePath.toUri());

            if (resource.exists()) {
                return ResponseEntity.ok()
                        .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + resource.getFilename() + "\"")
                        .body(resource);
            }
        }

        // 3. Fallback: Generate package on-the-fly
        log.info("No successful pre-built artifact found for trader {}. Generating on-the-fly.", traderId);
        byte[] zipContent = packageBuildService.generateTraderPackage(traderId);
        org.springframework.core.io.Resource resource = new org.springframework.core.io.ByteArrayResource(zipContent);

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"trader-" + traderId + "-package.zip\"")
                .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_OCTET_STREAM_VALUE)
                .body(resource);
    }
}
