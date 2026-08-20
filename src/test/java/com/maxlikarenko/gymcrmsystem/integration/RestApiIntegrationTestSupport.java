package com.maxlikarenko.gymcrmsystem.integration;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.maxlikarenko.gymcrmsystem.config.TransactionLoggingFilter;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import org.junit.jupiter.api.BeforeEach;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;
import org.springframework.transaction.annotation.Transactional;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@ActiveProfiles("test")
@Transactional
abstract class RestApiIntegrationTestSupport {
    @Autowired
    private WebApplicationContext webApplicationContext;

    @PersistenceContext
    private EntityManager entityManager;
    @Autowired
    private TransactionLoggingFilter transactionLoggingFilter;

    protected final ObjectMapper objectMapper = new ObjectMapper();
    protected MockMvc mockMvc;

    @BeforeEach
    void setUpMockMvc() {
        mockMvc = MockMvcBuilders.webAppContextSetup(webApplicationContext)
                .addFilters(transactionLoggingFilter)
                .build();
    }

    protected void flushAndClear() {
        entityManager.flush();
        entityManager.clear();
    }

    protected Credentials registerTrainee(String firstName, String lastName) throws Exception {
        String response = mockMvc.perform(post("/api/trainees")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "firstName": "%s",
                                  "lastName": "%s",
                                  "dateOfBirth": "1990-01-01",
                                  "address": "Test address"
                                }
                                """.formatted(firstName, lastName)))
                .andExpect(status().isCreated())
                .andReturn()
                .getResponse()
                .getContentAsString();
        return credentials(response);
    }

    protected Credentials registerTrainer(String firstName, String lastName) throws Exception {
        String response = mockMvc.perform(post("/api/trainers")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "firstName": "%s",
                                  "lastName": "%s",
                                  "specializationId": 1
                                }
                                """.formatted(firstName, lastName)))
                .andExpect(status().isCreated())
                .andReturn()
                .getResponse()
                .getContentAsString();
        return credentials(response);
    }

    private Credentials credentials(String response) throws Exception {
        JsonNode json = objectMapper.readTree(response);
        return new Credentials(json.get("username").asText(), json.get("password").asText());
    }

    protected record Credentials(String username, String password) {
        protected HttpHeaders headers() {
            return new HttpHeaders();
        }
    }
}
