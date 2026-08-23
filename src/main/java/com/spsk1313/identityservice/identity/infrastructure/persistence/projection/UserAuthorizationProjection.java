package com.spsk1313.identityservice.identity.infrastructure.persistence.projection;

public interface UserAuthorizationProjection {

    String getRoleName();

    String getPermissionName();
}