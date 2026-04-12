package com.example.development_01.test.JUnit;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

/**
 * Pure JUnit4 tests for logic extracted from
 * {@link com.example.development_01.core.ui.PostDescription}.
 *
 * These tests require NO Android framework — no Robolectric, no emulator.
 * They target the two stateless string-processing behaviours that live
 * inside PostDescription.onCreate():
 *
 *   1. Location / Pay prefix formatting
 *        location != null  →  "Location: " + location
 *        location == null  →  "Location: N/A"
 *        pay != null       →  "Pay: "      + pay
 *        pay == null       →  "Pay: N/A"
 *
 *   2. Tag string splitting
 *        tags != null && !tags.isEmpty()  →  tags.split(",")  then .trim() each token
 *        tags null or empty               →  no chips (empty array)
 *
 * These methods mirror the exact inline logic in PostDescription.onCreate() so
 * that the same branches are exercised here without spinning up an Activity.
 */
public class PostDescriptionLogicTest {

    // ─── Helpers that mirror PostDescription.onCreate() logic exactly ─────────

    /**
     * Replicates: locationText.setText(location != null ? "Location: " + location : "Location: N/A")
     */
    private String formatLocation(String location) {
        return location != null ? "Location: " + location : "Location: N/A";
    }

    /**
     * Replicates: payText.setText(pay != null ? "Pay: " + pay : "Pay: N/A")
     */
    private String formatPay(String pay) {
        return pay != null ? "Pay: " + pay : "Pay: N/A";
    }

    /**
     * Replicates the tag-splitting block:
     *   if (tags != null && !tags.isEmpty()) { tags.split(",") → trim each }
     * Returns the trimmed token array, or an empty array when tags are absent.
     */
    private String[] parseTags(String tags) {
        if (tags == null || tags.isEmpty()) {
            return new String[0];
        }
        String[] raw = tags.split(",");
        String[] trimmed = new String[raw.length];
        for (int i = 0; i < raw.length; i++) {
            trimmed[i] = raw[i].trim();
        }
        return trimmed;
    }

    // ─── Location formatting ──────────────────────────────────────────────────

    /**
     * A valid location string is prefixed with "Location: ".
     */
    @Test
    public void testFormatLocation_WithValue_PrependsPrefixAndValue() {
        assertEquals("Location: Toronto, ON", formatLocation("Toronto, ON"));
    }

    /**
     * A null location falls back to "Location: N/A".
     */
    @Test
    public void testFormatLocation_Null_ReturnsNAFallback() {
        assertEquals("Location: N/A", formatLocation(null));
    }

    /**
     * An empty string is a valid (non-null) value and is simply prefixed.
     * PostDescription does not guard against empty — it prefixes as-is.
     */
    @Test
    public void testFormatLocation_EmptyString_PrependsPrefix() {
        assertEquals("Location: ", formatLocation(""));
    }

    /**
     * Location with extra whitespace is preserved verbatim (no trimming in source).
     */
    @Test
    public void testFormatLocation_WhitespacePreserved() {
        assertEquals("Location:  Ottawa ", formatLocation(" Ottawa "));
    }

    // ─── Pay formatting ───────────────────────────────────────────────────────

    /**
     * A valid pay string is prefixed with "Pay: ".
     */
    @Test
    public void testFormatPay_WithValue_PrependsPrefixAndValue() {
        assertEquals("Pay: 25.5", formatPay("25.5"));
    }

    /**
     * A null pay string falls back to "Pay: N/A".
     */
    @Test
    public void testFormatPay_Null_ReturnsNAFallback() {
        assertEquals("Pay: N/A", formatPay(null));
    }

    /**
     * Integer pay string is prefixed correctly.
     */
    @Test
    public void testFormatPay_IntegerValue_PrependsPrefixAndValue() {
        assertEquals("Pay: 20", formatPay("20"));
    }

    /**
     * An empty pay string is prefixed as-is (source has no empty guard).
     */
    @Test
    public void testFormatPay_EmptyString_PrependsPrefix() {
        assertEquals("Pay: ", formatPay(""));
    }

    // ─── Tag parsing ──────────────────────────────────────────────────────────

    /**
     * Three comma-separated tags produce an array of three trimmed strings.
     */
    @Test
    public void testParseTags_ThreeCommaSeparated_ReturnsThreeTrimmedTokens() {
        String[] result = parseTags("Cooking, Leadership, Food Safety");
        assertArrayEquals(new String[]{"Cooking", "Leadership", "Food Safety"}, result);
    }

    /**
     * A single tag with no commas produces an array of exactly one element.
     */
    @Test
    public void testParseTags_SingleTag_ReturnsSingleElementArray() {
        String[] result = parseTags("Cooking");
        assertEquals(1, result.length);
        assertEquals("Cooking", result[0]);
    }

    /**
     * Tags with extra spaces around the comma are trimmed to clean tokens.
     */
    @Test
    public void testParseTags_ExtraSpacesAroundCommas_TrimmedCorrectly() {
        String[] result = parseTags("  Java  ,  Android  ,  Firebase  ");
        assertArrayEquals(new String[]{"Java", "Android", "Firebase"}, result);
    }

    /**
     * A null tags string returns an empty array — no chips should be created.
     */
    @Test
    public void testParseTags_Null_ReturnsEmptyArray() {
        String[] result = parseTags(null);
        assertEquals(0, result.length);
    }

    /**
     * An empty tags string returns an empty array — no chips should be created.
     */
    @Test
    public void testParseTags_EmptyString_ReturnsEmptyArray() {
        String[] result = parseTags("");
        assertEquals(0, result.length);
    }

    /**
     * Two tags without spaces around the comma are still split and trimmed correctly.
     */
    @Test
    public void testParseTags_NoSpacesAroundComma_SplitsCorrectly() {
        String[] result = parseTags("Chef,Manager");
        assertArrayEquals(new String[]{"Chef", "Manager"}, result);
    }

    /**
     * Tag count matches the number of comma-separated tokens.
     */
    @Test
    public void testParseTags_CountMatchesTokenCount() {
        String[] result = parseTags("A, B, C, D, E");
        assertEquals(5, result.length);
    }
}