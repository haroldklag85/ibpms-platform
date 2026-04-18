package com.ibpms.poc.application.service;

import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
public class EventReferenceGenerator {

    /**
     * Generates a deterministic event reference code: EVT- + 6 base36 characters
     * derived from the given UUID. Total length: 10 chars.
     */
    public String generateFromId(UUID eventId) {
        if (eventId == null) {
            throw new IllegalArgumentException("EventId cannot be null");
        }

        long lsb = Math.abs(eventId.getLeastSignificantBits());
        String base36 = Long.toString(lsb, 36).toUpperCase();
        
        // Ensure exactly 6 characters by padding or truncating
        String suffix;
        if (base36.length() > 6) {
            suffix = base36.substring(base36.length() - 6);
        } else {
            suffix = String.format("%6s", base36).replace(' ', '0');
        }

        return "EVT-" + suffix;
    }
}
