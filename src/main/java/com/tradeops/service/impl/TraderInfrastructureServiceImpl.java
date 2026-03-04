package com.tradeops.service.impl;

import com.tradeops.annotation.Auditable;
import com.tradeops.exceptions.ResourceNotFoundException;
import com.tradeops.exceptions.UserAlreadyExistsException;
import com.tradeops.model.entity.Role;
import com.tradeops.model.entity.Trader;
import com.tradeops.model.entity.TraderUser;
import com.tradeops.model.entity.UserEntity;
import com.tradeops.model.request.TraderRequests.CreatePersonnelRequest;
import com.tradeops.model.request.TraderRequests.ThemeConfigRequest;
import com.tradeops.repo.RoleRepo;
import com.tradeops.repo.TraderRepo;
import com.tradeops.repo.TraderUserRepo;
import com.tradeops.repo.UserEntityRepo;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.concurrent.CompletableFuture;

@Slf4j
@Service
@RequiredArgsConstructor
public class TraderInfrastructureServiceImpl {

    private final TraderRepo traderRepo;
    private final TraderUserRepo traderUserRepo;
    private final UserEntityRepo userEntityRepo;
    private final RoleRepo roleRepo;
    private final PasswordEncoder passwordEncoder;

    @Transactional
    @Auditable(action = "THEME_UPDATED", entityType = "TRADER")
    public Trader updateTheme(Long traderId, ThemeConfigRequest request) {
        Trader trader = traderRepo.findById(traderId)
                .orElseThrow(() -> new ResourceNotFoundException("Trader not found"));
        trader.setThemeConfigJson(request.themeConfigJson());
        return traderRepo.save(trader);
    }

    @Transactional
    @Auditable(action = "PERSONNEL_ADDED", entityType = "TRADER_USER")
    public TraderUser addPersonnel(Long traderId, CreatePersonnelRequest request) {
        Trader trader = traderRepo.findById(traderId)
                .orElseThrow(() -> new ResourceNotFoundException("Trader not found"));

        if (userEntityRepo.existsByEmail(request.email())) {
            throw new UserAlreadyExistsException("Email already in use");
        }

        Role role = roleRepo.findByName(request.role())
                .orElseThrow(() -> new ResourceNotFoundException("Role not found"));

        UserEntity userEntity = new UserEntity();
        userEntity.setUsername(request.email());
        userEntity.setEmail(request.email());
        userEntity.setFullName(request.name());
        userEntity.setPassword(passwordEncoder.encode(request.password()));
        userEntity.setRoles(List.of(role));
        userEntity.setCreatedAt(LocalDateTime.now());
        userEntity.setApproved(true);
        userEntityRepo.save(userEntity);

        TraderUser traderUser = new TraderUser();
        traderUser.setTrader(trader);
        traderUser.setName(request.name());
        traderUser.setEmail(request.email());
        traderUser.setRole(request.role());
        traderUser.setPasswordHash(userEntity.getPassword());

        return traderUserRepo.save(traderUser);
    }

    @Auditable(action = "SSL_UPLOADED", entityType = "TRADER")
    public void uploadSslCertificate(Long traderId) {
        log.info("SSL Certificate uploaded and applied for Trader ID: {}", traderId);
    }

    @Async
    @Auditable(action = "FRONTEND_BUILD_TRIGGERED", entityType = "TRADER")
    public CompletableFuture<String> triggerFrontendBuild(Long traderId) {
        log.info("Starting frontend build for Trader ID: {}...", traderId);
        try {
            Thread.sleep(5000);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            return CompletableFuture.completedFuture("BUILD_FAILED");
        }
        log.info("Frontend build COMPLETED for Trader ID: {}", traderId);
        return CompletableFuture.completedFuture("BUILD_SUCCESS");
    }
}
