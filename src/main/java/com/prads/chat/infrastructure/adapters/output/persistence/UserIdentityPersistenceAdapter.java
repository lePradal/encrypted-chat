package com.prads.chat.infrastructure.adapters.output.persistence;

import com.prads.chat.core.model.UserIdentity;
import com.prads.chat.core.ports.output.UserIdentityRepositoryPort;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;

@Component
@RequiredArgsConstructor
public class UserIdentityPersistenceAdapter implements UserIdentityRepositoryPort {

    private final UserIdentityRepository repository;

    @Override
    public UserIdentity save(UserIdentity domain) {
        if (!repository.existsById(domain.getUserHash())) {
            repository.save(mapToEntity(domain));
        }

        return repository.findById(domain.getUserHash())
                .map(this::mapToDomain)
                .orElseThrow(() -> new RuntimeException("Erro ao recuperar identidade salva"));
    }

    @Override
    public Optional<UserIdentity> findByHash(String userHash) {
        return repository.findById(userHash)
                .map(entity -> new UserIdentity(
                        entity.getUserHash(),
                        entity.getPublicKey(),
                        entity.getDisplayName(),
                        entity.getCreatedAt()
                ));
    }

    @Override
    public List<UserIdentity> searchByDisplayName(String displayName) {
        return repository.findByDisplayNameContainingIgnoreCase(displayName)
                .stream()
                .map(this::mapToDomain)
                .toList();
    }

    private UserIdentity mapToDomain(UserIdentityEntity entity) {
        return new UserIdentity(
                entity.getUserHash(),
                entity.getPublicKey(),
                entity.getDisplayName(),
                entity.getCreatedAt()
        );
    }

    private UserIdentityEntity mapToEntity(UserIdentity userIdentity) {
        return UserIdentityEntity.builder()
                .userHash(userIdentity.getUserHash())
                .publicKey(userIdentity.getPublicKey())
                .displayName(userIdentity.getDisplayName())
                .build();
    }
}