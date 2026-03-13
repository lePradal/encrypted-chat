package com.prads.chat.infrastructure.adapters.input.rest.dto;

public record KeyPairResponse(
        String userHash,
        String publicKey,
        String privateKey,
        String fingerprint,
        String proofSignature
) {
}