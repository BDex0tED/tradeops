package com.tradeops.service.impl;

import java.io.ByteArrayOutputStream;
import java.io.IOException;

import com.tradeops.annotation.Auditable;
import com.tradeops.exceptions.ResourceNotFoundException;
import com.tradeops.model.entity.PackageArtifact;
import com.tradeops.model.entity.Trader;
import com.tradeops.repo.PackageArtifactRepo;
import com.tradeops.repo.TraderRepo;
import com.tradeops.service.PackageBuildService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Stream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;
import org.springframework.util.FileSystemUtils;
import org.springframework.util.ResourceUtils;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletableFuture;

@Slf4j
@Service
@RequiredArgsConstructor
public class PackageBuildServiceImpl implements PackageBuildService {

    private final TraderRepo traderRepo;
    private final PackageArtifactRepo packageArtifactRepo;

    @Value("${tradeops.artifacts.dir:target/artifacts}")
    private String artifactsDir;

    @Value("${tradeops.main.api.baseUrl:http://localhost:8080}")
    private String mainApiBaseUrl;

    @Override
    public byte[] generateTraderPackage(Long traderId) throws IOException {
        log.info("Generating direct package ZIP for Trader ID: {}", traderId);
        Trader trader = traderRepo.findById(traderId)
                .orElseThrow(() -> new ResourceNotFoundException("Trader not found"));

        Path tempDir = Files.createTempDirectory("trader-pkg-");
        try {
            cloneTemplateToTempDir(tempDir);

            // 1. Generate the .env file specific to this trader
            String envContent = generateEnvContent(trader);
            Files.writeString(tempDir.resolve(".env"), envContent);

            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            zipDirectory(tempDir, baos);
            return baos.toByteArray();
        } catch (Exception e) {
            log.error("Failed to generate package for Trader ID: {}", traderId, e);
            throw new IOException("Failed to generate package ZIP", e);
        } finally {
            FileSystemUtils.deleteRecursively(tempDir);
        }
    }

    private String generateEnvContent(Trader trader) {
        // Generate unique secret keys
        String jwtSecret = UUID.randomUUID().toString().replace("-", "")
                + UUID.randomUUID().toString().replace("-", "");
        String shopJwtSecret = UUID.randomUUID().toString().replace("-", "")
                + UUID.randomUUID().toString().replace("-", "");
        String sessionSecret = UUID.randomUUID().toString().replace("-", "")
                + UUID.randomUUID().toString().replace("-", "");

        return "# =============================================================================\n" +
                "# QUICK START (see README.md for full setup)\n" +
                "# =============================================================================\n" +
                "# 1. Start backend: cd ../online_shop-backend && docker compose up -d\n" +
                "# 2. Register trader: POST /api/v1/auth/register-trader\n" +
                "# 3. Verify OTP: POST /api/v1/auth/login/otp\n" +
                "# 4. Wait for admin approval\n" +
                "# 5. Copy: cp .env.example .env\n" +
                "# 6. Edit: SHOP_NAME, TRADER_ID, TRADER_EMAIL, TRADER_PASSWORD, secrets\n" +
                "# 7. Run: docker compose up --build\n" +
                "# 8. Login CMS: http://localhost:8000 | Shop: http://localhost:8001\n" +
                "# =============================================================================\n\n" +
                "# =============================================================================\n" +
                "# SHOP TEMPLATE CONFIGURATION\n" +
                "# =============================================================================\n\n" +
                "# -----------------------------------------------------------------------------\n" +
                "# SHOP IDENTITY (REQUIRED - Configure for your shop)\n" +
                "# -----------------------------------------------------------------------------\n" +
                "# Unique shop name displayed in UI\n" +
                "SHOP_NAME=\"" + trader.getDisplayName() + "\"\n\n" +
                "# Your trader ID from the main backend system (Trader.id)\n" +
                "TRADER_ID=" + trader.getId() + "\n\n" +
                "# Trader login credentials (must match the account created in tradeops backend)\n" +
                "# Used to seed the CMS database on first boot\n" +
                "TRADER_EMAIL=trader@example.com\n" +
                "TRADER_PASSWORD=your-trader-password\n\n" +
                "# -----------------------------------------------------------------------------\n" +
                "# BACKEND CONNECTION (REQUIRED)\n" +
                "# -----------------------------------------------------------------------------\n" +
                "# URL to the main backend API\n" +
                "# For Docker: use container name (e.g., http://shopbackend:8080)\n" +
                "# For local development: use http://localhost:8080\n" +
                "ADMIN_API_BASE_URL=http://localhost:8080\n\n" +
                "# -----------------------------------------------------------------------------\n" +
                "# SECURITY KEYS (REQUIRED - CHANGE IN PRODUCTION!)\n" +
                "# -----------------------------------------------------------------------------\n" +
                "# Generate secure random keys for production:\n" +
                "# python -c \"import secrets; print(secrets.token_hex(32))\"\n\n" +
                "# JWT secret for CMS authentication\n" +
                "JWT_SECRET_KEY=" + jwtSecret + "\n\n" +
                "# JWT secret for Shop customer authentication\n" +
                "SHOP_JWT_SECRET_KEY=" + shopJwtSecret + "\n\n" +
                "# Session secret for cookie encryption\n" +
                "SESSION_SECRET_KEY=" + sessionSecret + "\n\n" +
                "# -----------------------------------------------------------------------------\n" +
                "# DATABASE (Auto-configured by Docker)\n" +
                "# -----------------------------------------------------------------------------\n" +
                "# Only change if using external database\n" +
                "# DATABASE_URL=postgresql+asyncpg://postgres:postgres@postgres:5432/brokercms\n" +
                "DATABASE_URL=postgresql+asyncpg://postgres:postgres@postgres:5432/shop_data\n\n" +
                "# -----------------------------------------------------------------------------\n" +
                "# OPTIONAL SETTINGS\n" +
                "# -----------------------------------------------------------------------------\n" +
                "# JWT token expiration\n" +
                "# JWT_ALGORITHM=HS256\n" +
                "# ACCESS_TOKEN_EXPIRE_MINUTES=30\n" +
                "# REFRESH_TOKEN_EXPIRE_DAYS=7\n\n" +
                "# File uploads\n" +
                "# MAX_IMAGE_SIZE_MB=5\n\n" +
                "# Port overrides (change if defaults conflict with other services)\n" +
                "# CMS_PORT=8000\n" +
                "# SHOP_PORT=8001\n" +
                "# DB_PORT=5432\n";
    }

    @Override
    @Async
    @Auditable(action = "FRONTEND_BUILD_TRIGGERED", entityType = "TRADER")
    public CompletableFuture<String> triggerBuild(Long traderId) {
        log.info("Starting Package Build for Trader ID: {}", traderId);

        Trader trader = traderRepo.findById(traderId)
                .orElseThrow(() -> new ResourceNotFoundException("Trader not found for build"));

        PackageArtifact artifact = new PackageArtifact();
        artifact.setTraderId(traderId);
        artifact.setBuildStatus(PackageArtifact.BuildStatus.PENDING);
        artifact.setArtifactFilePath("");
        artifact = packageArtifactRepo.save(artifact);

        try {
            File dir = new File(artifactsDir);
            if (!dir.exists()) {
                dir.mkdirs();
            }

            String zipFileName = "trader_" + traderId + "_package_" + System.currentTimeMillis() + ".zip";
            Path zipFilePath = Paths.get(artifactsDir, zipFileName);

            String envFileContent = generateEnvContent(trader);
            String deployScriptContent = generateDeployScript();

            Path tempDir = Files.createTempDirectory("trader-pkg-build-");
            try {
                cloneTemplateToTempDir(tempDir);

                Files.writeString(tempDir.resolve(".env"), envFileContent);
                Files.writeString(tempDir.resolve("deploy.sh"), deployScriptContent);

                try (FileOutputStream fos = new FileOutputStream(zipFilePath.toFile())) {
                    zipDirectory(tempDir, fos);
                }
            } finally {
                FileSystemUtils.deleteRecursively(tempDir);
            }

            artifact.setArtifactFilePath(zipFilePath.toAbsolutePath().toString());
            artifact.setBuildStatus(PackageArtifact.BuildStatus.SUCCESS);
            packageArtifactRepo.save(artifact);

            log.info("Package Build completed successfully for Trader ID: {}. File: {}", traderId, zipFilePath);
            return CompletableFuture.completedFuture("BUILD_SUCCESS");

        } catch (Exception e) {
            log.error("Failed to build package for Trader ID: {}", traderId, e);
            artifact.setBuildStatus(PackageArtifact.BuildStatus.FAILED);
            packageArtifactRepo.save(artifact);
            return CompletableFuture.completedFuture("FAILED");
        }
    }

    private void cloneTemplateToTempDir(Path tempDir) {
        try {
            log.info("Cloning trader-cms repository into temporary directory...");
            ProcessBuilder pb = new ProcessBuilder(
                    "git", "clone", "https://github.com/user31133/trader-cms.git", ".");
            pb.directory(tempDir.toFile());
            Process process = pb.start();
            int exitCode = process.waitFor();

            if (exitCode != 0) {
                log.error("Failed to clone repository. Exit code: {}", exitCode);
            } else {
                log.info("Successfully cloned trader-cms repository.");
            }

            // Clean up the .git directory so it doesn't get zipped
            FileSystemUtils.deleteRecursively(tempDir.resolve(".git"));
        } catch (IOException | InterruptedException e) {
            log.error("Exception occurred while cloning repository", e);
            Thread.currentThread().interrupt();
        }
    }

    private void zipDirectory(Path sourceDir, OutputStream os) throws IOException {
        try (ZipOutputStream zos = new ZipOutputStream(os);
                Stream<Path> paths = Files.walk(sourceDir)) {

            paths.filter(path -> !Files.isDirectory(path)).forEach(path -> {
                ZipEntry zipEntry = new ZipEntry(sourceDir.relativize(path).toString().replace("\\", "/"));
                try {
                    zos.putNextEntry(zipEntry);
                    Files.copy(path, zos);
                    zos.closeEntry();
                } catch (IOException e) {
                    throw new RuntimeException("Failed to zip file " + path, e);
                }
            });
        }
    }

    private String generateDeployScript() {
        return "#!/bin/bash\n" +
                "echo 'Deploying Trader CMS & Shop...'\n" +
                "docker-compose down\n" +
                "docker-compose pull\n" +
                "docker-compose up -d\n" +
                "echo 'Deployment Complete!'\n" +
                "echo 'CMS is running on port 8000'\n" +
                "echo 'Shop is running on port 8001'\n";
    }
}
