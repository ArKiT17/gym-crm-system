package com.maxlikarenko.gymcrmsystem.facade;

import com.maxlikarenko.gymcrmsystem.dto.request.ChangeLoginRequest;
import com.maxlikarenko.gymcrmsystem.dto.request.LoginRequest;
import com.maxlikarenko.gymcrmsystem.dto.response.LoginResponse;
import com.maxlikarenko.gymcrmsystem.security.JwtService;
import com.maxlikarenko.gymcrmsystem.service.AuthenticationService;
import com.maxlikarenko.gymcrmsystem.service.UserAccountService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class AccountFacade {
    private final AuthenticationService authenticationService;
    private final UserAccountService userAccountService;
    private final JwtService jwtService;

    public AccountFacade(
            AuthenticationService authenticationService,
            UserAccountService userAccountService,
            JwtService jwtService
    ) {
        this.authenticationService = authenticationService;
        this.userAccountService = userAccountService;
        this.jwtService = jwtService;
    }

    public LoginResponse login(LoginRequest request) {
        Authentication authentication = authenticationService.authenticate(request.username(), request.password());
        return new LoginResponse(
                jwtService.generateToken(authentication),
                "Bearer",
                jwtService.expirationSeconds()
        );
    }

    @PreAuthorize("#username == authentication.name")
    public void changePassword(String username, ChangeLoginRequest request) {
        log.info("Facade request to change password for user {}", username);
        userAccountService.changePassword(username, request.oldPassword(), request.newPassword());
    }

    @PreAuthorize("#username == authentication.name")
    public void activateUser(String username, boolean activated) {
        log.info("Facade request to {} user {}", activated ? "activate" : "deactivate", username);
        userAccountService.activate(username, activated);
    }
}
