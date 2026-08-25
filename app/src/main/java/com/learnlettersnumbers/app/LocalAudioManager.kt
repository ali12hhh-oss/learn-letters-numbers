package com.learnlettersnumbers.app

import android.content.Context
import android.media.AudioAttributes
import android.media.MediaPlayer

/**
 * Local-only audio engine. Every spoken action resolves to a real file in res/raw.
 * There is deliberately no TextToSpeech fallback.
 */
class LocalAudioManager(private val context: Context) {
    private var player: MediaPlayer? = null
    private var queue: List<Int> = emptyList()
    private var queueIndex = 0
    var enabled: Boolean = true
        private set

    fun setEnabled(value: Boolean) {
        enabled = value
        if (!value) stop()
    }

    fun playRequired(resourceName: String): Boolean {
        if (!enabled) return false
        stop()
        val id = rawId(resourceName)
        start(id, resourceName)
        return true
    }

    fun playSequence(resourceNames: List<String>): Boolean {
        if (!enabled || resourceNames.isEmpty()) return false
        stop()
        queue = resourceNames.map { rawId(it) }
        queueIndex = 0
        playQueueItem()
        return true
    }

    /** Maps common app messages to bundled audio; no dynamic TTS is used. */
    fun playSemantic(text: String, language: String): Boolean {
        if (!enabled) return false
        val lower = text.lowercase()
        val resource = when {
            lower.contains("welcome") && language == "en" -> "welcome_en"
            (lower.contains("أهلاً") || lower.contains("أهلا")) && language == "ar" -> "welcome_ar"
            lower.contains("stories") && language == "en" -> "stories_intro_en"
            lower.contains("قصص") && language == "ar" -> "stories_intro_ar"
            lower.contains("games") && language == "en" -> "games_intro_en"
            lower.contains("ألعاب") && language == "ar" -> "games_intro_ar"
            lower.contains("quiz") && language == "en" -> "quiz_intro_en"
            lower.contains("اختبار") || lower.contains("اختبارات") -> if (language == "ar") "quiz_intro_ar" else "quiz_intro_en"
            lower.contains("numbers") && language == "en" -> "numbers_intro_en"
            lower.contains("الأرقام") || lower.contains("الارقام") -> if (language == "ar") "numbers_intro_ar" else "numbers_intro_en"
            lower.contains("writing") && language == "en" -> "writing_intro_en"
            lower.contains("الكتابة") && language == "ar" -> "writing_intro_ar"
            lower.contains("reading") && language == "en" -> "reading_intro_en"
            lower.contains("القراءة") && language == "ar" -> "reading_intro_ar"
            lower.contains("correct") || lower.contains("excellent") || lower.contains("great job") || lower.contains("amazing") || lower.contains("رائع") || lower.contains("أحسنت") || lower.contains("ممتاز") -> if (language == "ar") "correct_ar" else "correct_en"
            lower.contains("wrong") || lower.contains("try again") || lower.contains("حاول") || lower.contains("خطأ") -> if (language == "ar") "wrong_ar" else "wrong_en"
            lower.contains("next") || lower.contains("التالي") -> if (language == "ar") "next_ar" else "next_en"
            lower.contains("back") || lower.contains("رجوع") -> if (language == "ar") "back_ar" else "back_en"
            else -> null
        }
        if (resource != null) return playRequired(resource)

        if (language == "en") {
            val letter = Regex("\\b([A-Za-z])\\b").find(text)?.groupValues?.getOrNull(1)?.uppercase()?.firstOrNull()
            if (letter != null) {
                val i = ('A'..'Z').indexOf(letter)
                if (i >= 0) return playRequired("en_letter_%02d_sound".format(i + 1))
            }
        } else {
            val names = listOf("ألف","باء","تاء","ثاء","جيم","حاء","خاء","دال","ذال","راء","زاي","سين","شين","صاد","ضاد","طاء","ظاء","عين","غين","فاء","قاف","كاف","لام","ميم","نون","هاء","واو","ياء")
            val i = names.indexOfFirst { text.contains(it) }
            if (i >= 0) return playRequired("ar_letter_%02d_sound".format(i + 1))
            val symbols = listOf("ا","ب","ت","ث","ج","ح","خ","د","ذ","ر","ز","س","ش","ص","ض","ط","ظ","ع","غ","ف","ق","ك","ل","م","ن","ه","و","ي")
            val match = Regex("(?:حرف|الحرف)\\s*([ابتثجحخدذرزسشصضطظعغفقكلمنهوي])").find(text)
            val symbol = match?.groupValues?.getOrNull(1) ?: text.trim().takeIf { it.length == 1 }
            val j = symbols.indexOf(symbol)
            if (j >= 0) return playRequired("ar_letter_%02d_sound".format(j + 1))
        }
        // Specific number requests use the exact bundled number file.
        parseNumber(text, language)?.let { n ->
            return playRequired(if (language == "ar") "ar_number_%03d".format(n) else "en_number_%03d".format(n))
        }
        return false
    }


    fun playOperationExample(example: NumbersExampleAudio): Boolean {
        val resources = listOf(
            "ar_number_%03d".format(example.a),
            "ar_number_%03d".format(example.b),
            "ar_number_%03d".format(example.result)
        )
        return playSequence(resources)
    }

    fun stop() {
        queue = emptyList()
        queueIndex = 0
        try { player?.stop() } catch (_: Exception) {}
        releasePlayer()
    }

    fun releaseAll() = stop()

    private fun rawId(name: String): Int {
        val id = context.resources.getIdentifier(name, "raw", context.packageName)
        check(id != 0) { "Missing local audio resource: $name" }
        return id
    }

    private fun start(id: Int, resourceName: String) {
        player = MediaPlayer.create(context, id)
            ?: error("Could not create local audio resource: $resourceName")
        player?.setAudioAttributes(
            AudioAttributes.Builder()
                .setUsage(AudioAttributes.USAGE_ASSISTANCE_ACCESSIBILITY)
                .setContentType(AudioAttributes.CONTENT_TYPE_SPEECH)
                .build()
        )
        player?.setOnCompletionListener { releasePlayer() }
        player?.start()
    }

    private fun playQueueItem() {
        if (queueIndex >= queue.size) { queue = emptyList(); return }
        val id = queue[queueIndex]
        player = MediaPlayer.create(context, id) ?: error("Could not create queued local audio")
        player?.setAudioAttributes(
            AudioAttributes.Builder()
                .setUsage(AudioAttributes.USAGE_ASSISTANCE_ACCESSIBILITY)
                .setContentType(AudioAttributes.CONTENT_TYPE_SPEECH)
                .build()
        )
        player?.setOnCompletionListener {
            releasePlayer()
            queueIndex++
            playQueueItem()
        }
        player?.start()
    }

    private fun releasePlayer() {
        player?.release()
        player = null
    }

    private fun parseNumber(text: String, language: String): Int? {
        val normalized = text.map { c ->
            when (c) {
                '٠' -> '0'; '١' -> '1'; '٢' -> '2'; '٣' -> '3'; '٤' -> '4'
                '٥' -> '5'; '٦' -> '6'; '٧' -> '7'; '٨' -> '8'; '٩' -> '9'; else -> c
            }
        }.joinToString("")
        val match = Regex("(?<!\\d)(100|[1-9]\\d?)(?!\\d)").find(normalized)
        return match?.value?.toInt()?.takeIf { it in 1..100 }
    }
}
