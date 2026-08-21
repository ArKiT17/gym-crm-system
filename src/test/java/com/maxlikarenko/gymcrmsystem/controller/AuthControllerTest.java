package com.maxlikarenko.gymcrmsystem.controller;

import com.maxlikarenko.gymcrmsystem.dto.request.LoginRequest;
import com.maxlikarenko.gymcrmsystem.dto.response.LoginResponse;
import com.maxlikarenko.gymcrmsystem.facade.AccountFacade;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class AuthControllerTest {
    private AccountFacade accountFacade;
    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        accountFacade = mock(AccountFacade.class);
        mockMvc = org.springframework.test.web.servlet.setup.MockMvcBuilders
                .standaloneSetup(new AuthController(accountFacade))
                .setControllerAdvice(new GlobalExceptionHandler())
                .build();
    }

    @Test
    void loginAcceptsValidJsonCredentials() throws Exception {
        when(accountFacade.login(any(LoginRequest.class)))
                .thenReturn(new LoginResponse("access-token", "Bearer", 900L));

        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"username":"John.Smith","password":"password"}
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.accessToken").value("access-token"))
                .andExpect(jsonPath("$.tokenType").value("Bearer"))
                .andExpect(jsonPath("$.expiresIn").value(900));

        verify(accountFacade).login(any(LoginRequest.class));
    }

    @Test
    void loginRejectsMissingRequiredParameter() throws Exception {
        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"username":"John.Smith"}
                                """))
                .andExpect(status().isBadRequest());

        verifyNoInteractions(accountFacade);
    }

    @Test
    void loginRejectsBlankCredentials() throws Exception {
        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"username":" ","password":" "}
                                """))
                .andExpect(status().isBadRequest());

        verifyNoInteractions(accountFacade);
    }
}
