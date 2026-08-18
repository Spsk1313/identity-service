package com.spsk1313.identityservice.identity.web.request;

import jakarta.validation.constraints.*;

public record RegisterUserRequest(
        @NotBlank
        String email,
        @NotBlank
        String password
) {
}
