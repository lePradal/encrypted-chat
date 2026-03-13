package com.prads.chat.core.ports.output;

import com.prads.chat.infrastructure.adapters.input.rest.dto.KeyPairResponse;

public interface CryptographyPort {
    KeyPairResponse generateFullBundle(byte[] seed);

    String getFingerprint(String publicKey);

    boolean verifySignature(String userHash, String proofSignature, String publicKey);
}