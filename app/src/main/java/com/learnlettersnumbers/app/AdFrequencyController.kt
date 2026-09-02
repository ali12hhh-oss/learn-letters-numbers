package com.learnlettersnumbers.app

import android.app.Activity
import android.content.Context

/** Central frequency rules for non-intrusive interstitial ads. */
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

    enum class CompletionType { STORY, GAME, TEST, MAIN_SECTION }

    fun recordStoryCompleted(): Boolean = recordCompletion(STORY_COUNT, 3)
    fun recordGameCompleted(): Boolean = recordCompletion(GAME_COUNT, 3)
    fun recordTestCompleted(): Boolean = recordCompletion(TEST_COUNT, 2)
    fun recordMainSectionTransition(): Boolean = recordCompletion(SECTION_TRANSITIONS, 4)

    fun canShowInterstitial(now: Long = System.currentTimeMillis()): Boolean =
        now - prefs.getLong(LAST_INTERSTITIAL_AT, 0L) >= MIN_GAP_MS

    fun markInterstitialShown(now: Long = System.currentTimeMillis()) {
        prefs.edit().putLong(LAST_INTERSTITIAL_AT, now).apply()
    }

    fun showAfterCompletion(
        activity: Activity,
        ads: AdMobManager,
        completionType: CompletionType
    ): Boolean {
        val thresholdReached = when (completionType) {
            CompletionType.STORY -> recordStoryCompleted()
            CompletionType.GAME -> recordGameCompleted()
            CompletionType.TEST -> recordTestCompleted()
            CompletionType.MAIN_SECTION -> recordMainSectionTransition()
        }

        if (!thresholdReached || !canShowInterstitial()) return false
        if (!ads.showInterstitial(activity)) return false

        resetCount(completionType)
        markInterstitialShown()
        return true
    }

    private fun recordCompletion(key: String, threshold: Int): Boolean {
        val count = (prefs.getInt(key, 0) + 1).coerceAtMost(threshold)
        prefs.edit().putInt(key, count).apply()
        return count >= threshold
    }

    private fun resetCount(type: CompletionType) {
        val key = when (type) {
            CompletionType.STORY -> STORY_COUNT
            CompletionType.GAME -> GAME_COUNT
            CompletionType.TEST -> TEST_COUNT
            CompletionType.MAIN_SECTION -> SECTION_TRANSITIONS
        }
        prefs.edit().putInt(key, 0).apply()
    }
}
