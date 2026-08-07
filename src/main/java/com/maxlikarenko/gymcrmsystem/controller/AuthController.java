package com.maxlikarenko.gymcrmsystem.controller;

import com.maxlikarenko.gymcrmsystem.dto.request.LoginRequest;
import com.maxlikarenko.gymcrmsystem.facade.AccountFacade;
import jakarta.validation.Valid;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/auth")
@Tag(name = "Authentication", description = "Authentication endpoints")
public class AuthController {
    private final AccountFacade accountFacade;

    @Autowired
    public AuthController(AccountFacade accountFacade) {
        this.accountFacade = accountFacade;
    }

    @GetMapping("/login")
    @Operation(summary = "Login", description = "Validates username and password and returns HTTP 200 on success.")
    public ResponseEntity<Void> login(@Valid @ModelAttribute LoginRequest request) {
        accountFacade.login(request);
        return ResponseEntity.ok().build();
    }
}
