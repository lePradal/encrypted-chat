package com.prads.chat.infrastructure.adapters.input.rest.dto;

import java.time.LocalDateTime;
import java.util.UUID;

public record MessageResponse(
        UUID id,
        String senderHash,
        String receiverHash,
        String content,
        LocalDateTime sentAt,
        boolean delivered
) {}
