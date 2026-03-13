package com.prads.chat.infrastructure.adapters.output.crypto;

import com.prads.chat.core.ports.output.CryptographyPort;
import com.prads.chat.infrastructure.adapters.input.rest.dto.KeyPairResponse;
import org.bouncycastle.bcpg.ArmoredOutputStream;
import org.bouncycastle.bcpg.HashAlgorithmTags;
import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.bouncycastle.openpgp.*;
import org.bouncycastle.openpgp.operator.PGPDigestCalculator;
import org.bouncycastle.openpgp.operator.bc.BcKeyFingerprintCalculator;
import org.bouncycastle.openpgp.operator.bc.BcPGPDigestCalculatorProvider;
import org.bouncycastle.openpgp.operator.jcajce.*;
import org.bouncycastle.util.encoders.Hex;
import org.springframework.stereotype.Component;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.SecureRandom;
import java.security.Security;
import java.util.Date;

@Component
public class BouncyCastleAdapter implements CryptographyPort {

    static {
        if (Security.getProvider("BC") == null) {
            Security.addProvider(new BouncyCastleProvider());
        }
    }

    @Override
    public KeyPairResponse generateFullBundle(byte[] seed) {
        try {
            SecureRandom random = SecureRandom.getInstance("SHA1PRNG");
            random.setSeed(seed);

            KeyPairGenerator kpg = KeyPairGenerator.getInstance("RSA", "BC");
            kpg.initialize(2048, random);
            KeyPair kp = kpg.generateKeyPair();

            Date fixedDate = new Date(1704067200000L);

            PGPKeyPair pgpKeyPair = new JcaPGPKeyPair(PGPPublicKey.RSA_GENERAL, kp, fixedDate);

            PGPDigestCalculator sha1Calc = new BcPGPDigestCalculatorProvider().get(HashAlgorithmTags.SHA1);

            PGPKeyRingGenerator keyRingGen = new PGPKeyRingGenerator(PGPSignature.POSITIVE_CERTIFICATION, pgpKeyPair, "user@chat.local", sha1Calc, null, null, new JcaPGPContentSignerBuilder(pgpKeyPair.getPublicKey().getAlgorithm(), HashAlgorithmTags.SHA256), new JcePBESecretKeyEncryptorBuilder(PGPEncryptedData.AES_256).setProvider("BC").build("".toCharArray()));

            PGPPublicKey publicKey = keyRingGen.generatePublicKeyRing().getPublicKey();
            String publicKeyArmor = exportToArmor(publicKey, true);

            String fingerprint = Hex.toHexString(publicKey.getFingerprint()).toUpperCase();
            String userHash = "0x" + fingerprint;

            String proofSignature = generateProofSignature(userHash, keyRingGen.generateSecretKeyRing().getSecretKey());

            return new KeyPairResponse(userHash, publicKeyArmor, exportToArmor(keyRingGen.generateSecretKeyRing().getSecretKey(), false), fingerprint, proofSignature);

        } catch (Exception e) {
            throw new RuntimeException("Erro na geração determinística", e);
        }
    }

    @Override
    public String getFingerprint(String publicKeyArmor) {
        try {
            InputStream in = new ByteArrayInputStream(publicKeyArmor.getBytes(StandardCharsets.UTF_8));
            InputStream decoderStream = PGPUtil.getDecoderStream(in);

            PGPPublicKeyRing publicKeyRing = new PGPPublicKeyRing(decoderStream, new BcKeyFingerprintCalculator());
            PGPPublicKey publicKey = publicKeyRing.getPublicKey();

            return "0x" + Hex.toHexString(publicKey.getFingerprint()).toUpperCase();
        } catch (Exception e) {
            throw new RuntimeException("Erro ao extrair fingerprint", e);
        }
    }

    @Override
    public boolean verifySignature(String data, String signatureArmor, String publicKeyArmor) {
        try {
            InputStream keyIn = PGPUtil.getDecoderStream(new ByteArrayInputStream(publicKeyArmor.getBytes()));
            PGPPublicKeyRing pubRing = new PGPPublicKeyRing(keyIn, new BcKeyFingerprintCalculator());
            PGPPublicKey publicKey = pubRing.getPublicKey();

            InputStream sigIn = PGPUtil.getDecoderStream(new ByteArrayInputStream(signatureArmor.getBytes()));
            PGPSignatureList sigList = (PGPSignatureList) new PGPObjectFactory(sigIn, new BcKeyFingerprintCalculator()).nextObject();
            PGPSignature sig = sigList.get(0);

            sig.init(new JcaPGPContentVerifierBuilderProvider().setProvider("BC"), publicKey);
            sig.update(data.getBytes(StandardCharsets.UTF_8));

            return sig.verify();
        } catch (Exception e) {
            return false;
        }
    }

    private String exportToArmor(Object pgpKey, boolean isPublic) {
        try (ByteArrayOutputStream out = new ByteArrayOutputStream(); ArmoredOutputStream armoredOut = new ArmoredOutputStream(out)) {

            if (isPublic && pgpKey instanceof PGPPublicKey pk) {
                pk.encode(armoredOut);
            } else if (!isPublic && pgpKey instanceof PGPSecretKey sk) {
                sk.encode(armoredOut);
            }

            armoredOut.close();
            return out.toString();
        } catch (Exception e) {
            throw new RuntimeException("Erro ao converter chave para PGP Armor", e);
        }
    }

    private String generateProofSignature(String userHash, PGPSecretKey secretKey) {
        try {
            PGPPrivateKey pgpPrivKey = secretKey.extractPrivateKey(new JcePBESecretKeyDecryptorBuilder().setProvider("BC").build("".toCharArray()));

            PGPSignatureGenerator sGen = new PGPSignatureGenerator(new JcaPGPContentSignerBuilder(secretKey.getPublicKey().getAlgorithm(), HashAlgorithmTags.SHA256));
            sGen.init(PGPSignature.BINARY_DOCUMENT, pgpPrivKey);

            ByteArrayOutputStream out = new ByteArrayOutputStream();
            try (ArmoredOutputStream armoredOut = new ArmoredOutputStream(out)) {
                sGen.update(userHash.getBytes(StandardCharsets.UTF_8));
                sGen.generate().encode(armoredOut);
            }

            return out.toString();
        } catch (Exception e) {
            throw new RuntimeException("Erro ao gerar proofSignature", e);
        }
    }
}