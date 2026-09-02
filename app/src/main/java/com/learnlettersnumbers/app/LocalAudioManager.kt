package com.learnlettersnumbers.app

import android.content.Context
import android.media.AudioAttributes
import android.media.MediaPlayer
import android.os.Bundle
import android.speech.tts.TextToSpeech
import android.speech.tts.UtteranceProgressListener
import android.speech.tts.Voice

/**
 * Offline-first audio engine.
 *
 * The tested on-device embedded Android TTS voice is the only speech path.
 * No bundled legacy audio files and no network TTS are used.
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
            @Deprecated("Required for Android TTS API compatibility.")
            override fun onError(utteranceId: String?) = Unit
        })
    }

    private fun findOfflineVoice(engine: TextToSpeech, language: String): Voice? =
        engine.voices
            ?.asSequence()
            ?.filter { it.locale.language.equals(language, ignoreCase = true) }
            ?.filter { !it.isNetworkConnectionRequired }
            ?.sortedWith(compareByDescending<Voice> { it.quality }.thenBy { it.latency })
            ?.firstOrNull()

    fun setEnabled(value: Boolean) {
        enabled = value
        if (!value) stop()
    }

    /**
     * Resolves the educational resource name to text and speaks it with the
     * tested offline Android TTS voice. Legacy bundled audio is not used.
     */
    fun playRequired(resourceName: String): Boolean {
        if (!enabled) return false

        if (resourceName.matches(Regex("story_\\d{2}"))) {
            val index = resourceName.removePrefix("story_").toIntOrNull()
            if (index != null && speakStoryFromScreen(index)) return true
        }

        // English letter SOUND only.
        // Do not send the visible letter itself to TTS: Google/Android TTS
        // interprets A, B, C... as letter names ("ay", "bee", "see").
        // Use short phonetic cues instead so the Letter Name and Letter Sound
        // paths can never be mixed.
        Regex("en_letter_(\\d{2})_sound").matchEntire(resourceName)?.let {
            val index = it.groupValues[1].toInt() - 1
            if (index in 0..25) return speakEnglishLetterSound(index)
        }

        val offline = offlineTextForResource(resourceName)
        return offline != null && speakOffline(offline.first, offline.second)
    }

    fun playSequence(resourceNames: List<String>): Boolean {
        if (!enabled || resourceNames.isEmpty()) return false
        val parts = resourceNames.mapNotNull { offlineTextForResource(it) }
        if (parts.isEmpty()) return false
        val language = parts.first().second
        return speakOffline(parts.joinToString(" ") { it.first }, language)
    }

    /** Maps common app messages to the new offline voice first. */
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
        val utteranceId = "offline_${System.nanoTime()}"
        val params = Bundle().apply {
            putString(TextToSpeech.Engine.KEY_PARAM_UTTERANCE_ID, utteranceId)
        }
        return engine.speak(text, TextToSpeech.QUEUE_FLUSH, params, utteranceId) == TextToSpeech.SUCCESS
    }

    /**
     * English A-Z Letter Sound only.
     *
     * This intentionally does NOT speak the letter character, because Android
     * TTS reads a standalone character as its letter name. TtsSpan was removed
     * here because support for phoneme substitution is engine-dependent and can
     * result in silence or the original letter name. These short cues are the
     * reliable offline path through the installed English voice and are kept
     * completely separate from the Letter Name path.
     */
    private fun speakEnglishLetterSound(index: Int): Boolean {
        if (!enabled || !ttsReady || index !in 0..25) return false
        val phoneticCues = listOf(
            "ah", "buh", "kuh", "duh", "eh", "fff", "guh", "huh", "ih", "juh",
            "kuh", "lll", "mmm", "nnn", "ah", "puh", "kwuh", "rrr", "sss", "tuh",
            "uh", "vvv", "wuh", "ks", "yuh", "zzz"
        )
        return speakOffline(phoneticCues[index], "en")
    }

    private fun offlineTextForResource(name: String): Pair<String, String>? {
        val enLetters = ('A'..'Z').toList()
        val arLetters = listOf("ا","ب","ت","ث","ج","ح","خ","د","ذ","ر","ز","س","ش","ص","ض","ط","ظ","ع","غ","ف","ق","ك","ل","م","ن","ه","و","ي")
        val arNames = listOf("الألف","الباء","التاء","الثاء","الجيم","الحاء","الخاء","الدال","الذال","الراء","الزاي","السين","الشين","الصاد","الضاد","الطاء","الظاء","العين","الغين","الفاء","القاف","الكاف","اللام","الميم","النون","الهاء","الواو","الياء")

        Regex("en_letter_(\\d{2})_name").matchEntire(name)?.let {
            val i = it.groupValues[1].toInt() - 1
            if (i in enLetters.indices) return enLetters[i].toString() to "en"
        }

        Regex("ar_letter_(\\d{2})_sound").matchEntire(name)?.let {
            val i = it.groupValues[1].toInt() - 1
            if (i in arLetters.indices) return (if (arLetters[i] == "ا") "أَ" else arLetters[i] + "َ") to "ar"
        }

        Regex("ar_letter_(\\d{2})_name").matchEntire(name)?.let {
            val i = it.groupValues[1].toInt() - 1
            if (i in arNames.indices) return arNames[i] to "ar"
        }

        Regex("ar_letter_(\\d{2})_vowel_([123])").matchEntire(name)?.let {
            val i = it.groupValues[1].toInt() - 1
            val v = it.groupValues[2].toInt()
            if (i in arLetters.indices) {
                val marks = listOf("َ", "ُ", "ِ")
                return arLetters[i] + marks[v - 1] to "ar"
            }
        }

        Regex("(?:en|ar)_number_(\\d{3})").matchEntire(name)?.let {
            val n = it.groupValues[1].toInt()
            if (n in 1..100) return if (name.startsWith("en_")) englishNumberName(n) to "en" else arabicNumberName(n) to "ar"
        }

        val fixed = mapOf(
            "welcome_en" to ("Hello! Welcome to the learning app." to "en"),
            "welcome_ar" to ("أهلاً بك! هيا نتعلم معاً." to "ar"),
            "letters_intro_en" to ("Let's learn the English letters." to "en"),
            "letters_intro_ar" to ("هيا نتعلم الحروف العربية." to "ar"),
            "numbers_intro_en" to ("Let's learn the numbers." to "en"),
            "numbers_intro_ar" to ("هيا نتعلم الأرقام." to "ar"),
            "writing_intro_en" to ("Let's practice writing." to "en"),
            "writing_intro_ar" to ("هيا نتدرب على الكتابة." to "ar"),
            "reading_intro_en" to ("Let's practice reading." to "en"),
            "reading_intro_ar" to ("هيا نتدرب على القراءة." to "ar"),
            "stories_intro_en" to ("Let's listen to a story." to "en"),
            "stories_intro_ar" to ("هيا نستمع إلى قصة." to "ar"),
            "games_intro_en" to ("Let's play and learn!" to "en"),
            "games_intro_ar" to ("هيا نلعب ونتعلم!" to "ar"),
            "quiz_intro_en" to ("Let's start the quiz." to "en"),
            "quiz_intro_ar" to ("هيا نبدأ الاختبار." to "ar"),
            "correct_en" to ("Great job!" to "en"),
            "correct_ar" to ("أحسنت!" to "ar"),
            "wrong_en" to ("Try again." to "en"),
            "wrong_ar" to ("حاول مرة أخرى." to "ar"),
            "next_en" to ("Next." to "en"),
            "next_ar" to ("التالي." to "ar"),
            "back_en" to ("Back." to "en"),
            "back_ar" to ("رجوع." to "ar")
        )
        fixed[name]?.let { return it }

        Regex("praise_(ar|en)_\\d{2}").matchEntire(name)?.let {
            return if (it.groupValues[1] == "ar") "أحسنت! عمل رائع! استمر!" to "ar" else "Great job! Keep going!" to "en"
        }
        return null
    }

    private fun englishNumberName(n: Int): String {
        val ones = arrayOf("zero","one","two","three","four","five","six","seven","eight","nine","ten","eleven","twelve","thirteen","fourteen","fifteen","sixteen","seventeen","eighteen","nineteen")
        val tens = arrayOf("","","twenty","thirty","forty","fifty","sixty","seventy","eighty","ninety")
        return when {
            n < 20 -> ones[n]
            n % 10 == 0 -> tens[n / 10]
            else -> "${tens[n / 10]} ${ones[n % 10]}"
        }
    }

    private fun arabicNumberName(n: Int): String {
        val ones = arrayOf("","واحد","اثنان","ثلاثة","أربعة","خمسة","ستة","سبعة","ثمانية","تسعة","عشرة","أحد عشر","اثنا عشر","ثلاثة عشر","أربعة عشر","خمسة عشر","ستة عشر","سبعة عشر","ثمانية عشر","تسعة عشر")
        val tens = arrayOf("","","عشرون","ثلاثون","أربعون","خمسون","ستون","سبعون","ثمانون","تسعون")
        return when {
            n == 100 -> "مئة"
            n < 20 -> ones[n]
            n % 10 == 0 -> tens[n / 10]
            else -> "${ones[n % 10]} و${tens[n / 10]}"
        }
    }

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
