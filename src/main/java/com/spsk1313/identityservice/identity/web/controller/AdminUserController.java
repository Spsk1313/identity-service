package com.spsk1313.identityservice.identity.web.controller;

import com.spsk1313.identityservice.identity.application.port.in.AdminUserReader;
import com.spsk1313.identityservice.identity.application.port.in.UserDisabler;
import com.spsk1313.identityservice.identity.application.port.in.UserRoleAssigner;
import com.spsk1313.identityservice.identity.application.port.in.UserSessionRevoker;
import com.spsk1313.identityservice.identity.application.result.AdminUserResult;
import com.spsk1313.identityservice.identity.web.request.AssignUserRoleRequest;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/admin/users")
public class AdminUserController {

    private final AdminUserReader adminUserReader;
    private final UserDisabler userDisabler;
    private final UserSessionRevoker userSessionRevoker;
    private final UserRoleAssigner userRoleAssigner;

    public AdminUserController(
            AdminUserReader adminUserReader,
            UserDisabler userDisabler,
            UserSessionRevoker userSessionRevoker,
            UserRoleAssigner userRoleAssigner
    ) {
        this.adminUserReader = adminUserReader;
        this.userDisabler = userDisabler;
        this.userSessionRevoker = userSessionRevoker;
        this.userRoleAssigner = userRoleAssigner;
    }

    @GetMapping("/{userId}")
    @PreAuthorize("hasAuthority('USER_READ')")
    public ResponseEntity<AdminUserResult> getUser(
            @PathVariable Long userId
    ) {
        AdminUserResult result =
                adminUserReader.getById(userId);

        return ResponseEntity.ok(result);
    }

    @PatchMapping("/{userId}/disable")
    @PreAuthorize("hasAuthority('USER_DISABLE')")
    public ResponseEntity<Void> disableUser(
            @PathVariable Long userId
    ) {
        userDisabler.disable(userId);

        return ResponseEntity.noContent().build();
    }

    @PostMapping("/{userId}/sessions/revoke")
    @PreAuthorize("hasAuthority('SESSION_REVOKE')")
    public ResponseEntity<Void> revokeAllSessions(
            @PathVariable Long userId
    ) {
        userSessionRevoker.revokeAll(userId);

        return ResponseEntity.noContent().build();
    }

    @PostMapping("/{userId}/roles")
    @PreAuthorize("hasAuthority('ROLE_ASSIGN')")
    public ResponseEntity<Void> assignRole(
            @PathVariable Long userId,
            @Valid @RequestBody AssignUserRoleRequest request
    ) {
        userRoleAssigner.assign(
                userId,
                request.role()
        );

        return ResponseEntity.noContent().build();
    }
}