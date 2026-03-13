package com.tradeops;

import com.tradeops.model.entity.Trader;
import com.tradeops.model.request.TraderRequests;
import com.tradeops.model.response.TraderResponse;
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
        String uniqueDomain = "tech-" + System.currentTimeMillis() + ".tradeops.kg";
        traderRepo.findByDomain(uniqueDomain).ifPresent(t -> traderRepo.delete(t));

        TraderRequests.CreateTraderRequest req = new TraderRequests.CreateTraderRequest(
                "Tech Store LLC", "Tech Store", uniqueDomain);
        TraderResponse newTrader = traderManagementService.createTrader(req);

        Assertions.assertNotNull(newTrader.id());
        Assertions.assertEquals(Trader.TraderStatus.PENDING, newTrader.status());

        traderManagementService.changeStatus(newTrader.id(), Trader.TraderStatus.ACTIVE);
        Assertions.assertEquals(Trader.TraderStatus.ACTIVE, traderRepo.findById(newTrader.id()).get().getStatus());

        CompletableFuture<String> buildFuture = traderInfrastructureService.triggerFrontendBuild(newTrader.id());

        String buildResult = buildFuture.get(7, TimeUnit.SECONDS);
        Assertions.assertEquals("BUILD_SUCCESS", buildResult);

        boolean hasBuildLog = auditLogRepo.findAll().stream()
                .anyMatch(log -> log.getAction().equals("FRONTEND_BUILD_TRIGGERED")
                        && log.getEntityId().equals(newTrader.id()));

        Assertions.assertTrue(hasBuildLog, "Audit log should contain FRONTEND_BUILD_TRIGGERED action");

        // Cleanup
        traderRepo.deleteById(newTrader.id());
    }
}
