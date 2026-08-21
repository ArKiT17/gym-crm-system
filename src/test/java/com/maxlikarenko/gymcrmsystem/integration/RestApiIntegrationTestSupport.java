package com.maxlikarenko.gymcrmsystem.integration;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.maxlikarenko.gymcrmsystem.config.TransactionLoggingFilter;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import org.junit.jupiter.api.BeforeEach;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.context.WebApplicationContext;

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

    @Value("${security.jwt.expiration-seconds}")
    protected int jwtExpirationSeconds;

    @BeforeEach
    void setUpMockMvc() {
        mockMvc = MockMvcBuilders.webAppContextSetup(webApplicationContext)
                .addFilters(transactionLoggingFilter)
                .apply(SecurityMockMvcConfigurers.springSecurity())
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
        return authenticate(credentials(response));
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
        return authenticate(credentials(response));
    }

    private Credentials credentials(String response) throws Exception {
        JsonNode json = objectMapper.readTree(response);
        return new Credentials(json.get("username").asText(), json.get("password").asText(), null);
    }

    private Credentials authenticate(Credentials credentials) throws Exception {
        String response = mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(new LoginPayload(
                                credentials.username(), credentials.password()
                        ))))
                .andExpect(status().isOk())
                .andReturn()
                .getResponse()
                .getContentAsString();
        return new Credentials(
                credentials.username(),
                credentials.password(),
                objectMapper.readTree(response).get("accessToken").asText()
        );
    }

    private record LoginPayload(String username, String password) {
    }

    protected record Credentials(String username, String password, String accessToken) {
        protected HttpHeaders headers() {
            HttpHeaders headers = new HttpHeaders();
            headers.setBearerAuth(accessToken);
            return headers;
        }
    }
}
