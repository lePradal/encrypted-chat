package com.prads.chat.core.model;

import java.time.LocalDateTime;
import java.util.UUID;

public record ChatMessage(
        UUID id,
        String senderHash,
        String receiverHash,
        String content,
        LocalDateTime sentAt,
        boolean delivered
) {}
