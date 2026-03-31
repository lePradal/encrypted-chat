package com.prads.chat.core.ports.output;

import com.prads.chat.core.model.UserIdentity;

import java.util.List;
import java.util.Optional;

public interface UserIdentityRepositoryPort {
    UserIdentity save(UserIdentity userIdentity);

    Optional<UserIdentity> findByHash(String userHash);

    List<UserIdentity> searchByDisplayName(String displayName);
}