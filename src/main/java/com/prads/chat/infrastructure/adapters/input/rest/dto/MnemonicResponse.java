package com.prads.chat.infrastructure.adapters.input.rest.dto;

import java.util.List;

public record MnemonicResponse(
        List<String> words
) {}