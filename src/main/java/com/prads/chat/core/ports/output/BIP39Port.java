package com.prads.chat.core.ports.output;

import com.prads.chat.infrastructure.adapters.input.rest.dto.MnemonicResponse;

import java.util.List;

public interface BIP39Port {
    List<String> generateMnemonic(int wordsCount);
    byte[] decodeToSeed(List<String> words, String passphrase);
}