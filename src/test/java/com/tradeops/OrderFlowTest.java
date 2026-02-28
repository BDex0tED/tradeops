package com.tradeops;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.tradeops.model.entity.Category;
import com.tradeops.model.entity.InventoryItem;
import com.tradeops.model.entity.Product;
import com.tradeops.model.entity.Trader;
import com.tradeops.model.request.CreateOrderRequest;
import com.tradeops.model.request.OrderLineRequest;
import com.tradeops.repo.*;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.util.List;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
public class OrderFlowTest {

    @Autowired
    private MockMvc mockMvc;

    private final ObjectMapper objMapper = new ObjectMapper();
    @Autowired
    private TraderRepo traderRepo;
    @Autowired
    private CategoryRepo categoryRepo;
    @Autowired
    private ProductRepo productRepo;
    @Autowired
    private InventoryItemRepo inventoryItemRepo;
    @Autowired
    private OrderRepo orderRepo;

    private Trader testTrader;
    private Product testProduct;

    @BeforeEach
    void setUp() {
        String uniqueSuffix = String.valueOf(System.currentTimeMillis());

        testTrader = new Trader();
        testTrader.setLegalName("Test LLC " + uniqueSuffix);
        testTrader.setDomain("test" + uniqueSuffix + ".tradeops.kg");
        testTrader.setDisplayName("Test Store " + uniqueSuffix);
        testTrader.setStatus(Trader.TraderStatus.ACTIVE);
        testTrader = traderRepo.save(testTrader);

        Category category = new Category();
        category.setName("Electronics " + uniqueSuffix);
        category.setSlug("electronics-" + uniqueSuffix);
        category = categoryRepo.save(category);

        testProduct = new Product();
        testProduct.setSku("LAPTOP-" + uniqueSuffix); // Уникальный SKU!
        testProduct.setName("MacBook Pro");
        testProduct.setBasePrice(new BigDecimal("1500.00"));
        testProduct.setCategory(category);
        testProduct = productRepo.save(testProduct);

        InventoryItem inventory = new InventoryItem();
        inventory.setProduct(testProduct);
        inventory.setQtyOnHand(5);
        inventory.setQtyReserved(0);
        inventoryItemRepo.save(inventory);
    }

    @Test
    void createOrder_WithValidData_CreatesOrderAndReservesStock() throws Exception{
        OrderLineRequest oLR = new OrderLineRequest(testProduct.getId(), 2);
        CreateOrderRequest createOR = new CreateOrderRequest(
                testTrader.getId(),
                "Bishkek, Ankara 1/8 st.",
                "0502040883",
                "COD",
                List.of(oLR)
        );

        mockMvc.perform(post("/api/v1/storefront/orders")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objMapper.writeValueAsString(createOR)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.orderNumber").exists())
                .andExpect(jsonPath("$.status").value("NEW"))
                .andExpect(jsonPath("$.totals").value(3000.00));

        InventoryItem iI = inventoryItemRepo.findByProductId(testProduct.getId()).orElseThrow();
        Assertions.assertEquals(2, iI.getQtyReserved(), "Should reserve 2 laptops");
    }


    @Test
    void createOrder_WithInsufficientStock_ReturnsErrorAndRollsBack() throws Exception {
        OrderLineRequest line = new OrderLineRequest(testProduct.getId(), 10);
        CreateOrderRequest request = new CreateOrderRequest(
                testTrader.getId(),
                "Bishkek, Chuy Ave 123",
                "+996555123456",
                "COD",
                List.of(line)
        );

        mockMvc.perform(post("/api/v1/storefront/orders")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objMapper.writeValueAsString(request)))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.error.message").exists());

        Assertions.assertEquals(0, orderRepo.count(), "Order should not be created");
    }
}
