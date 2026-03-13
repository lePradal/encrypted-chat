package com.prads.chat.core.service;

import com.prads.chat.core.model.UserIdentity;
import com.prads.chat.core.ports.output.BIP39Port;
import com.prads.chat.core.ports.output.CryptographyPort;
import com.prads.chat.core.ports.output.UserIdentityRepositoryPort;
import com.prads.chat.infrastructure.adapters.input.rest.dto.IdentityRequest;
import com.prads.chat.infrastructure.adapters.input.rest.dto.KeyPairResponse;
import com.prads.chat.infrastructure.adapters.input.rest.dto.MnemonicResponse;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.stereotype.Service;

import java.util.List;

@Log4j2
@Service
@RequiredArgsConstructor
public class UserIdentityService {
    private final BIP39Port bip39Port;
    private final CryptographyPort cryptoPort;
    private final UserIdentityRepositoryPort userIdentityRepositoryPort;

    public MnemonicResponse createRandomMnemonic(int size) {
        log.info("Generating random mnemonic with size: {}", size);
        List<String> words = bip39Port.generateMnemonic(size);
        return new MnemonicResponse(words);
    }

    public KeyPairResponse generateBundle(List<String> words, String passphrase) {
        log.info("Generating key pair bundle from mnemonic. Words: {}, Passphrase: {}", words, passphrase != null ? "****" : "null");
        byte[] seed = bip39Port.decodeToSeed(words, passphrase);
        return cryptoPort.generateFullBundle(seed);
    }

    public UserIdentity registerUser(IdentityRequest identityRequest) {
        log.info("Registering user with hash: {}", identityRequest.userHash());
        String derivedHash = cryptoPort.getFingerprint(identityRequest.publicKey());
        if (!derivedHash.equalsIgnoreCase(identityRequest.userHash())) {
            throw new IllegalArgumentException("UserHash does not match the provided Public Key.");
        }

        boolean isOwner = cryptoPort.verifySignature(identityRequest.userHash(), identityRequest.proofSignature(), identityRequest.publicKey());
        if (!isOwner) {
            throw new SecurityException("Invalid proof signature.");
        }

        UserIdentity userIdentity = new UserIdentity(identityRequest.userHash(), identityRequest.publicKey(), identityRequest.displayName());

        return userIdentityRepositoryPort.save(userIdentity);
    }

    public UserIdentity findByHash(String userHash) {
        log.info("Finding user identity by hash: {}", userHash);
        return userIdentityRepositoryPort.findByHash(userHash)
                .orElseThrow(() -> new EntityNotFoundException("Identity not found for the hash: " + userHash));
    }

    public List<UserIdentity> searchIdentities(String query) {
        log.info("Searching user identities by display name with query: {}", query);
        return userIdentityRepositoryPort.searchByDisplayName(query);
    }
}