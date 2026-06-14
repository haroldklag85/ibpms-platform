package com.ibpms.poc.domain.port;

/**
 * Hexagonal port for anti-malware scanning (US-004 CA-11).
 * Implementations can target ClamAV, Windows Defender, or any other scanner.
 */
public interface ClamAvScanner {

    /**
     * Scans the given file bytes for malware.
     *
     * @param fileBytes the raw file content
     * @param fileName  the original filename (for logging)
     * @return the scan result
     */
    ScanResult scan(byte[] fileBytes, String fileName);

    /**
     * Represents the outcome of a malware scan.
     */
    enum ScanResult {
        /** File is clean — no threats detected. */
        CLEAN,
        /** File is infected — malware signature matched. */
        INFECTED,
        /** Scanner service is unavailable — fail-secure. */
        UNAVAILABLE
    }
}
