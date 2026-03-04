package com.tradeops;

import com.tradeops.model.entity.Trader;
import com.tradeops.model.request.TraderRequests;
import com.tradeops.repo.AuditLogRepo;
import com.tradeops.repo.TraderRepo;
import com.tradeops.service.impl.TraderInfrastructureServiceImpl;
import com.tradeops.service.impl.TraderManagementServiceImpl;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.transaction.annotation.Transactional;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;

@SpringBootTest
@Transactional
public class EpicB5TraderAcceptanceTest {

    @Autowired
    private TraderManagementServiceImpl traderManagementService;

    @Autowired
    private TraderInfrastructureServiceImpl traderInfrastructureService;

    @Autowired
    private TraderRepo traderRepo;

    @Autowired
    private AuditLogRepo auditLogRepo;

    @Test
    @WithMockUser(username = "superadmin", authorities = "ROLE_SUPER_ADMIN")
    void testTraderCreationAndAsyncBuild() throws Exception {
        TraderRequests.CreateTraderRequest req = new TraderRequests.CreateTraderRequest(
                "Tech Store LLC", "Tech Store", "tech.tradeops.kg");
        Trader newTrader = traderManagementService.createTrader(req);

        Assertions.assertNotNull(newTrader.getId());
        Assertions.assertEquals(Trader.TraderStatus.PENDING, newTrader.getStatus());

        traderManagementService.changeStatus(newTrader.getId(), Trader.TraderStatus.ACTIVE);
        Assertions.assertEquals(Trader.TraderStatus.ACTIVE, traderRepo.findById(newTrader.getId()).get().getStatus());

        CompletableFuture<String> buildFuture = traderInfrastructureService.triggerFrontendBuild(newTrader.getId());

        String buildResult = buildFuture.get(7, TimeUnit.SECONDS);
        Assertions.assertEquals("BUILD_SUCCESS", buildResult);

        boolean hasBuildLog = auditLogRepo.findAll().stream()
                .anyMatch(log -> log.getAction().equals("FRONTEND_BUILD_TRIGGERED")
                        && log.getEntityId().equals(newTrader.getId()));

        Assertions.assertTrue(hasBuildLog, "Audit log should contain FRONTEND_BUILD_TRIGGERED action");
    }
}
