package com.maxlikarenko.gymcrmsystem.controller;

import com.maxlikarenko.gymcrmsystem.dto.request.ChangeLoginRequest;
import com.maxlikarenko.gymcrmsystem.dto.request.UserActivationRequest;
import com.maxlikarenko.gymcrmsystem.facade.AccountFacade;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/user")
@Tag(name = "User account", description = "Password and activation management")
public class UserController {
    private final AccountFacade accountFacade;

    public UserController(AccountFacade accountFacade) {
        this.accountFacade = accountFacade;
    }

    @PutMapping("/{username}/password")
    @Operation(summary = "Change password", description = "Changes the password for the specified user.")
    public ResponseEntity<Void> changePassword(
            @PathVariable String username,
            @Valid @RequestBody ChangeLoginRequest request) {
        accountFacade.changePassword(username, request);
        return ResponseEntity.ok().build();
    }

    @PatchMapping("/{username}/status")
    @Operation(summary = "Change activation status", description = "Activates or deactivates the specified user.")
    public ResponseEntity<Void> activate(@PathVariable String username, @Valid @RequestBody UserActivationRequest request) {
        accountFacade.activateUser(username, request.active());
        return ResponseEntity.ok().build();
    }
}
