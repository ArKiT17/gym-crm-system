package com.maxlikarenko.gymcrmsystem.facade;

import com.maxlikarenko.gymcrmsystem.dto.request.ChangeLoginRequest;
import com.maxlikarenko.gymcrmsystem.dto.request.LoginRequest;
import com.maxlikarenko.gymcrmsystem.service.AuthenticationService;
import com.maxlikarenko.gymcrmsystem.service.UserAccountService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.*;

class AccountFacadeTest {
    private AuthenticationService authenticationService;
    private UserAccountService userAccountService;
    private AccountFacade accountFacade;

    @BeforeEach
    void setUp() {
        authenticationService = mock(AuthenticationService.class);
        userAccountService = mock(UserAccountService.class);
        accountFacade = new AccountFacade(authenticationService, userAccountService);
    }

    @Test
    void loginReturnsAuthenticationResult() {
        LoginRequest request = new LoginRequest("John.Smith", "password");
        when(authenticationService.authenticate("John.Smith", "password")).thenReturn(true);

        assertTrue(accountFacade.login(request));

        verify(authenticationService).authenticate("John.Smith", "password");
        verifyNoInteractions(userAccountService);
    }

    @Test
    void loginReturnsFalseWhenAuthenticationFails() {
        LoginRequest request = new LoginRequest("John.Smith", "wrong");
        when(authenticationService.authenticate("John.Smith", "wrong")).thenReturn(false);

        assertFalse(accountFacade.login(request));

        verify(authenticationService).authenticate("John.Smith", "wrong");
        verifyNoInteractions(userAccountService);
    }

    @Test
    void changePasswordDelegatesNewPassword() {
        ChangeLoginRequest request = new ChangeLoginRequest("old", "new");

        accountFacade.changePassword("John.Smith", request);

        verify(userAccountService).changePassword("John.Smith", "new");
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
