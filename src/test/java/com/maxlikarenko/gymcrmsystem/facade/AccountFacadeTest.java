package com.maxlikarenko.gymcrmsystem.facade;

import com.maxlikarenko.gymcrmsystem.dto.request.ChangeLoginRequest;
import com.maxlikarenko.gymcrmsystem.dto.request.LoginRequest;
import com.maxlikarenko.gymcrmsystem.dto.response.LoginResponse;
import com.maxlikarenko.gymcrmsystem.security.JwtService;
import com.maxlikarenko.gymcrmsystem.service.AuthenticationService;
import com.maxlikarenko.gymcrmsystem.service.UserAccountService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.security.core.Authentication;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.*;

class AccountFacadeTest {
    private AuthenticationService authenticationService;
    private UserAccountService userAccountService;
    private JwtService jwtService;
    private AccountFacade accountFacade;

    @BeforeEach
    void setUp() {
        authenticationService = mock(AuthenticationService.class);
        userAccountService = mock(UserAccountService.class);
        jwtService = mock(JwtService.class);
        accountFacade = new AccountFacade(authenticationService, userAccountService, jwtService);
    }

    @Test
    void loginReturnsJwtResponse() {
        LoginRequest request = new LoginRequest("John.Smith", "password");
        Authentication authentication = mock(Authentication.class);
        when(authenticationService.authenticate("John.Smith", "password")).thenReturn(authentication);
        when(jwtService.generateToken(authentication)).thenReturn("access-token");
        when(jwtService.expirationSeconds()).thenReturn(900L);

        assertEquals(
                new LoginResponse("access-token", "Bearer", 900L),
                accountFacade.login(request)
        );

        verify(authenticationService).authenticate("John.Smith", "password");
        verify(jwtService).generateToken(authentication);
        verifyNoInteractions(userAccountService);
    }

    @Test
    void changePasswordDelegatesNewPassword() {
        ChangeLoginRequest request = new ChangeLoginRequest("old", "new");

        accountFacade.changePassword("John.Smith", request);

        verify(userAccountService).changePassword("John.Smith", "old", "new");
        verifyNoInteractions(authenticationService);
    }

    @Test
    void activateUserDelegatesRequestedStatus() {
        accountFacade.activateUser("John.Smith", true);
        accountFacade.activateUser("Jane.Doe", false);

        verify(userAccountService).activate("John.Smith", true);
        verify(userAccountService).activate("Jane.Doe", false);
        verifyNoInteractions(authenticationService);
    }
}
