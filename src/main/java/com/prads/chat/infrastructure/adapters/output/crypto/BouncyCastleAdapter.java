package com.prads.chat.infrastructure.adapters.output.crypto;

import com.prads.chat.core.ports.output.CryptographyPort;
import com.prads.chat.infrastructure.adapters.input.rest.dto.KeyPairResponse;
import lombok.extern.log4j.Log4j2;
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
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.SecureRandom;
import java.security.Security;
import java.util.Date;

@Log4j2
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

            PGPPublicKeyRing publicKeyRing = keyRingGen.generatePublicKeyRing();
            String publicKeyArmor = exportToArmor(publicKeyRing, true);

            String userHash = getFingerprint(publicKeyArmor);

            String proofSignature = generateProofSignature(userHash, keyRingGen.generateSecretKeyRing().getSecretKey());

            return new KeyPairResponse(userHash, publicKeyArmor, exportToArmor(keyRingGen.generateSecretKeyRing().getSecretKey(), false), userHash, proofSignature);

        } catch (Exception e) {
            throw new RuntimeException("Error in deterministic generation", e);
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
            throw new RuntimeException("Error extracting fingerprint", e);
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

    @Override
    public String encrypt(String plainText, String receiverPublicKeyArmor) {
        try {

            receiverPublicKeyArmor = receiverPublicKeyArmor.replace("\\n", "\n");

            InputStream keyIn = PGPUtil.getDecoderStream(new ByteArrayInputStream(receiverPublicKeyArmor.getBytes()));
            PGPPublicKeyRingCollection keyRingCollection = new PGPPublicKeyRingCollection(keyIn, new BcKeyFingerprintCalculator());

            PGPPublicKey publicKey = null;
            for (PGPPublicKeyRing ring : keyRingCollection) {
                for (PGPPublicKey key : ring) {
                    log.info("Checking key with ID: {} and algorithm: {} and encryption: {} and master key: {}", Long.toHexString(key.getKeyID()), key.getAlgorithm(), key.isEncryptionKey(), key.isMasterKey());
                    if (key.isEncryptionKey()) {
                        publicKey = key;
                        break;
                    }
                }
                if (publicKey != null) break;
            }

            if (publicKey == null) throw new IllegalArgumentException("No encryption key found.");

            ByteArrayOutputStream out = new ByteArrayOutputStream();
            try (ArmoredOutputStream armoredOut = new ArmoredOutputStream(out)) {

                PGPEncryptedDataGenerator encGen = new PGPEncryptedDataGenerator(new JcePGPDataEncryptorBuilder(PGPEncryptedData.AES_256).setWithIntegrityPacket(true).setSecureRandom(new SecureRandom()).setProvider("BC"));
                encGen.addMethod(new JcePublicKeyKeyEncryptionMethodGenerator(publicKey).setProvider("BC"));

                try (OutputStream encryptedOut = encGen.open(armoredOut, new byte[4096])) {
                    PGPCompressedDataGenerator comGen = new PGPCompressedDataGenerator(PGPCompressedData.ZIP);
                    try (OutputStream comOut = comGen.open(encryptedOut)) {
                        PGPLiteralDataGenerator lGen = new PGPLiteralDataGenerator();
                        try (OutputStream lOut = lGen.open(comOut, PGPLiteralData.BINARY, "_CONSOLE", plainText.getBytes().length, new Date())) {
                            lOut.write(plainText.getBytes(StandardCharsets.UTF_8));
                        }
                    }
                }
            }

            String content = out.toString();
            return content.replace("\r\n", "\n");
        } catch (Exception e) {
            throw new RuntimeException("Error encrypting message", e);
        }
    }

    @Override
    public String decrypt(String cipherTextArmor, String ownerPrivateKeyArmor) {
        try {
            InputStream cipherIn = PGPUtil.getDecoderStream(new ByteArrayInputStream(cipherTextArmor.getBytes(StandardCharsets.UTF_8)));
            InputStream keyIn = PGPUtil.getDecoderStream(new ByteArrayInputStream(ownerPrivateKeyArmor.getBytes(StandardCharsets.UTF_8)));

            PGPObjectFactory pgpFactory = new PGPObjectFactory(cipherIn, new BcKeyFingerprintCalculator());
            PGPEncryptedDataList encList;

            Object obj = pgpFactory.nextObject();
            if (obj instanceof PGPEncryptedDataList list) {
                encList = list;
            } else {
                encList = (PGPEncryptedDataList) pgpFactory.nextObject();
            }

            PGPSecretKeyRingCollection pgpSec = new PGPSecretKeyRingCollection(keyIn, new BcKeyFingerprintCalculator());
            PGPPublicKeyEncryptedData pbe = null;
            PGPSecretKey secretKey = null;

            for (PGPEncryptedData data : encList) {
                pbe = (PGPPublicKeyEncryptedData) data;
                secretKey = pgpSec.getSecretKey(pbe.getKeyID());
                if (secretKey != null) break;
            }

            if (secretKey == null) throw new IllegalArgumentException("Private key not found for decryption.");

            PGPPrivateKey privKey = secretKey.extractPrivateKey(new JcePBESecretKeyDecryptorBuilder().setProvider("BC").build("".toCharArray()));

            InputStream clearIn = pbe.getDataStream(new JcePublicKeyDataDecryptorFactoryBuilder().setProvider("BC").build(privKey));
            PGPObjectFactory plainFactory = new PGPObjectFactory(clearIn, new BcKeyFingerprintCalculator());

            Object message = plainFactory.nextObject();
            if (message instanceof PGPCompressedData cData) {
                plainFactory = new PGPObjectFactory(cData.getDataStream(), new BcKeyFingerprintCalculator());
                message = plainFactory.nextObject();
            }

            if (message instanceof PGPLiteralData ld) {
                return new String(ld.getInputStream().readAllBytes(), StandardCharsets.UTF_8);
            }

            throw new RuntimeException("PGP message is invalid or corrupted.");
        } catch (Exception e) {
            throw new RuntimeException("Error decrypting message", e);
        }
    }

    private String exportToArmor(Object pgpKey, boolean isPublic) {
        try (ByteArrayOutputStream out = new ByteArrayOutputStream();
             ArmoredOutputStream armoredOut = new ArmoredOutputStream(out)) {

            if (isPublic && pgpKey instanceof PGPPublicKeyRing ring) {
                ring.encode(armoredOut);
            } else if (!isPublic && pgpKey instanceof PGPSecretKey sk) {
                sk.encode(armoredOut);
            }

            armoredOut.close();
            return out.toString();
        } catch (Exception e) {
            throw new RuntimeException("Error converting key to PGP Armor", e);
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
            throw new RuntimeException("Error generating proofSignature", e);
        }
    }
}