package com.prads.chat.core.service;

import com.prads.chat.core.model.ChatMessage;
import com.prads.chat.core.ports.output.CryptographyPort;
import com.prads.chat.core.ports.output.MessageRepositoryPort;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class MessageService {

    private final MessageRepositoryPort messageRepositoryPort;
    private final UserIdentityService identityService;
    private final CryptographyPort cryptoPort;

    public ChatMessage sendMessage(String senderHash, String receiverHash, String plainText) {
        var receiver = identityService.findByHash(receiverHash);

        String encryptedContent = cryptoPort.encrypt(plainText, receiver.getPublicKey());

        String normalizedContent = encryptedContent.replace("\r\n", "\n");


        ChatMessage message = new ChatMessage(
                null,
                senderHash,
                receiverHash,
                normalizedContent,
                LocalDateTime.now(),
                false
        );

        return messageRepositoryPort.save(message);
    }

    public List<ChatMessage> getChatHistory(String user1, String user2) {
        return messageRepositoryPort.findChatHistory(user1, user2);
    }
}
