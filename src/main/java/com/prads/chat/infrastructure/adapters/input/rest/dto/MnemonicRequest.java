package com.prads.chat.infrastructure.adapters.input.rest.dto;

import java.util.List;

public record MnemonicRequest(
        List<String> words,
        String passphrase
) {
}