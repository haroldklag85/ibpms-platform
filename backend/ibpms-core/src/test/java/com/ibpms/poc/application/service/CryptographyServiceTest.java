package com.ibpms.poc.application.service;

import org.bouncycastle.bcpg.ArmoredOutputStream;
import org.bouncycastle.bcpg.HashAlgorithmTags;
import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.bouncycastle.openpgp.*;

import org.bouncycastle.openpgp.operator.jcajce.*;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.io.ByteArrayOutputStream;

import java.nio.charset.StandardCharsets;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.Security;
import java.util.Date;

import static org.junit.jupiter.api.Assertions.*;

class CryptographyServiceTest {

    private final CryptographyService cryptographyService = new CryptographyService();

    @BeforeAll
    static void setup() {
        if (Security.getProvider(BouncyCastleProvider.PROVIDER_NAME) == null) {
            Security.addProvider(new BouncyCastleProvider());
        }
    }

    // ── QA Instruction: Test PGP Encryption Engine ──
    @Test
    @DisplayName("Debe recibir un JSON en texto plano y encriptarlo en un Bloque PGP Armored indescifrable en tránsito")
    void testPgpEncryption_ReturnsValidArmorBlock() throws Exception {
        // 1. Arrange: Generar llave temporal en memoria para test
        KeyPairGenerator kpg = KeyPairGenerator.getInstance("RSA", "BC");
        kpg.initialize(2048);
        KeyPair kp = kpg.generateKeyPair();

        PGPKeyPair pgpKp = new JcaPGPKeyPair(PGPPublicKey.RSA_GENERAL, kp, new Date());
        PGPKeyRingGenerator keyRingGen = new PGPKeyRingGenerator(
                PGPSignature.POSITIVE_CERTIFICATION, pgpKp, "test@ibpms.com", null, null, null,
                new JcaPGPContentSignerBuilder(pgpKp.getPublicKey().getAlgorithm(), HashAlgorithmTags.SHA1),
                new JcePBESecretKeyEncryptorBuilder(PGPEncryptedData.AES_256).setProvider("BC")
                        .build("password".toCharArray()));

        PGPPublicKeyRing pubRing = keyRingGen.generatePublicKeyRing();
        ByteArrayOutputStream pubOut = new ByteArrayOutputStream();
        try (ArmoredOutputStream armorOut = new ArmoredOutputStream(pubOut)) {
            pubRing.encode(armorOut);
        }
        String publicKeyAscii = pubOut.toString(StandardCharsets.UTF_8);

        String rawJson = "{\"status\": \"CONFIDENTIAL\", \"amount\": 1000000}";

        // 2. Act: Llamar al servicio iBPMS
        String encryptedArmoredPayload = cryptographyService.encryptPayloadPgp(rawJson, publicKeyAscii);

        // 3. Assert
        assertNotNull(encryptedArmoredPayload);
        assertTrue(encryptedArmoredPayload.contains("-----BEGIN PGP MESSAGE-----"));
        assertFalse(encryptedArmoredPayload.contains("CONFIDENTIAL")); // Verify it's actually encrypted
    }
}
