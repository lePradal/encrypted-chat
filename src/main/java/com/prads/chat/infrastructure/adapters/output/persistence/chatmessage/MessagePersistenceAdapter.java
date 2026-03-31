package com.prads.chat.infrastructure.adapters.output.persistence.chatmessage;

import com.prads.chat.core.model.ChatMessage;
import com.prads.chat.core.ports.output.MessageRepositoryPort;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@RequiredArgsConstructor
public class MessagePersistenceAdapter implements MessageRepositoryPort {

    private final JpaChatMessageRepository repository;

    @Override
    public ChatMessage save(ChatMessage domain) {
        ChatMessageEntity entity = ChatMessageEntity.builder().senderHash(domain.senderHash()).receiverHash(domain.receiverHash()).content(domain.content()).sentAt(domain.sentAt()).delivered(domain.delivered()).build();

        var saved = repository.save(entity);
        return new ChatMessage(saved.getId(), saved.getSenderHash(), saved.getReceiverHash(), saved.getContent(), saved.getSentAt(), saved.isDelivered());
    }

    @Override
    public List<ChatMessage> findChatHistory(String userHash1, String userHash2) {
        return repository.findConversation(userHash1, userHash2).stream()
                .map(e -> new ChatMessage(e.getId(), e.getSenderHash(), e.getReceiverHash(), e.getContent(), e.getSentAt(), e.isDelivered()))
                .toList();
    }
}
