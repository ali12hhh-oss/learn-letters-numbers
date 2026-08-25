package com.learnlettersnumbers.app

import android.content.Context

/** Strict offline audio catalog validation. Missing/empty required audio is a development error. */
object AudioCatalogValidator {
    fun validateOrThrow(context: Context) {
        val missing = mutableListOf<String>()
        fun required(name: String) {
            if (context.resources.getIdentifier(name, "raw", context.packageName) == 0) missing += name
        }
        for (i in 1..28) {
            val n = "%02d".format(i)
            required("ar_letter_${n}_sound")
            required("ar_letter_${n}_name")
            required("ar_letter_${n}_vowel_1")
            required("ar_letter_${n}_vowel_2")
            required("ar_letter_${n}_vowel_3")
        }
        for (i in 1..26) {
            val n = "%02d".format(i)
            required("en_letter_${n}_sound")
            required("en_letter_${n}_name")
        }
        for (i in 1..100) {
            required("ar_number_%03d".format(i))
            required("en_number_%03d".format(i))
        }
        listOf(
            "welcome_ar", "welcome_en", "letters_intro_ar", "letters_intro_en",
            "numbers_intro_ar", "numbers_intro_en", "reading_intro_ar", "reading_intro_en",
            "writing_intro_ar", "writing_intro_en", "quiz_intro_ar", "quiz_intro_en",
            "stories_intro_ar", "stories_intro_en", "games_intro_ar", "games_intro_en",
            "correct_ar", "correct_en", "wrong_ar", "wrong_en", "next_ar", "next_en", "back_ar", "back_en"
        ).forEach(::required)
        for (i in 1..20) required("story_%02d".format(i))
        for (i in 1..10) {
            required("praise_ar_%02d".format(i))
            required("praise_en_%02d".format(i))
        }
        if (missing.isNotEmpty()) {
            error("Missing required local audio resources (${missing.size}): ${missing.joinToString()}")
        }
    }
}
