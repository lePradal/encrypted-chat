package com.prads.chat.infrastructure.adapters.input.rest;

import com.prads.chat.core.model.UserIdentity;
import com.prads.chat.core.service.UserIdentityService;
import com.prads.chat.infrastructure.adapters.input.rest.dto.IdentityRequest;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Size;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/identity")
@RequiredArgsConstructor
public class UserIdentityController {
    private final UserIdentityService userIdentityService;

    @PostMapping
    public ResponseEntity<UserIdentity> createUserIdentity(@RequestBody @Valid IdentityRequest request) {
        return ResponseEntity.ok(userIdentityService.registerUser(request));
    }

    @GetMapping
    public ResponseEntity<List<UserIdentity>> getUserIdentities(@RequestParam @Size(min = 3, message = "Display name must be min 3 characters long") String displayName) {
        return ResponseEntity.ok(userIdentityService.searchIdentities(displayName));
    }

    @GetMapping("/{hash}")
    public ResponseEntity<UserIdentity> findByHash(@PathVariable String hash) {
        return ResponseEntity.ok(userIdentityService.findByHash(hash));
    }
}
