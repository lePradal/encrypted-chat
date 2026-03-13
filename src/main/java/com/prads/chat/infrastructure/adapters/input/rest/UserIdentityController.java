package com.prads.chat.infrastructure.adapters.input.rest;

import com.prads.chat.core.model.UserIdentity;
import com.prads.chat.core.service.IdentityService;
import com.prads.chat.infrastructure.adapters.input.rest.dto.IdentityRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/identity")
@RequiredArgsConstructor
public class UserIdentityController {
    private final IdentityService identityService;

    @PostMapping
    public ResponseEntity<UserIdentity> generate(@RequestBody IdentityRequest request) {
        return ResponseEntity.ok(identityService.registerUser(request));
    }
}
