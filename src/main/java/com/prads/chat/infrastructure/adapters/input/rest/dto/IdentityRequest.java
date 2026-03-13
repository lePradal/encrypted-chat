package com.prads.chat.infrastructure.adapters.input.rest.dto;

public record IdentityRequest(
        String userHash,
        String publicKey,
        String proofSignature,
        String displayName
) {
}