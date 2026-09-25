package com.maxlikarenko.gymcrmsystem.integration;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.security.oauth2.jose.jws.MacAlgorithm;
import org.springframework.security.oauth2.jwt.*;

import java.time.Instant;
import java.util.UUID;

import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

class AuthenticationApiIntegrationTest extends RestApiIntegrationTestSupport {
    @Autowired
    private JwtDecoder jwtDecoder;

    @Autowired
    private JwtEncoder jwtEncoder;

    @Test
    void registersAndAuthenticatesTrainee() throws Exception {
        Credentials trainee = registerTrainee("Rest", "Trainee");

        String loginResponse = mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"username":"%s","password":"%s"}
                                """.formatted(trainee.username(), trainee.password())))
                .andExpect(status().isOk())
                .andExpect(header().exists("X-Transaction-Id"))
                .andExpect(jsonPath("$.accessToken").isNotEmpty())
                .andExpect(jsonPath("$.tokenType", is("Bearer")))
                .andExpect(jsonPath("$.expiresIn", is(jwtExpirationSeconds)))
                .andReturn()
                .getResponse()
                .getContentAsString();

        String accessToken = objectMapper.readTree(loginResponse).get("accessToken").asText();
        Jwt jwt = jwtDecoder.decode(accessToken);

        assertAll(
                () -> assertEquals(trainee.username(), jwt.getSubject()),
                () -> assertNotNull(jwt.getIssuedAt()),
                () -> assertNotNull(jwt.getExpiresAt()),
                () -> assertTrue(jwt.getExpiresAt() != null && jwt.getExpiresAt().isAfter(Instant.now())),
                () -> assertNotNull(jwt.getId()),
                () -> assertTrue(jwt.getId() != null && !jwt.getId().isBlank())
        );

        mockMvc.perform(get("/api/trainees/{username}", trainee.username())
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + accessToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.username", is(trainee.username())))
                .andExpect(jsonPath("$.firstName", is("Rest")))
                .andExpect(jsonPath("$.lastName", is("Trainee")))
                .andExpect(jsonPath("$.isActive", is(true)));
    }

    @Test
    void allowsPublicTrainingTypesAndOpenApiWithoutAuthentication() throws Exception {
        mockMvc.perform(get("/api/training-types"))
                .andExpect(status().isOk());

        mockMvc.perform(get("/v3/api-docs"))
                .andExpect(status().isOk());

        mockMvc.perform(get("/swagger-ui/index.html"))
                .andExpect(status().isOk());
    }

    @Test
    void locksLoginAfterThreeFailedAttempts() throws Exception {
        Credentials trainee = registerTrainee("Brute", "Force");
        String invalidLogin = """
                {"username":"%s","password":"wrong-password"}
                """.formatted(trainee.username());

        for (int attempt = 0; attempt < 3; attempt++) {
            mockMvc.perform(post("/api/auth/login")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(invalidLogin))
                    .andExpect(status().isUnauthorized());
        }

        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(invalidLogin))
                .andExpect(status().isTooManyRequests())
                .andExpect(jsonPath("$.detail", is("Too many login attempts. Try again later.")));
    }

    @Test
    void logoutRevokesAccessToken() throws Exception {
        Credentials trainee = registerTrainee("Logout", "Trainee");

        mockMvc.perform(post("/api/auth/logout")
                        .headers(trainee.headers()))
                .andExpect(status().isNoContent());

        mockMvc.perform(get("/api/trainees/{username}", trainee.username())
                        .headers(trainee.headers()))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void logoutRequiresAuthentication() throws Exception {
        mockMvc.perform(post("/api/auth/logout"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void rejectsMalformedBearerToken() throws Exception {
        mockMvc.perform(get("/api/trainees/{username}", "protected.user")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer malformed-token"))
                .andExpect(status().isUnauthorized());

        mockMvc.perform(get("/api/trainees/{username}", "protected.user")
                        .header(HttpHeaders.AUTHORIZATION, "Basic malformed-token"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void rejectsTamperedJwt() throws Exception {
        Credentials trainee = registerTrainee("Tampered", "Token");
        String tamperedToken = trainee.accessToken().substring(0, trainee.accessToken().lastIndexOf('.') + 1)
                + "invalid-signature";

        mockMvc.perform(get("/api/trainees/{username}", trainee.username())
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + tamperedToken))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void rejectsExpiredJwt() throws Exception {
        String expiredToken = jwtEncoder.encode(
                JwtEncoderParameters.from(
                        JwsHeader.with(MacAlgorithm.HS256).build(),
                        JwtClaimsSet.builder()
                                .id(UUID.randomUUID().toString())
                                .subject("Expired.User")
                                .issuedAt(Instant.now().minusSeconds(120))
                                .expiresAt(Instant.now().minusSeconds(60))
                                .build()
                )
        ).getTokenValue();

        mockMvc.perform(get("/api/trainees/{username}", "expired.user")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + expiredToken))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void revokingOneTokenDoesNotRevokeAnotherTokenForTheSameUser() throws Exception {
        Credentials trainee = registerTrainee("Token", "Isolation");

        String secondLoginResponse = mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(
                                new LoginPayload(trainee.username(), trainee.password())
                        )))
                .andExpect(status().isOk())
                .andReturn()
                .getResponse()
                .getContentAsString();
        String secondToken = objectMapper.readTree(secondLoginResponse).get("accessToken").asText();

        assertNotEquals(trainee.accessToken(), secondToken);

        mockMvc.perform(post("/api/auth/logout")
                        .headers(trainee.headers()))
                .andExpect(status().isNoContent());

        mockMvc.perform(get("/api/trainees/{username}", trainee.username())
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + secondToken))
                .andExpect(status().isOk());
    }

    @Test
    void allowsCorsPreflightFromConfiguredOrigin() throws Exception {
        mockMvc.perform(options("/api/training-types")
                        .header("Origin", "http://localhost:3000")
                        .header("Access-Control-Request-Method", "GET")
                        .header("Access-Control-Request-Headers", "Authorization"))
                .andExpect(status().isOk())
                .andExpect(header().string("Access-Control-Allow-Origin", "http://localhost:3000"))
                .andExpect(header().string("Access-Control-Allow-Methods", org.hamcrest.Matchers.containsString("GET")))
                .andExpect(header().string("Access-Control-Allow-Headers", org.hamcrest.Matchers.containsString("Authorization")))
                .andExpect(header().string("Access-Control-Max-Age", "3600"));
    }

    @Test
    void exposesTransactionHeaderForConfiguredCorsOrigin() throws Exception {
        Credentials trainee = registerTrainee("Cors", "Response");

        mockMvc.perform(get("/api/training-types")
                        .headers(trainee.headers())
                        .header("Origin", "http://localhost:3000"))
                .andExpect(status().isOk())
                .andExpect(header().string("Access-Control-Allow-Origin", "http://localhost:3000"))
                .andExpect(header().string("Access-Control-Expose-Headers", org.hamcrest.Matchers.containsString("X-Transaction-Id")));
    }

    @Test
    void rejectsCorsPreflightFromUnconfiguredOrigin() throws Exception {
        mockMvc.perform(options("/api/training-types")
                        .header("Origin", "http://malicious.example")
                        .header("Access-Control-Request-Method", "GET"))
                .andExpect(status().isForbidden());
    }

    @Test
    void rejectsAccessToAnotherUsersProfile() throws Exception {
        Credentials trainee = registerTrainee("Owner", "Trainee");
        Credentials trainer = registerTrainer("Other", "Trainer");

        mockMvc.perform(get("/api/trainers/{username}", trainer.username())
                        .headers(trainee.headers()))
                .andExpect(status().isForbidden());
    }

    @Test
    void generatesAndPropagatesTransactionId() throws Exception {
        Credentials trainee = registerTrainee("Transaction", "Trainee");

        String firstTransactionId = mockMvc.perform(get("/api/training-types")
                        .headers(trainee.headers()))
                .andExpect(status().isOk())
                .andReturn()
                .getResponse()
                .getHeader("X-Transaction-Id");

        String propagatedTransactionId = UUID.randomUUID().toString();
        String secondTransactionId = mockMvc.perform(get("/api/training-types")
                        .headers(trainee.headers())
                        .header("X-Transaction-Id", propagatedTransactionId))
                .andExpect(status().isOk())
                .andReturn()
                .getResponse()
                .getHeader("X-Transaction-Id");

        assertTrue(firstTransactionId != null && !firstTransactionId.isBlank());
        assertEquals(propagatedTransactionId, secondTransactionId);
        assertNotEquals(firstTransactionId, secondTransactionId);
    }

    @Test
    void validatesRegistrationAndMalformedDates() throws Exception {
        mockMvc.perform(post("/api/trainees")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"firstName":"", "lastName":"Trainee"}
                                """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errors.firstName", is("First name is required")));

        mockMvc.perform(post("/api/trainees")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"firstName":"Rest", "lastName":"Trainee", "dateOfBirth":"not-a-date"}
                                """))
                .andExpect(status().isBadRequest());
    }

    private record LoginPayload(String username, String password) {
    }
}
