package com.prads.chat.infrastructure.adapters.output.persistence.userentity;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface UserIdentityRepository extends JpaRepository<UserIdentityEntity, String> {
    List<UserIdentityEntity> findByDisplayNameContainingIgnoreCase(String displayName);
}