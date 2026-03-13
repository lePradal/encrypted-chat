package com.prads.chat.infrastructure.adapters.input.rest.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record IdentityRequest(
        @NotBlank(message = "User hash is required")
        @Pattern(
                regexp = "^0x([a-fA-F0-9]{40}|[a-fA-F0-9]{64})$",
                message = "User hash must be a valid 0x hex string (40 or 64 chars)"
        )
        String userHash,

        @NotBlank(message = "Public key is required")
        @Pattern(
                regexp = "(?s).*?-----BEGIN PGP PUBLIC KEY BLOCK-----.*?-----END PGP PUBLIC KEY BLOCK-----\\s*$",
                message = "Invalid PGP Public Key format"
        )
        String publicKey,

        @NotBlank(message = "Proof signature is required")
        @Pattern(
                regexp = "(?s).*?-----BEGIN PGP SIGNATURE-----.*?-----END PGP SIGNATURE-----\\s*$",
                message = "Invalid PGP Signature format"
        )
        String proofSignature,

        @Size(min = 3, max = 50, message = "Display name must be between 3 and 50 characters long.")
        String displayName
) {
}