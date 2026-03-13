package com.prads.chat.core.ports.output;

import com.prads.chat.core.model.UserIdentity;

import java.util.Optional;

public interface UserRepositoryPort {
    UserIdentity save(UserIdentity userIdentity);
    Optional<UserIdentity> findByHash(String userHash);
}