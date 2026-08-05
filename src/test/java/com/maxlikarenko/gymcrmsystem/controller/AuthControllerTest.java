package com.maxlikarenko.gymcrmsystem.controller;

import com.maxlikarenko.gymcrmsystem.dto.request.LoginRequest;
import com.maxlikarenko.gymcrmsystem.facade.AccountFacade;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
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
    void loginAcceptsValidQueryParameters() throws Exception {
        when(accountFacade.login(any(LoginRequest.class))).thenReturn(true);

        mockMvc.perform(get("/api/auth/login")
                        .param("username", "John.Smith")
                        .param("password", "password"))
                .andExpect(status().isOk());

        verify(accountFacade).login(any(LoginRequest.class));
    }

    @Test
    void loginRejectsMissingRequiredParameter() throws Exception {
        mockMvc.perform(get("/api/auth/login")
                        .param("username", "John.Smith"))
                .andExpect(status().isBadRequest());

        verifyNoInteractions(accountFacade);
    }

    @Test
    void loginRejectsBlankCredentials() throws Exception {
        mockMvc.perform(get("/api/auth/login")
                        .param("username", " ")
                        .param("password", " "))
                .andExpect(status().isBadRequest());

        verifyNoInteractions(accountFacade);
    }
}
