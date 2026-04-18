package com.ibpms.poc.domain.port;

public interface EncryptionKeyProvider {
    /**
     * Devuelve la clave secreta en formato binario puro (AES 256 bits).
     */
    byte[] getEncryptionKey();
}
