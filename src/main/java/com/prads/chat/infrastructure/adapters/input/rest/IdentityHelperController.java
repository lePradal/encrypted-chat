package com.prads.chat.infrastructure.adapters.input.rest;

import com.prads.chat.core.service.IdentityService;
import com.prads.chat.infrastructure.adapters.input.rest.dto.KeyPairResponse;
import com.prads.chat.infrastructure.adapters.input.rest.dto.MnemonicRequest;
import com.prads.chat.infrastructure.adapters.input.rest.dto.MnemonicResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/helpers")
@RequiredArgsConstructor
public class IdentityHelperController {
    private final IdentityService identityService;

    @PostMapping("/generate-mnemonic")
    public ResponseEntity<MnemonicResponse> generate(@RequestHeader(defaultValue = "12") int size) {
        return ResponseEntity.ok(identityService.createRandomMnemonic(size));
    }

    @PostMapping("/generate-bundle")
    public ResponseEntity<KeyPairResponse> generate(@RequestBody MnemonicRequest request) {
        return ResponseEntity.ok(identityService.generateBundle(request.words(), request.passphrase()));
    }
}
