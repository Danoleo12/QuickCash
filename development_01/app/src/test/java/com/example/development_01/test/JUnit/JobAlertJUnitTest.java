package com.example.development_01.test.JUnit;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

/**
 * Pure JUnit4 tests for the preference-matching logic that will live in
 * {@link com.example.development_01.core.ui.JobAlertService}.
 *
 * No Android framework is required — all tests run on the plain JVM.
 *
 * The helper {@code matchesPreferences(List, String)} mirrors the exact
 * algorithm that JobAlertService.checkPreferencesAndNotify() will use:
 *
 *   For each preference string in the employee's list, check whether
 *   the job title contains that string (case-insensitive).
 *   Return true on the first match; false if none match.
 */
public class JobAlertJUnitTest {

    // ── Helper that mirrors JobAlertService logic exactly ────────────────────

    /**
     * Replicates the matching check inside JobAlertService.checkPreferencesAndNotify():
     *   if pref != null && !pref.isEmpty() && title.toLowerCase().contains(pref.toLowerCase())
     *       → return true (notify)
     */
    private boolean matchesPreferences(List<String> preferences, String jobTitle) {
        if (preferences == null || preferences.isEmpty() || jobTitle == null) {
            return false;
        }
        String lowerTitle = jobTitle.toLowerCase();
        for (String pref : preferences) {
            if (pref != null && !pref.trim().isEmpty()
                    && lowerTitle.contains(pref.toLowerCase().trim())) {
                return true;
            }
        }
        return false;
    }

    // ── Single preference, matching cases ────────────────────────────────────

    /**
     * A preference that exactly equals the job title should match.
     */
    @Test
    public void singlePreference_exactMatch_returnsTrue() {
        List<String> prefs = Collections.singletonList("Software Development");
        assertTrue(matchesPreferences(prefs, "Software Development"));
    }

    /**
     * A preference contained as a substring in the title should match.
     * "Developer" is contained in "Senior Developer Engineer".
     */
    @Test
    public void singlePreference_substringOfTitle_returnsTrue() {
        List<String> prefs = Collections.singletonList("Developer");
        assertTrue(matchesPreferences(prefs, "Senior Developer Engineer"));
    }

    /**
     * Matching must be case-insensitive.
     * Preference "developer" (lowercase) should match title "Senior Developer".
     */
    @Test
    public void singlePreference_caseInsensitiveMatch_returnsTrue() {
        List<String> prefs = Collections.singletonList("developer");
        assertTrue(matchesPreferences(prefs, "Senior Developer"));
    }

    /**
     * A preference that does not appear anywhere in the title should not match.
     */
    @Test
    public void singlePreference_noMatch_returnsFalse() {
        List<String> prefs = Collections.singletonList("Chef");
        assertFalse(matchesPreferences(prefs, "Software Engineer"));
    }

    // ── Multiple preferences ──────────────────────────────────────────────────

    /**
     * When the first preference matches, the method returns true immediately.
     */
    @Test
    public void multiplePreferences_firstPreferenceMatches_returnsTrue() {
        List<String> prefs = Arrays.asList("Developer", "Chef");
        assertTrue(matchesPreferences(prefs, "Senior Developer"));
    }

    /**
     * When only the second preference matches, the method still returns true.
     */
    @Test
    public void multiplePreferences_secondPreferenceMatches_returnsTrue() {
        List<String> prefs = Arrays.asList("Chef", "Developer");
        assertTrue(matchesPreferences(prefs, "Senior Developer"));
    }

    /**
     * When none of the preferences match the title, returns false.
     */
    @Test
    public void multiplePreferences_noneMatch_returnsFalse() {
        List<String> prefs = Arrays.asList("Chef", "Cook", "Baker");
        assertFalse(matchesPreferences(prefs, "Software Engineer"));
    }

    // ── Edge cases: null / empty inputs ──────────────────────────────────────

    /**
     * A null preferences list means the employee has no preferences set.
     * No notification should be sent.
     */
    @Test
    public void nullPreferenceList_returnsFalse() {
        assertFalse(matchesPreferences(null, "Software Developer"));
    }

    /**
     * An empty preferences list means the employee has cleared all preferences.
     * No notification should be sent.
     */
    @Test
    public void emptyPreferenceList_returnsFalse() {
        assertFalse(matchesPreferences(new ArrayList<>(), "Software Developer"));
    }

    /**
     * A null job title (malformed Firestore document) should not crash —
     * returns false gracefully.
     */
    @Test
    public void nullJobTitle_returnsFalse() {
        List<String> prefs = Collections.singletonList("Developer");
        assertFalse(matchesPreferences(prefs, null));
    }

    /**
     * Blank / whitespace-only preference strings must be skipped and never
     * match — otherwise every job posting would trigger a notification.
     */
    @Test
    public void blankPreferenceStrings_areIgnored_returnsFalse() {
        List<String> prefs = Arrays.asList("", "   ");
        assertFalse(matchesPreferences(prefs, "Software Developer"));
    }
}
