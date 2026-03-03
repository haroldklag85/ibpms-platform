package com.ibpms.poc.application.service;

import org.bouncycastle.bcpg.ArmoredOutputStream;
import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.bouncycastle.openpgp.*;

import org.bouncycastle.openpgp.operator.jcajce.JcaKeyFingerprintCalculator;
import org.bouncycastle.openpgp.operator.jcajce.JcePGPDataEncryptorBuilder;
import org.bouncycastle.openpgp.operator.jcajce.JcePublicKeyKeyEncryptionMethodGenerator;
import org.springframework.stereotype.Service;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.security.SecureRandom;
import java.security.Security;
import java.util.Date;

@Service
public class CryptographyService {

    static {
        if (Security.getProvider(BouncyCastleProvider.PROVIDER_NAME) == null) {
            Security.addProvider(new BouncyCastleProvider());
        }
    }

    /**
     * Encrypts a raw JSON payload using the given PGP Public Key.
     * Retuns an ASCII Armored PGP Block.
     *
     * @param payloadRaw     The raw JSON payload to encrypt.
     * @param publicKeyAscii The recipient's PGP Public Key in ASCII Armor format.
     * @return Encrypted payload in ASCII Armor format.
     */
    public String encryptPayloadPgp(String payloadRaw, String publicKeyAscii) throws Exception {
        PGPPublicKey publicKey = readPublicKey(publicKeyAscii);

        ByteArrayOutputStream encOut = new ByteArrayOutputStream();
        try (ArmoredOutputStream armoredOut = new ArmoredOutputStream(encOut)) {
            PGPEncryptedDataGenerator encGen = new PGPEncryptedDataGenerator(
                    new JcePGPDataEncryptorBuilder(PGPEncryptedData.AES_256)
                            .setWithIntegrityPacket(true)
                            .setSecureRandom(new SecureRandom())
                            .setProvider(BouncyCastleProvider.PROVIDER_NAME));

            encGen.addMethod(new JcePublicKeyKeyEncryptionMethodGenerator(publicKey)
                    .setProvider(BouncyCastleProvider.PROVIDER_NAME));

            try (OutputStream cOut = encGen.open(armoredOut, new byte[1 << 16])) {
                PGPLiteralDataGenerator literalGen = new PGPLiteralDataGenerator();
                try (OutputStream pOut = literalGen.open(cOut, PGPLiteralData.BINARY,
                        PGPLiteralData.CONSOLE, payloadRaw.getBytes(StandardCharsets.UTF_8).length, new Date())) {
                    pOut.write(payloadRaw.getBytes(StandardCharsets.UTF_8));
                }
            }
        }

        return encOut.toString(StandardCharsets.UTF_8);
    }

    private PGPPublicKey readPublicKey(String publicKeyAscii) throws Exception {
        InputStream keyIn = new ByteArrayInputStream(publicKeyAscii.getBytes(StandardCharsets.UTF_8));
        InputStream decoderStream = PGPUtil.getDecoderStream(keyIn);
        PGPPublicKeyRingCollection pgpPub = new PGPPublicKeyRingCollection(decoderStream,
                new JcaKeyFingerprintCalculator());

        for (PGPPublicKeyRing keyRing : pgpPub) {
            for (PGPPublicKey key : keyRing) {
                if (key.isEncryptionKey()) {
                    return key;
                }
            }
        }
        throw new IllegalArgumentException("No se encontró una clave de encripción en el bloque PGP provisto.");
    }
}
