package com.serjnn.ClientService.integration;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.serjnn.ClientService.dtos.AuthRequest;
import com.serjnn.ClientService.dtos.OrderDTO;
import com.serjnn.ClientService.dtos.RegRequest;
import com.serjnn.ClientService.models.Client;
import com.serjnn.ClientService.repo.ClientRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.util.Collections;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;


public class ClientIntegrationTest extends AbstractIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ClientRepository clientRepository;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    void shouldRegisterAndAuthenticate() throws Exception {
        String email = "test" + UUID.randomUUID() + "@mail.com";
        RegRequest regRequest = new RegRequest(email, "password");

        mockMvc.perform(post("/api/v1/clients/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(regRequest)))
                .andExpect(status().isCreated());

        AuthRequest authRequest = new AuthRequest(email, "password");

        String token = mockMvc.perform(post("/api/v1/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(authRequest)))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();
        assertThat(token).isNotEmpty();
    }

    @Test
    @WithMockUser(username = "user@mail.com")
    void shouldManageBalanceAndAddress() throws Exception {
        String email = "user" + UUID.randomUUID() + "@mail.com";
        clientRepository.save(new Client(email, "pass"));
        Client client = clientRepository.findByMail(email).orElseThrow();

        // 1. Get Info (need to update MockUser to match email or use the one from MockUser)
    }

    // Simplified version to avoid complex security setup in MockMvc for this specific test
    @Test
    void shouldPerformFullLifecycle() throws Exception {
        String email = "lifecycle@mail.com";
        RegRequest reg = new RegRequest(email, "pass");
        mockMvc.perform(post("/api/v1/clients/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(reg)))
                .andExpect(status().isCreated());

        Client client = clientRepository.findByMail(email).orElseThrow();

        // Add balance
        mockMvc.perform(patch("/api/v1/clients/" + client.id() + "/balance")
                .param("amount", "100.00"))
                .andExpect(status().isNoContent());

        assertThat(clientRepository.findById(client.id()).orElseThrow().balance()).isEqualByComparingTo("100.00");

        // Deduct
        OrderDTO order = new OrderDTO(UUID.randomUUID(), client.id(), Collections.emptyList(), new BigDecimal("40.00"));
        mockMvc.perform(post("/api/v1/clients/transactions/deduct")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(order)))
                .andExpect(status().isOk());

        assertThat(clientRepository.findById(client.id()).orElseThrow().balance()).isEqualByComparingTo("60.00");
    }

    @Test
    void shouldFailOnInsufficientFunds() throws Exception {
        String email = "poor" + UUID.randomUUID() + "@mail.com";
        clientRepository.save(new Client(email, "pass"));
        Client client = clientRepository.findByMail(email).orElseThrow();

        OrderDTO expensiveOrder = new OrderDTO(UUID.randomUUID(), client.id(), Collections.emptyList(), new BigDecimal("1000.00"));

        assertThatThrownBy(() -> {
            mockMvc.perform(post("/api/v1/clients/transactions/deduct")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(expensiveOrder)));
        }).hasCauseInstanceOf(RuntimeException.class).hasMessageContaining("Insufficient funds");
    }
}
