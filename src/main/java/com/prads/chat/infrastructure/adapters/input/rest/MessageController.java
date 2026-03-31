package com.prads.chat.infrastructure.adapters.input.rest;

import com.prads.chat.core.model.ChatMessage;
import com.prads.chat.core.service.MessageService;
import com.prads.chat.infrastructure.adapters.input.rest.dto.MessageResponse;
import com.prads.chat.infrastructure.adapters.input.rest.dto.SendMessageRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/messages")
@RequiredArgsConstructor
public class MessageController {

    private final MessageService messageService;

    @PostMapping
    public ResponseEntity<MessageResponse> sendMessage(@RequestBody @Valid SendMessageRequest request) {
        var domain = messageService.sendMessage(
                request.senderHash(),
                request.receiverHash(),
                request.text()
        );
        return ResponseEntity.ok(mapToResponse(domain));
    }


    @GetMapping
    public ResponseEntity<List<MessageResponse>> getHistory(
            @RequestParam String user1,
            @RequestParam String user2) {

        var history = messageService.getChatHistory(user1, user2);
        var response = history.stream()
                .map(this::mapToResponse)
                .toList();

        return ResponseEntity.ok(response);
    }

    private MessageResponse mapToResponse(ChatMessage domain) {
        return new MessageResponse(
                domain.id(),
                domain.senderHash(),
                domain.receiverHash(),
                domain.content(),
                domain.sentAt(),
                domain.delivered()
        );
    }
}
