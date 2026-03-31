package com.prads.chat.utils;

public class ConstantUtils {
    public static final String USER_HASH_REGEX = "^0x([a-fA-F0-9]{40}|[a-fA-F0-9]{64})$";
    public static final String PGP_PUBLIC_KEY_REGEX = "(?s).*?-----BEGIN PGP PUBLIC KEY BLOCK-----.*?-----END PGP PUBLIC KEY BLOCK-----\\s*$";
    public static final String PGP_SIGNATURE_REGEX = "(?s).*?-----BEGIN PGP SIGNATURE-----.*?-----END PGP SIGNATURE-----\\s*$";
}
