package com.prads.chat.infrastructure.adapters.output.persistence;

import jakarta.persistence.*;
import lombok.*;

import java.time.Instant;

@Entity
@Table(name = "users")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserIdentityEntity {

    @Id
    @Column(name = "user_hash", length = 64)
    private String userHash;

    @Column(name = "public_key", columnDefinition = "TEXT", nullable = false)
    private String publicKey;

    @Column(name = "display_name")
    private String displayName;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @Column(name = "last_activity")
    private Instant lastActivity;

    @Column(name = "is_revoked")
    private boolean isRevoked = false;

    @PrePersist
    protected void onCreate() {
        this.createdAt = Instant.now();
        this.lastActivity = Instant.now();
    }
}