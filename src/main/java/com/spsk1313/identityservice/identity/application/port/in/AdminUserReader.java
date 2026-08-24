package com.spsk1313.identityservice.identity.application.port.in;

import com.spsk1313.identityservice.identity.application.result.AdminUserResult;

public interface AdminUserReader {

    AdminUserResult getById(Long userId);
}