package com.prads.chat.core.ports.output;

import com.prads.chat.core.model.ChatMessage;

import java.util.List;

public interface MessageRepositoryPort {
    ChatMessage save(ChatMessage message);

    List<ChatMessage> findChatHistory(String userHash1, String userHash2);
}
