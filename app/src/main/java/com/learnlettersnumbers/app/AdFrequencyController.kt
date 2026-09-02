package com.learnlettersnumbers.app

import android.content.Context

/**
 * Central frequency rules for non-intrusive interstitial ads.
 * Learning screens never call this controller directly.
 */
class AdFrequencyController(context: Context) {
    private val prefs = context.getSharedPreferences("ad_frequency", Context.MODE_PRIVATE)

    companion object {
        private const val STORY_COUNT = "story_count"
        private const val GAME_COUNT = "game_count"
        private const val TEST_COUNT = "test_count"
        private const val SECTION_TRANSITIONS = "section_transitions"
        private const val LAST_INTERSTITIAL_AT = "last_interstitial_at"
        private const val MIN_GAP_MS = 5 * 60 * 1000L
    }

    fun recordStoryCompleted(): Boolean = recordAndCheck(STORY_COUNT, 3)
    fun recordGameCompleted(): Boolean = recordAndCheck(GAME_COUNT, 3)
    fun recordTestCompleted(): Boolean = recordAndCheck(TEST_COUNT, 2)

    fun recordMainSectionTransition(): Boolean = recordAndCheck(SECTION_TRANSITIONS, 4)

    fun canShowInterstitial(now: Long = System.currentTimeMillis()): Boolean =
        now - prefs.getLong(LAST_INTERSTITIAL_AT, 0L) >= MIN_GAP_MS

    fun markInterstitialShown(now: Long = System.currentTimeMillis()) {
        prefs.edit().putLong(LAST_INTERSTITIAL_AT, now).apply()
    }

    private fun recordAndCheck(key: String, threshold: Int): Boolean {
        val count = prefs.getInt(key, 0) + 1
        if (count < threshold) {
            prefs.edit().putInt(key, count).apply()
            return false
        }
        prefs.edit().putInt(key, 0).apply()
        return canShowInterstitial()
    }
}
