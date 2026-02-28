package com.tradeops;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.tradeops.model.entity.Trader;
import com.tradeops.model.request.CustomerLinkRequest;
import com.tradeops.repo.CustomerLinkRepo;
import com.tradeops.repo.TraderRepo;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
public class CustomerLinkTest {

    @Autowired
    private MockMvc mockMvc;

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Autowired
    private TraderRepo traderRepo;

    @Autowired
    private CustomerLinkRepo customerLinkRepo;

    @Test
    void createCustomerLink_ValidData_SavesToDatabase() throws Exception {
        Trader trader = new Trader();
        trader.setLegalName("Link Test LLC");
        trader.setDomain("linktest.tradeops.kg");
        trader.setDisplayName("Link Test Store");
        trader.setStatus(Trader.TraderStatus.ACTIVE);
        trader = traderRepo.save(trader);

        String externalId = UUID.randomUUID().toString();
        CustomerLinkRequest request = new CustomerLinkRequest(
                trader.getId(),
                externalId,
                "+502040883"
        );

        mockMvc.perform(post("/api/v1/storefront/customer-link")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated());

        boolean linkExists = customerLinkRepo.findAll().stream()
                .anyMatch(link -> link.getCustomerExternalId().equals(externalId));

        Assertions.assertTrue(linkExists, "CustomerLink should be saved to database");
    }
}