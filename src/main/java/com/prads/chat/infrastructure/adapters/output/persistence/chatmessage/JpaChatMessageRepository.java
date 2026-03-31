package com.prads.chat.infrastructure.adapters.output.persistence.chatmessage;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface JpaChatMessageRepository extends JpaRepository<ChatMessageEntity, UUID> {

    @Query("SELECT m FROM ChatMessageEntity m WHERE " +
            "(m.senderHash = :u1 AND m.receiverHash = :u2) OR " +
            "(m.senderHash = :u2 AND m.receiverHash = :u1) " +
            "ORDER BY m.sentAt ASC")
    List<ChatMessageEntity> findConversation(String u1, String u2);
}
