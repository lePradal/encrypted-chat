package com.prads.chat.infrastructure.adapters.input.rest.dto;

import com.prads.chat.utils.ConstantUtils;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record IdentityRequest(
        @NotBlank(message = "User hash is required")
        @Pattern(
                regexp = ConstantUtils.USER_HASH_REGEX,
                message = "User hash must be a valid 0x hex string (40 or 64 chars)"
        )
        String userHash,

        @NotBlank(message = "Public key is required")
        @Pattern(
                regexp = ConstantUtils.PGP_PUBLIC_KEY_REGEX,
                message = "Invalid PGP Public Key format"
        )
        String publicKey,

        @NotBlank(message = "Proof signature is required")
        @Pattern(
                regexp = ConstantUtils.PGP_SIGNATURE_REGEX,
                message = "Invalid PGP Signature format"
        )
        String proofSignature,

        @Size(min = 3, max = 50, message = "Display name must be between 3 and 50 characters long.")
        String displayName
) {
}