package com.prads.chat.core.service;

import com.prads.chat.core.model.UserIdentity;
import com.prads.chat.core.ports.output.BIP39Port;
import com.prads.chat.core.ports.output.CryptographyPort;
import com.prads.chat.core.ports.output.UserRepositoryPort;
import com.prads.chat.infrastructure.adapters.input.rest.dto.IdentityRequest;
import com.prads.chat.infrastructure.adapters.input.rest.dto.KeyPairResponse;
import com.prads.chat.infrastructure.adapters.input.rest.dto.MnemonicResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class IdentityService {
    private final BIP39Port bip39Port;
    private final CryptographyPort cryptoPort;
    private final UserRepositoryPort userRepositoryPort;

    public MnemonicResponse createRandomMnemonic(int size) {
        List<String> words = bip39Port.generateMnemonic(size);
        return new MnemonicResponse(words);
    }

    public KeyPairResponse generateBundle(List<String> words, String passphrase) {
        byte[] seed = bip39Port.decodeToSeed(words, passphrase);
        return cryptoPort.generateFullBundle(seed);
    }

    public UserIdentity registerUser(IdentityRequest identityRequest) {
        String derivedHash = cryptoPort.getFingerprint(identityRequest.publicKey());
        if (!derivedHash.equalsIgnoreCase(identityRequest.userHash())) {
            throw new IllegalArgumentException("UserHash não corresponde à Chave Pública fornecida.");
        }

        boolean isOwner = cryptoPort.verifySignature(identityRequest.userHash(), identityRequest.proofSignature(), identityRequest.publicKey());
        if (!isOwner) {
            throw new SecurityException("Assinatura de prova inválida.");
        }

        UserIdentity userIdentity = new UserIdentity(identityRequest.userHash(), identityRequest.publicKey(), identityRequest.displayName());

        return userRepositoryPort.save(userIdentity);
    }
}