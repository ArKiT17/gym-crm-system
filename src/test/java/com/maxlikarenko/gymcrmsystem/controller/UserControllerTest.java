package com.maxlikarenko.gymcrmsystem.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.maxlikarenko.gymcrmsystem.dto.request.ChangeLoginRequest;
import com.maxlikarenko.gymcrmsystem.dto.request.UserActivationRequest;
import com.maxlikarenko.gymcrmsystem.facade.AccountFacade;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class UserControllerTest {
    private AccountFacade accountFacade;
    private MockMvc mockMvc;
    private ObjectMapper objectMapper;

    @BeforeEach
    void setUp() {
        accountFacade = mock(AccountFacade.class);
        mockMvc = org.springframework.test.web.servlet.setup.MockMvcBuilders
                .standaloneSetup(new UserController(accountFacade))
                .setControllerAdvice(new GlobalExceptionHandler())
                .build();
        objectMapper = new ObjectMapper();
    }

    @Test
    void changePasswordAcceptsValidRequest() throws Exception {
        mockMvc.perform(put("/api/user/John.Smith/password")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(new ChangeLoginRequest("old", "new"))))
                .andExpect(status().isOk());

        verify(accountFacade).changePassword(eq("John.Smith"), any(ChangeLoginRequest.class));
    }

    @Test
    void changePasswordRejectsBlankPassword() throws Exception {
        mockMvc.perform(put("/api/user/John.Smith/password")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"oldPassword":" ","newPassword":"new"}
                                """))
                .andExpect(status().isBadRequest());

        verifyNoInteractions(accountFacade);
    }

    @Test
    void activateAcceptsBothStatusBoundaries() throws Exception {
        mockMvc.perform(patch("/api/user/John.Smith/status")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(new UserActivationRequest(true))))
                .andExpect(status().isOk());
        mockMvc.perform(patch("/api/user/John.Smith/status")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(new UserActivationRequest(false))))
                .andExpect(status().isOk());

        verify(accountFacade).activateUser("John.Smith", true);
        verify(accountFacade).activateUser("John.Smith", false);
    }

    @Test
    void activateRejectsMissingStatus() throws Exception {
        mockMvc.perform(patch("/api/user/John.Smith/status")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}"))
                .andExpect(status().isBadRequest());

        verifyNoInteractions(accountFacade);
    }
}
