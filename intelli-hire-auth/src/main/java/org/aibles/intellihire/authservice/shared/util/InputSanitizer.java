package org.aibles.intellihire.authservice.shared.util;

import java.util.regex.Pattern;

public final class InputSanitizer {

    private static final Pattern HTML_TAG_PATTERN = Pattern.compile("<[^>]*>");
    private static final Pattern JAVASCRIPT_URI_PATTERN = Pattern.compile("(?i)javascript\\s*:");
    private static final Pattern URL_PATTERN = Pattern.compile("^https?://[\\w\\-]+(\\.[\\w\\-]+)+[/\\w\\-.~:/?#\\[\\]@!$&'()*+,;=%]*$");

    private InputSanitizer() {
        throw new UnsupportedOperationException("Utility class");
    }

    public static String sanitizeText(String input) {
        if (input == null) return null;
        return HTML_TAG_PATTERN.matcher(input).replaceAll("").trim();
    }

    public static String sanitizeUrl(String url) {
        if (url == null || url.isBlank()) return url;
        String trimmed = url.trim();
        if (JAVASCRIPT_URI_PATTERN.matcher(trimmed).find()) {
            return null;
        }
        return trimmed;
    }

    public static boolean isValidUrl(String url) {
        if (url == null || url.isBlank()) return true; // null/empty is valid (optional field)
        return URL_PATTERN.matcher(url.trim()).matches();
    }
}
