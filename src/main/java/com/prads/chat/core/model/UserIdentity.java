package com.prads.chat.core.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class UserIdentity {
    String hash;
    String publicKey;
    String displayName;
    Instant createdAt;

    public UserIdentity(String hash, String publicKey, String displayName) {
        this.hash = hash;
        this.publicKey = publicKey;
        this.displayName = displayName;
    }
}