package com.tradeops;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
public class RbacTest {

    private ObjectMapper objectMapper = new ObjectMapper();

    @Autowired
    private MockMvc mockMvc;

    @Test
    void accessAdminEndpoint_WithoutAuth_ReturnsForbidden() throws Exception {
        mockMvc.perform(post("/api/v1/admin/catalog/categories"))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(authorities = "COURIER")
    void accessAdminEndpoint_WithWrongRole_ReturnsForbidden() throws Exception {
        mockMvc.perform(post("/api/v1/admin/catalog/categories"))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(authorities = "SUPER_ADMIN")
    void accessAdminEndpoint_WithCorrectRole_DoesNotReturnForbidden() throws Exception {
        mockMvc.perform(post("/api/v1/admin/catalog/categories")
                        .contentType("application/json")
                        .content("{}"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void accessPublicEndpoint_WithoutAuth_ReturnsOk() throws Exception {
        Long traderId = 1L;
        Long parentId = 2L;
        String query = "Lorem";

        mockMvc.perform(get("/api/v1/catalog/categories")
                        .contentType(MediaType.APPLICATION_JSON)
                        .param("trader_id", String.valueOf(traderId))
                        .param("parent_id", String.valueOf(parentId))
                        .param("q", query))
                .andExpect(status().isOk());
    }
}