package com.prads.chat.infrastructure.adapters.output.bip39;

import com.prads.chat.core.ports.output.BIP39Port;
import lombok.extern.log4j.Log4j2;
import org.bitcoinj.crypto.MnemonicCode;
import org.bitcoinj.crypto.MnemonicException;
import org.springframework.stereotype.Component;

import java.security.SecureRandom;
import java.util.List;

@Log4j2
@Component
public class BitcoinJAdapter implements BIP39Port {

    @Override
    public List<String> generateMnemonic(int wordsCount) {
        int entropyBits = (wordsCount == 24) ? 256 : 128;
        byte[] entropy = new byte[entropyBits / 8];
        new SecureRandom().nextBytes(entropy);

        try {
            return MnemonicCode.INSTANCE.toMnemonic(entropy);
        } catch (MnemonicException.MnemonicLengthException e) {
            throw new RuntimeException("Erro ao gerar palavras", e);
        }
    }

    @Override
    public byte[] decodeToSeed(List<String> words, String passphrase) {
        return MnemonicCode.toSeed(words, passphrase == null ? "" : passphrase);
    }
}