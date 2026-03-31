package com.prads.chat.infrastructure.adapters.input.rest.dto;

import com.prads.chat.utils.ConstantUtils;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;

public record SendMessageRequest(
        @NotBlank(message = "Sender user hash is required")
        @Pattern(
                regexp = ConstantUtils.USER_HASH_REGEX,
                message = "Sender user hash must be a valid 0x hex string (40 or 64 chars)"
        )
        String senderHash,

        @NotBlank(message = "Receiver user hash is required")
        @Pattern(
                regexp = ConstantUtils.USER_HASH_REGEX,
                message = "Receiver user hash must be a valid 0x hex string (40 or 64 chars)"
        )
        String receiverHash,
        String text
) {
}
