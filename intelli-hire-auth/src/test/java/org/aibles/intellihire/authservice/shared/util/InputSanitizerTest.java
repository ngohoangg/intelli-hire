package org.aibles.intellihire.authservice.shared.util;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class InputSanitizerTest {

    @Test
    void sanitizeText_stripsHtmlTags() {
        assertThat(InputSanitizer.sanitizeText("<b>bold</b>")).isEqualTo("bold");
        assertThat(InputSanitizer.sanitizeText("<script>alert('xss')</script>")).isEqualTo("alert('xss')");
        assertThat(InputSanitizer.sanitizeText("hello <img src=x> world")).isEqualTo("hello  world");
    }

    @Test
    void sanitizeText_nullInput_returnsNull() {
        assertThat(InputSanitizer.sanitizeText(null)).isNull();
    }

    @Test
    void sanitizeText_cleanInput_returnsUnchanged() {
        assertThat(InputSanitizer.sanitizeText("John Doe")).isEqualTo("John Doe");
    }

    @Test
    void sanitizeUrl_blocksJavascriptUri() {
        assertThat(InputSanitizer.sanitizeUrl("javascript:alert('xss')")).isNull();
        assertThat(InputSanitizer.sanitizeUrl("JAVASCRIPT:alert('xss')")).isNull();
        assertThat(InputSanitizer.sanitizeUrl("  javascript : alert('xss')")).isNull();
    }

    @Test
    void sanitizeUrl_allowsHttpUrls() {
        assertThat(InputSanitizer.sanitizeUrl("https://github.com/user")).isEqualTo("https://github.com/user");
        assertThat(InputSanitizer.sanitizeUrl("http://example.com")).isEqualTo("http://example.com");
    }

    @Test
    void sanitizeUrl_nullInput_returnsNull() {
        assertThat(InputSanitizer.sanitizeUrl(null)).isNull();
    }

    @Test
    void isValidUrl_validUrls() {
        assertThat(InputSanitizer.isValidUrl("https://github.com/user")).isTrue();
        assertThat(InputSanitizer.isValidUrl("http://linkedin.com/in/user")).isTrue();
        assertThat(InputSanitizer.isValidUrl(null)).isTrue(); // null is valid (optional)
        assertThat(InputSanitizer.isValidUrl("")).isTrue(); // empty is valid (optional)
    }

    @Test
    void isValidUrl_invalidUrls() {
        assertThat(InputSanitizer.isValidUrl("not-a-url")).isFalse();
        assertThat(InputSanitizer.isValidUrl("ftp://example.com")).isFalse();
    }
}
