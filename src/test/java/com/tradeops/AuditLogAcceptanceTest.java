package com.tradeops;

import com.tradeops.model.entity.*;
import com.tradeops.repo.*;
import com.tradeops.service.OrderService;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@SpringBootTest
@Transactional
public class AuditLogAcceptanceTest {

    @Autowired
    private OrderService orderService;

    @Autowired
    private AuditLogRepo auditLogRepo;

    @Autowired
    private OrderRepo orderRepo;

    @Autowired
    private UserEntityRepo userEntityRepo;

    private Order testOrder;

    @BeforeEach
    void setUp() {
        testOrder = new Order();
        testOrder.setOrderNumber("TEST-AUDIT-123");
        testOrder.setStatus(OrderStatus.NEW);
        testOrder = orderRepo.save(testOrder);

        UserEntity dispatcher = new UserEntity();
        dispatcher.setUsername("dispatcher@test.com");
        dispatcher.setEmail("dispatcher@test.com");
        dispatcher.setPassword("pass");
        dispatcher.setFullName("Test Dispatcher");
        dispatcher.setRoles(List.of(new Role("ROLE_DISPATCHER")));
        userEntityRepo.save(dispatcher);
    }

    @Test
    @WithMockUser(username = "dispatcher@test.com", authorities = "ROLE_DISPATCHER")
    void testAuditLogIsCreatedAutomatically_WhenOrderStatusChanges() {
        orderService.changeOrderStatus(testOrder.getId(), OrderStatus.ASSIGNED);

        List<AuditLog> logs = auditLogRepo.findAll();

        Assertions.assertFalse(logs.isEmpty(), "Audit log must be created!");

        AuditLog latestLog = logs.get(logs.size() - 1);
        Assertions.assertEquals("ORDER_STATUS_CHANGED", latestLog.getAction());
        Assertions.assertEquals("ORDER", latestLog.getEntityType());
        Assertions.assertEquals(testOrder.getId(), latestLog.getEntityId());

        Assertions.assertEquals(ActorType.COMPANY, latestLog.getActorType());
        Assertions.assertNotNull(latestLog.getDiffJson());
        Assertions.assertTrue(latestLog.getDiffJson().contains("ASSIGNED"));
    }
}
