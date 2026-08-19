package com.spsk1313.identityservice.identity.web.controller;

import com.spsk1313.identityservice.identity.application.command.RegisterUserCommand;
import com.spsk1313.identityservice.identity.application.result.RegisterUserResult;
import com.spsk1313.identityservice.identity.application.service.RegisterUserService;
import com.spsk1313.identityservice.identity.application.service.VerifyEmailService;
import com.spsk1313.identityservice.identity.web.request.RegisterUserRequest;
import com.spsk1313.identityservice.identity.web.response.RegisterUserResponse;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
@Validated
public class AuthController {

    private final RegisterUserService registerUserService;
    private final VerifyEmailService verifyEmailService;

    public AuthController(
            RegisterUserService registerUserService,
            VerifyEmailService verifyEmailService) {
        this.registerUserService = registerUserService;
        this.verifyEmailService = verifyEmailService;
    }

    @PostMapping("/register")
    public ResponseEntity<RegisterUserResponse> registerUser(@Valid @RequestBody RegisterUserRequest req) {
        RegisterUserCommand command = new RegisterUserCommand(req.email(), req.password());
        RegisterUserResult result = registerUserService.register(command);
        RegisterUserResponse response = new RegisterUserResponse(result.id(), result.email(), result.emailVerified(), result.accountStatus());
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @PostMapping("/verify-email")
    public ResponseEntity<Void> verifyEmail(
            @RequestParam String token
    ) {
        verifyEmailService.verify(token);
        return ResponseEntity.noContent().build();
    }

}
