package com.maxlikarenko.gymcrmsystem.controller;

import com.maxlikarenko.gymcrmsystem.dto.request.ChangeLoginRequest;
import com.maxlikarenko.gymcrmsystem.dto.request.UserActivationRequest;
import com.maxlikarenko.gymcrmsystem.facade.AccountFacade;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/user")
public class UserController {
    private final AccountFacade accountFacade;

    @Autowired
    public UserController(AccountFacade accountFacade) {
        this.accountFacade = accountFacade;
    }

    @PutMapping("/{username}/password")
    public ResponseEntity<Void> changePassword(
            @PathVariable String username,
            @Valid @RequestBody ChangeLoginRequest request) {
        accountFacade.changePassword(username, request);
        return ResponseEntity.ok().build();
    }

    @PatchMapping("/{username}/status")
    public ResponseEntity<Void> activate(@PathVariable String username, @Valid @RequestBody UserActivationRequest request) {
        accountFacade.activateUser(username, request.active());
        return ResponseEntity.ok().build();
    }
}
