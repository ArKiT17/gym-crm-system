package com.maxlikarenko.gymcrmsystem.facade;

import com.maxlikarenko.gymcrmsystem.dto.request.ChangeLoginRequest;
import com.maxlikarenko.gymcrmsystem.dto.request.LoginRequest;
import com.maxlikarenko.gymcrmsystem.service.AuthenticationService;
import com.maxlikarenko.gymcrmsystem.service.UserAccountService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class AccountFacade {
    private final AuthenticationService authenticationService;
    private final UserAccountService userAccountService;

    @Autowired
    public AccountFacade(AuthenticationService authenticationService, UserAccountService userAccountService) {
        this.authenticationService = authenticationService;
        this.userAccountService = userAccountService;
    }

    public boolean login(LoginRequest request) {
        return authenticationService.authenticate(request.username(), request.password());
    }

    public void changePassword(String username, ChangeLoginRequest request) {
        log.info("Facade request to change password for user {}", username);
        userAccountService.changePassword(username, request.newPassword());
    }

    public void activateUser(String username, boolean activated) {
        log.info("Facade request to {} user {}", activated ? "activate" : "deactivate", username);
        userAccountService.activate(username, activated);
    }
}
