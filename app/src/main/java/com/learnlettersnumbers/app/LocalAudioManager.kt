package com.learnlettersnumbers.app

import android.content.Context
import android.media.AudioAttributes
import android.media.MediaPlayer
import android.os.Bundle
import android.speech.tts.TextToSpeech
import android.speech.tts.UtteranceProgressListener
import android.speech.tts.Voice
import java.util.Locale

/**
 * Local-only audio engine.
 *
 * Normal app sounds remain bundled in res/raw. Stories can additionally use an
 * Android embedded TTS voice, but only when that voice explicitly reports that
 * it does NOT require a network connection. No network TTS is ever selected.
 */
class LocalAudioManager(private val context: Context) : TextToSpeech.OnInitListener {
    private var player: MediaPlayer? = null
    private var queue: List<Int> = emptyList()
    private var queueIndex = 0
    private var tts: TextToSpeech? = null
    private var ttsReady = false
    private var ttsArabicVoice: Voice? = null
    private var ttsEnglishVoice: Voice? = null

    var enabled: Boolean = true
        private set

    init {
        // Android's TTS engine is used only for an already-installed embedded voice.
        // We never request or trigger a network voice/download from the app.
        tts = TextToSpeech(context.applicationContext, this)
    }

    override fun onInit(status: Int) {
        if (status != TextToSpeech.SUCCESS) return
        val engine = tts ?: return
        ttsArabicVoice = findOfflineVoice(engine, "ar")
        ttsEnglishVoice = findOfflineVoice(engine, "en")
        ttsReady = ttsArabicVoice != null || ttsEnglishVoice != null
        engine.setSpeechRate(0.88f)
        engine.setPitch(1.0f)
        engine.setOnUtteranceProgressListener(object : UtteranceProgressListener() {
            override fun onStart(utteranceId: String?) = Unit
            override fun onDone(utteranceId: String?) = Unit
            override fun onError(utteranceId: String?) = Unit
        })
    }

    private fun findOfflineVoice(engine: TextToSpeech, language: String): Voice? {
        return engine.voices
            ?.asSequence()
            ?.filter { it.locale.language.equals(language, ignoreCase = true) }
            ?.filter { !it.isNetworkConnectionRequired }
            ?.sortedWith(compareByDescending<Voice> { it.quality }.thenBy { it.latency })
            ?.firstOrNull()
    }

    fun setEnabled(value: Boolean) {
        enabled = value
        if (!value) stop()
    }

    fun playRequired(resourceName: String): Boolean {
        if (!enabled) return false

        // The existing StoriesScreen requests story_01 ... story_20.
        // For those requests, prefer the on-device embedded TTS voice.
        if (resourceName.matches(Regex("story_\\d{2}"))) {
            val index = resourceName.removePrefix("story_").toIntOrNull()
            if (index != null && speakStoryFromScreen(index)) return true
        }

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

    /** Maps common app messages to bundled audio; no dynamic network TTS is used. */
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

    /** Speaks arbitrary text only with an already-installed offline voice. */
    fun speakOffline(text: String, language: String): Boolean {
        if (!enabled || text.isBlank() || !ttsReady) return false
        val engine = tts ?: return false
        val voice = if (language == "ar") ttsArabicVoice else ttsEnglishVoice
        if (voice == null || voice.isNetworkConnectionRequired) return false

        stopMediaOnly()
        engine.stop()
        engine.setVoice(voice)
        engine.setSpeechRate(if (language == "ar") 0.84f else 0.88f)
        engine.setPitch(1.0f)
        val params = Bundle().apply {
            putString(TextToSpeech.Engine.KEY_PARAM_UTTERANCE_ID, "offline_${System.nanoTime()}")
        }
        return engine.speak(text, TextToSpeech.QUEUE_FLUSH, params, params.getString(TextToSpeech.Engine.KEY_PARAM_UTTERANCE_ID)) == TextToSpeech.SUCCESS
    }

    /**
     * Keeps StoriesScreen unchanged while replacing its story_XX playback with
     * the story's actual text. If no embedded offline voice exists, the old
     * bundled MP3 remains the safe fallback.
     */
    private fun speakStoryFromScreen(index: Int): Boolean {
        if (!ttsReady) return false
        return try {
            val holder = Class.forName("com.learnlettersnumbers.app.StoriesScreenKt")
            val fieldName = if (index <= 10) "arabicStories" else "englishStories"
            val field = holder.getDeclaredField(fieldName).apply { isAccessible = true }
            val stories = field.get(null) as? List<*> ?: return false
            val item = stories.getOrNull(if (index <= 10) index - 1 else index - 11) ?: return false
            val textField = item.javaClass.getDeclaredField("text").apply { isAccessible = true }
            val text = textField.get(item) as? String ?: return false
            speakOffline(text, if (index <= 10) "ar" else "en")
        } catch (_: Throwable) {
            false
        }
    }

    fun stop() {
        queue = emptyList()
        queueIndex = 0
        try { tts?.stop() } catch (_: Exception) {}
        stopMediaOnly()
    }

    private fun stopMediaOnly() {
        try { player?.stop() } catch (_: Exception) {}
        releasePlayer()
    }

    fun releaseAll() {
        stop()
        try { tts?.shutdown() } catch (_: Exception) {}
        tts = null
        ttsReady = false
    }

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
