package com.learnlettersnumbers.app

import android.content.Context
import org.json.JSONObject

/** Local, offline progress storage. No fake values are generated. */
class ProgressRepository(context: Context) {
    private val prefs = context.getSharedPreferences("child_progress_v1", Context.MODE_PRIVATE)

    data class Snapshot(
        val childName: String = "",
        val completedLessons: Int = 0,
        val attemptedAnswers: Int = 0,
        val correctAnswers: Int = 0,
        val wrongAnswers: Int = 0,
        val stars: Int = 0,
        val earnedTitles: List<String> = emptyList(),
        val lettersLearned: Int = 0,
        val numbersLearned: Int = 0,
        val writingPracticed: Int = 0,
        val completedStage: Int = 0,
        val lastActivity: String = "لم يبدأ التعلم بعد"
    )

    fun load(): Snapshot = Snapshot(
        childName = prefs.getString("child_name", "") ?: "",
        completedLessons = prefs.getInt("completed_lessons", 0),
        attemptedAnswers = prefs.getInt("attempted_answers", 0),
        correctAnswers = prefs.getInt("correct_answers", 0),
        wrongAnswers = prefs.getInt("wrong_answers", 0),
        stars = prefs.getInt("stars", 0),
        earnedTitles = prefs.getStringSet("earned_titles", emptySet())?.toList()?.sorted() ?: emptyList(),
        lettersLearned = prefs.getInt("letters_learned", 0),
        numbersLearned = prefs.getInt("numbers_learned", 0),
        writingPracticed = prefs.getInt("writing_practiced", 0),
        completedStage = prefs.getInt("completed_stage", 0),
        lastActivity = prefs.getString("last_activity", "لم يبدأ التعلم بعد") ?: "لم يبدأ التعلم بعد"
    )

    fun recordLesson(section: String, item: String, completed: Boolean = false) {
        val editor = prefs.edit()
        editor.putString("last_activity", "$section: $item")
        if (completed) editor.putInt("completed_lessons", load().completedLessons + 1)
        editor.apply()
    }

    fun recordLetterSeen(index: Int) {
        val current = load().lettersLearned
        prefs.edit().putInt("letters_learned", maxOf(current, index + 1)).apply()
    }

    fun recordNumberSeen(value: Int) {
        val current = load().numbersLearned
        prefs.edit().putInt("numbers_learned", maxOf(current, value)).apply()
    }

    fun recordWritingPractice() {
        prefs.edit().putInt("writing_practiced", load().writingPracticed + 1).apply()
    }

    /** Call only when the app has a real answer validation result. */
    fun recordAnswer(correct: Boolean) {
        val s = load()
        prefs.edit()
            .putInt("attempted_answers", s.attemptedAnswers + 1)
            .putInt(if (correct) "correct_answers" else "wrong_answers", if (correct) s.correctAnswers + 1 else s.wrongAnswers + 1)
            .apply()
    }

    fun addTitle(title: String) {
        val current = prefs.getStringSet("earned_titles", emptySet())?.toMutableSet() ?: mutableSetOf()
        current.add(title)
        prefs.edit().putStringSet("earned_titles", current).apply()
    }

    fun addStars(amount: Int) {
        val next = (load().stars + amount).coerceAtLeast(0)
        prefs.edit().putInt("stars", next).apply()
    }

    fun setChildName(name: String) {
        prefs.edit().putString("child_name", name).apply()
    }

    fun ownedRewards(): Set<String> = prefs.getStringSet("owned_rewards", emptySet()) ?: emptySet()

    fun addOwnedReward(id: String) {
        val current = ownedRewards().toMutableSet()
        current.add(id)
        prefs.edit().putStringSet("owned_rewards", current).apply()
    }

    /** Stage completion is recorded only when the app has actually verified the requirement. */
    fun completeStage(stage: Int) {
        val current = load().completedStage
        if (stage > current && stage in 1..4) {
            prefs.edit().putInt("completed_stage", stage).apply()
        }
    }

    fun isStageUnlocked(stage: Int): Boolean {
        if (stage <= 1) return true
        val s = load()
        return when (stage) {
            2 -> s.completedStage >= 1 || s.stars >= 10
            3 -> s.completedStage >= 2 || (s.stars >= 25 && s.correctAnswers >= 5)
            4 -> s.completedStage >= 3 || (s.stars >= 50 && s.correctAnswers >= 15 && s.writingPracticed >= 3)
            else -> false
        }
    }
}
