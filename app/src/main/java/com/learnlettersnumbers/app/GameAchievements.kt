package com.learnlettersnumbers.app

import android.content.Context

/**
 * نظام إنجازات الألعاب المحلي.
 * يعمل أوفلاين بالكامل ولا يعتمد على الشبكة.
 */
internal data class GameAchievement(
    val id: String,
    val title: String,
    val description: String,
    val icon: String,
    val target: Int
)

internal object GameAchievements {
    private const val PREFS = "professional_games_achievements_v1"

    val all: List<GameAchievement> = listOf(
        GameAchievement("first_win", "أول انتصار", "أكمل أول لعبة بنجاح", "🏆", 1),
        GameAchievement("five_streak", "سلسلة نارية", "حقق 5 إجابات صحيحة متتالية", "🔥", 5),
        GameAchievement("ten_correct", "بطل الإجابات", "حقق 10 إجابات صحيحة", "⭐", 10),
        GameAchievement("perfect_game", "العلامة الكاملة", "أنه لعبة بدقة 100%", "💯", 1),
        GameAchievement("no_mistakes", "بلا أخطاء", "أنه لعبة دون خسارة قلب", "💎", 1),
        GameAchievement("three_wins", "لاعب مثابر", "أكمل 3 ألعاب", "🎮", 3),
        GameAchievement("ten_wins", "محترف الألعاب", "أكمل 10 ألعاب", "👑", 10),
        GameAchievement("hard_win", "بطل التحدي", "أكمل مستوى الصعوبة", "🚀", 1)
    )

    private fun prefs(context: Context) =
        context.getSharedPreferences(PREFS, Context.MODE_PRIVATE)

    private fun progressKey(id: String) = "progress_$id"
    private fun unlockedKey(id: String) = "unlocked_$id"

    fun progress(context: Context, id: String): Int =
        prefs(context).getInt(progressKey(id), 0)

    fun isUnlocked(context: Context, id: String): Boolean =
        prefs(context).getBoolean(unlockedKey(id), false)

    fun unlocked(context: Context): List<GameAchievement> =
        all.filter { isUnlocked(context, it.id) }

    /** يسجل تقدمًا محدودًا بقيمة الهدف ويعيد الإنجازات التي فتحت الآن. */
    fun addProgress(context: Context, id: String, amount: Int = 1): List<GameAchievement> {
        if (amount <= 0) return emptyList()
        val achievement = all.firstOrNull { it.id == id } ?: return emptyList()
        val p = prefs(context)
        if (p.getBoolean(unlockedKey(id), false)) return emptyList()

        val old = p.getInt(progressKey(id), 0)
        val next = (old + amount).coerceAtMost(achievement.target)
        val unlockedNow = next >= achievement.target
        p.edit()
            .putInt(progressKey(id), next)
            .putBoolean(unlockedKey(id), unlockedNow)
            .apply()

        return if (unlockedNow) listOf(achievement) else emptyList()
    }

    fun recordGameFinished(
        context: Context,
        accuracy: Int,
        bestStreak: Int,
        level: Int,
        lostLives: Int
    ): List<GameAchievement> {
        val newlyUnlocked = mutableListOf<GameAchievement>()
        fun add(id: String, amount: Int = 1) {
            newlyUnlocked += addProgress(context, id, amount)
        }

        add("first_win")
        add("three_wins")
        add("ten_wins")
        if (bestStreak >= 5) add("five_streak", 5)
        if (accuracy >= 100) add("perfect_game")
        if (lostLives == 0) add("no_mistakes")
        if (level >= 3) add("hard_win")
        return newlyUnlocked.distinctBy { it.id }
    }
}
