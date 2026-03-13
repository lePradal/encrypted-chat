package com.prads.chat.infrastructure.config;

import org.springframework.context.annotation.Configuration;

import java.security.Security;

@Configuration
public class EncryptionConfig {
    static {
        Security.addProvider(new org.bouncycastle.jce.provider.BouncyCastleProvider());
    }
}