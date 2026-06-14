package com.ibpms.poc.application.util;

import java.util.regex.Pattern;

public class PiiSanitizer {

    // Regex heuristico sencillo para QA
    private static final Pattern EMAIL_PATTERN = Pattern.compile("[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\\.[a-zA-Z]{2,6}");
    private static final Pattern PHONE_PATTERN = Pattern.compile("\\b(\\+\\d{1,3}[- ]?)?\\d{10}\\b");

    /**
     * Sanitiza el prompt eliminando PII antes de enviarlo al GenAI DMN Builder.
     */
    public static String sanitize(String input) {
        if (input == null) return null;
        String sanitized = EMAIL_PATTERN.matcher(input).replaceAll("[REDACTED_EMAIL]");
        sanitized = PHONE_PATTERN.matcher(sanitized).replaceAll("[REDACTED_PHONE]");
        return sanitized;
    }
}
