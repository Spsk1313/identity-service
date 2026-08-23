package com.spsk1313.identityservice.identity.infrastructure.security.jwt;

import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
class TestAuthorizationController {

    @GetMapping("/test/security/user")
    @PreAuthorize("hasRole('USER')")
    String userOnly() {
        return "user";
    }

    @GetMapping("/test/security/admin")
    @PreAuthorize("hasRole('ADMIN')")
    String adminOnly() {
        return "admin";
    }

    @GetMapping("/test/security/user-disable")
    @PreAuthorize("hasAuthority('USER_DISABLE')")
    String userDisable() {
        return "user-disable";
    }
}