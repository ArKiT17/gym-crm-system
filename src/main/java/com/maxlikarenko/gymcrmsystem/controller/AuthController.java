package com.maxlikarenko.gymcrmsystem.controller;

import com.maxlikarenko.gymcrmsystem.dto.request.LoginRequest;
import com.maxlikarenko.gymcrmsystem.facade.AccountFacade;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/auth")
public class AuthController {
    private final AccountFacade accountFacade;

    @Autowired
    public AuthController(AccountFacade accountFacade) {
        this.accountFacade = accountFacade;
    }

    @GetMapping("/login")
    public ResponseEntity<Void> login(@Valid @ModelAttribute LoginRequest request) {
        accountFacade.login(request);
        return ResponseEntity.ok().build();
    }
}
