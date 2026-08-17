package com.spsk1313.identityservice.identity.application.port.out;

import com.spsk1313.identityservice.identity.domain.EmailAddress;
import com.spsk1313.identityservice.identity.domain.User;

public interface UserRepository {
    boolean existsByEmail(EmailAddress email);

    User save(User user);
}
