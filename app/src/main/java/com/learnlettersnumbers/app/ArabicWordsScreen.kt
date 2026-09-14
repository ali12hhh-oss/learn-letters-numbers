package com.learnlettersnumbers.app

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Paint
import android.graphics.Path as AndroidPath
import android.graphics.PathMeasure
import android.graphics.Typeface
import android.os.Bundle
import android.speech.RecognitionListener
import android.speech.RecognizerIntent
import android.speech.SpeechRecognizer
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import java.text.Normalizer
import java.util.Locale
import kotlin.math.hypot

private data class ArabicWord(val text: String, val letters: Int)

private val arabicWords = listOf(
    ArabicWord("باب", 3), ArabicWord("بيت", 3), ArabicWord("قلم", 3), ArabicWord("ولد", 3),
    ArabicWord("بنت", 3), ArabicWord("نهر", 3), ArabicWord("قمر", 3), ArabicWord("شمس", 3),
    ArabicWord("كتاب", 4), ArabicWord("حليب", 4), ArabicWord("تفاح", 4), ArabicWord("حصان", 4),
    ArabicWord("سمكة", 4), ArabicWord("زهرة", 4), ArabicWord("كرسي", 4), ArabicWord("ملعب", 4)
)

@Composable
fun ArabicWordsScreen(audio: LocalAudioManager, repo: ProgressRepository, onBack: () -> Unit) {
    var mode by remember { mutableStateOf("read") }
    var length by remember { mutableIntStateOf(3) }
    var index by remember { mutableIntStateOf(0) }
    val words = remember(length) { arabicWords.filter { it.letters == length } }
    val current = words[index.coerceIn(0, words.lastIndex)]

    CompositionLocalProvider(androidx.compose.ui.platform.LocalLayoutDirection provides androidx.compose.ui.unit.LayoutDirection.Rtl) {
        Column(
            Modifier.fillMaxSize().background(Color(0xFFF3FAFF)).padding(12.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                Button(onClick = onBack, shape = RoundedCornerShape(16.dp)) { Text("رجوع") }
                Text("القراءة", fontSize = 29.sp, fontWeight = FontWeight.ExtraBold, color = Color(0xFF2357A6))
            }
            Spacer(Modifier.height(8.dp))
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                WordModeButton("📖 اقرءا كلمات", mode == "read", Color(0xFF4C8BF5), Modifier.weight(1f)) { mode = "read" }
                WordModeButton("✏️ اكتب كلمات", mode == "write", Color(0xFF6BCB77), Modifier.weight(1f)) { mode = "write" }
            }
            Spacer(Modifier.height(8.dp))
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                WordLengthButton("3 حروف", length == 3, Color(0xFFFF8A4C), Modifier.weight(1f)) { length = 3; index = 0 }
                WordLengthButton("4 حروف", length == 4, Color(0xFF9B72E8), Modifier.weight(1f)) { length = 4; index = 0 }
            }
            Spacer(Modifier.height(8.dp))

            if (mode == "read") {
                ArabicWordReading(word = current.text, audio = audio, repo = repo, onNext = {
                    index = if (index < words.lastIndex) index + 1 else 0
                })
            } else {
                ArabicWordWriting(word = current.text, audio = audio, repo = repo, onNext = {
                    index = if (index < words.lastIndex) index + 1 else 0
                })
            }
        }
    }
}

@Composable
private fun WordModeButton(text: String, selected: Boolean, color: Color, modifier: Modifier, onClick: () -> Unit) {
    Card(modifier.height(58.dp).clickable(onClick = onClick), shape = RoundedCornerShape(18.dp), colors = CardDefaults.cardColors(containerColor = if (selected) color else Color.White), elevation = CardDefaults.cardElevation(if (selected) 7.dp else 2.dp)) {
        Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Text(text, fontSize = 15.sp, fontWeight = FontWeight.ExtraBold, color = if (selected) Color.White else color, textAlign = TextAlign.Center)
        }
    }
}

@Composable
private fun WordLengthButton(text: String, selected: Boolean, color: Color, modifier: Modifier, onClick: () -> Unit) {
    Button(onClick = onClick, modifier = modifier.height(46.dp), shape = RoundedCornerShape(16.dp), colors = ButtonDefaults.buttonColors(containerColor = if (selected) color else Color(0xFFE8EEF5))) {
        Text(text, fontWeight = FontWeight.ExtraBold, color = if (selected) Color.White else Color(0xFF34526F))
    }
}

@Composable
private fun ArabicWordReading(word: String, audio: LocalAudioManager, repo: ProgressRepository, onNext: () -> Unit) {
    var heard by remember(word) { mutableStateOf("") }
    var status by remember(word) { mutableStateOf("اضغط على الميكروفون ثم اقرأ الكلمة بصوت واضح") }
    var correct by remember(word) { mutableStateOf<Boolean?>(null) }
    var listening by remember { mutableStateOf(false) }
    var recognizer by remember { mutableStateOf<SpeechRecognizer?>(null) }

    val launcher = rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()) { granted ->
        if (granted) startArabicRecognition(word, audio, repo, onNext, { listening = it }, { heard = it }, { status = it }, { correct = it }, recognizer)
        else status = "نحتاج إذن الميكروفون حتى يسمع التطبيق قراءة الطفل"
    }

    DisposableEffect(Unit) {
        if (SpeechRecognizer.isRecognitionAvailable(audioContext(audio))) {
            recognizer = SpeechRecognizer.createSpeechRecognizer(audioContext(audio))
        }
        onDispose { recognizer?.destroy(); recognizer = null }
    }

    LaunchedEffect(recognizer) {
        recognizer?.setRecognitionListener(object : RecognitionListener {
            override fun onReadyForSpeech(params: Bundle?) { listening = true; status = "استمع إليك... اقرأ الكلمة الآن" }
            override fun onBeginningOfSpeech() { status = "أحسنت، أكمل قراءة الكلمة..." }
            override fun onRmsChanged(rmsdB: Float) = Unit
            override fun onBufferReceived(buffer: ByteArray?) = Unit
            override fun onEndOfSpeech() { listening = false; status = "جارٍ التحقق..." }
            override fun onError(error: Int) { listening = false; status = if (error == SpeechRecognizer.ERROR_NO_MATCH) "لم أفهم الكلمة، حاول مرة أخرى" else "تعذر سماع الكلمة، حاول مرة أخرى"; correct = false; audio.speakOffline("حاول مرة أخرى", "ar") }
            override fun onResults(results: Bundle?) {
                listening = false
                val options = results?.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION).orEmpty()
                val recognized = options.firstOrNull().orEmpty()
                heard = recognized
                val ok = options.any { arabicWordMatches(it, word) }
                correct = ok
                if (ok) {
                    status = "أحسنت! قراءة صحيحة 👏"
                    repo.recordLesson("Arabic word reading", word, true)
                    repo.addStars(1)
                    audio.speakOffline("أحسنت", "ar")
                } else {
                    status = "حاول مرة أخرى 🌟"
                    audio.speakOffline("حاول مرة أخرى", "ar")
                }
            }
            override fun onPartialResults(partialResults: Bundle?) = Unit
            override fun onEvent(eventType: Int, params: Bundle?) = Unit
        })
    }

    Card(Modifier.fillMaxWidth().weight(1f), shape = RoundedCornerShape(28.dp), colors = CardDefaults.cardColors(containerColor = Color.White), elevation = CardDefaults.cardElevation(10.dp)) {
        Column(Modifier.fillMaxSize().padding(16.dp), horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.Center) {
            Text("اقرأ الكلمة بصوتك", fontSize = 20.sp, fontWeight = FontWeight.Bold, color = Color(0xFF34526F))
            Spacer(Modifier.height(22.dp))
            Text(word, fontSize = 78.sp, fontWeight = FontWeight.Black, color = Color(0xFF1F5D8C), textAlign = TextAlign.Center)
            Spacer(Modifier.height(14.dp))
            Button(onClick = { audio.speakOffline(word, "ar") }, shape = RoundedCornerShape(18.dp)) { Text("🔊 اسمع الكلمة", fontSize = 17.sp) }
            Spacer(Modifier.height(12.dp))
            Button(onClick = {
                if (androidx.core.content.ContextCompat.checkSelfPermission(audioContext(audio), Manifest.permission.RECORD_AUDIO) == PackageManager.PERMISSION_GRANTED) {
                    startArabicRecognition(word, audio, repo, onNext, { listening = it }, { heard = it }, { status = it }, { correct = it }, recognizer)
                } else launcher.launch(Manifest.permission.RECORD_AUDIO)
            }, enabled = !listening && recognizer != null, modifier = Modifier.fillMaxWidth().height(56.dp), shape = RoundedCornerShape(20.dp), colors = ButtonDefaults.buttonColors(containerColor = if (listening) Color.Gray else Color(0xFF4C8BF5))) {
                Text(if (listening) "🎙️ أستمع..." else "🎙️ اقرأ الآن", fontSize = 20.sp, fontWeight = FontWeight.ExtraBold)
            }
            Spacer(Modifier.height(14.dp))
            Text(status, fontSize = 20.sp, fontWeight = FontWeight.Bold, color = when (correct) { true -> Color(0xFF16833D); false -> Color(0xFFC62828); else -> Color(0xFF5C6B73) }, textAlign = TextAlign.Center)
            if (heard.isNotBlank()) Text("سمعت: $heard", fontSize = 16.sp, color = Color(0xFF53636D), modifier = Modifier.padding(top = 8.dp))
            Spacer(Modifier.height(18.dp))
            Button(onClick = onNext, shape = RoundedCornerShape(18.dp), colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFFF8A4C))) { Text("الكلمة التالية", fontWeight = FontWeight.ExtraBold, fontSize = 17.sp) }
        }
    }
}

private fun startArabicRecognition(word: String, audio: LocalAudioManager, repo: ProgressRepository, onNext: () -> Unit, setListening: (Boolean) -> Unit, setHeard: (String) -> Unit, setStatus: (String) -> Unit, setCorrect: (Boolean?) -> Unit, recognizer: SpeechRecognizer?) {
    if (recognizer == null) { setStatus("التعرّف على الصوت غير متاح على هذا الجهاز"); return }
    val intent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
        putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
        putExtra(RecognizerIntent.EXTRA_LANGUAGE, "ar-IQ")
        putExtra(RecognizerIntent.EXTRA_LANGUAGE_PREFERENCE, "ar-IQ")
        putExtra(RecognizerIntent.EXTRA_MAX_RESULTS, 5)
        putExtra(RecognizerIntent.EXTRA_PARTIAL_RESULTS, false)
    }
    setListening(true)
    setStatus("استمع إليك... اقرأ الكلمة الآن")
    setHeard("")
    setCorrect(null)
    recognizer.startListening(intent)
}

private fun arabicWordMatches(recognized: String, target: String): Boolean {
    val a = normalizeArabic(recognized)
    val b = normalizeArabic(target)
    return a == b || a.contains(b) || b.contains(a)
}

private fun normalizeArabic(value: String): String {
    val decomposed = Normalizer.normalize(value.trim().lowercase(Locale.ROOT), Normalizer.Form.NFD)
    return decomposed.replace("\u064B".toRegex(), "")
        .replace("\u064C".toRegex(), "")
        .replace("\u064D".toRegex(), "")
        .replace("\u064E".toRegex(), "")
        .replace("\u064F".toRegex(), "")
        .replace("\u0650".toRegex(), "")
        .replace("\u0651".toRegex(), "")
        .replace("\u0652".toRegex(), "")
        .replace("\u0640", "")
        .replace(Regex("^ال"), "")
        .replace("أ", "ا").replace("إ", "ا").replace("آ", "ا").replace("ى", "ي")
        .replace(Regex("\\s+"), "")
}

@Composable
private fun ArabicWordWriting(word: String, audio: LocalAudioManager, repo: ProgressRepository, onNext: () -> Unit) {
    val strokes = remember(word) { mutableStateListOf<List<Offset>>() }
    var currentStroke by remember(word) { mutableStateOf<List<Offset>>(emptyList()) }
    var boardWidth by remember(word) { mutableFloatStateOf(0f) }
    var boardHeight by remember(word) { mutableFloatStateOf(0f) }
    var result by remember(word) { mutableStateOf<Boolean?>(null) }
    var help by remember(word) { mutableStateOf(true) }

    fun clear() { strokes.clear(); currentStroke = emptyList(); result = null }

    Column(Modifier.fillMaxSize().weight(1f)) {
        Card(Modifier.fillMaxWidth(), shape = RoundedCornerShape(22.dp), colors = CardDefaults.cardColors(containerColor = Color.White), elevation = CardDefaults.cardElevation(7.dp)) {
            Column(Modifier.padding(10.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                Text("اكتب الكلمة التالية", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = Color(0xFF45606F))
                Text(word, fontSize = 48.sp, fontWeight = FontWeight.Black, color = Color(0xFF2357A6), textAlign = TextAlign.Center)
            }
        }
        Spacer(Modifier.height(8.dp))
        Card(Modifier.fillMaxWidth().weight(1f), shape = RoundedCornerShape(26.dp), colors = CardDefaults.cardColors(containerColor = Color.White), elevation = CardDefaults.cardElevation(9.dp)) {
            Column(Modifier.fillMaxSize().padding(8.dp)) {
                Row(Modifier.fillMaxWidth().height(38.dp), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                    Text(if (help) "دليل خفيف للمساعدة" else "سبورة حرة", fontSize = 13.sp, color = Color(0xFF60727D))
                    TextButton(onClick = { help = !help }) { Text(if (help) "إخفاء الدليل" else "إظهار الدليل") }
                }
                val boardModifier = Modifier.fillMaxWidth().weight(1f).background(Color(0xFFFFFEF8), RoundedCornerShape(20.dp))
                    .pointerInput(word) {
                        detectDragGestures(
                            onDragStart = { currentStroke = listOf(it) },
                            onDrag = { change, _ -> change.consume(); currentStroke = currentStroke + change.position },
                            onDragEnd = { if (currentStroke.isNotEmpty()) strokes.add(currentStroke); currentStroke = emptyList() },
                            onDragCancel = { currentStroke = emptyList() }
                        )
                    }
                Canvas(boardModifier) {
                    boardWidth = size.width
                    boardHeight = size.height
                    if (help) drawContext.canvas.nativeCanvas.apply {
                        val paint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                            color = android.graphics.Color.argb(45, 35, 87, 130)
                            textSize = minOf(size.width / (word.length.coerceAtLeast(2) * 0.72f), 150f)
                            typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
                            textAlign = Paint.Align.CENTER
                        }
                        drawText(word, size.width / 2f, size.height / 2f - (paint.ascent() + paint.descent()) / 2f, paint)
                    }
                    val all = strokes + listOf(currentStroke)
                    all.forEach { pts ->
                        if (pts.size == 1) drawCircle(Color(0xFF245B8A), 7f, pts.first())
                        else if (pts.size > 1) {
                            val path = Path().apply { moveTo(pts[0].x, pts[0].y); for (i in 1 until pts.size) lineTo(pts[i].x, pts[i].y) }
                            drawPath(path, Color(0xFF245B8A), style = androidx.compose.ui.graphics.drawscope.Stroke(width = 20f, cap = StrokeCap.Round, join = StrokeJoin.Round))
                        }
                    }
                }
                Text("التحقق هنا مساعد تقريبي: يقارن مسار الكتابة بدليل الكلمة، وليس OCR حقيقيًا.", fontSize = 11.sp, color = Color(0xFF71808A), modifier = Modifier.padding(4.dp), textAlign = TextAlign.Center)
            }
        }
        Spacer(Modifier.height(6.dp))
        Text(result?.let { if (it) "أحسنت! الكتابة قريبة من الكلمة 👏" else "حاول مرة أخرى؛ اكتب الحروف كاملة وبهدوء" } ?: "بعد الانتهاء اضغط: تحقق من الكتابة", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = when (result) { true -> Color(0xFF16833D); false -> Color(0xFFC62828); else -> Color(0xFF596A74) }, textAlign = TextAlign.Center, modifier = Modifier.fillMaxWidth())
        Row(Modifier.fillMaxWidth().height(52.dp), horizontalArrangement = Arrangement.spacedBy(7.dp)) {
            OutlinedButton(onClick = { clear() }, modifier = Modifier.weight(1f), shape = RoundedCornerShape(16.dp)) { Text("مسح") }
            Button(onClick = {
                val ok = evaluateArabicWriting(word, strokes.toList(), boardWidth, boardHeight)
                result = ok
                if (ok) { repo.recordLesson("Arabic word writing", word, true); repo.addStars(1); audio.speakOffline("أحسنت", "ar") }
                else audio.speakOffline("حاول مرة أخرى", "ar")
            }, enabled = strokes.isNotEmpty(), modifier = Modifier.weight(1.4f), shape = RoundedCornerShape(16.dp), colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF4C8BF5))) { Text("✓ تحقق من الكتابة", fontWeight = FontWeight.ExtraBold) }
            Button(onClick = { clear(); onNext() }, modifier = Modifier.weight(1f), shape = RoundedCornerShape(16.dp), colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFFF8A4C))) { Text("التالي", fontWeight = FontWeight.ExtraBold) }
        }
    }
}

private fun evaluateArabicWriting(word: String, strokes: List<List<Offset>>, width: Float, height: Float): Boolean {
    if (strokes.isEmpty() || width <= 0f || height <= 0f) return false
    val points = strokes.flatten()
    if (points.size < 25) return false
    val paint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        textSize = minOf(width / (word.length.coerceAtLeast(2) * 0.72f), 150f)
        typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
    }
    val textWidth = paint.measureText(word)
    val x = width / 2f - textWidth / 2f
    val y = height / 2f - (paint.ascent() + paint.descent()) / 2f
    val targetPath = AndroidPath()
    paint.getTextPath(word, 0, word.length, x, y, targetPath)
    val measure = PathMeasure(targetPath, false)
    val samples = mutableListOf<Offset>()
    val pos = FloatArray(2)
    do {
        var d = 0f
        while (d <= measure.length) {
            if (measure.getPosTan(d, pos, null)) samples.add(Offset(pos[0], pos[1]))
            d += 8f
        }
    } while (measure.nextContour())
    if (samples.isEmpty()) return false

    fun minDistance(p: Offset, list: List<Offset>): Float = list.minOf { hypot(p.x - it.x, p.y - it.y) }
    val averageUserToGuide = points.map { minDistance(it, samples) }.average()
    val nearGuide = samples.count { minDistance(it, points) <= 45f }.toFloat() / samples.size
    val minX = points.minOf { it.x }; val maxX = points.maxOf { it.x }
    val minY = points.minOf { it.y }; val maxY = points.maxOf { it.y }
    val userWidth = maxX - minX; val userHeight = maxY - minY
    val enoughSize = userWidth >= textWidth * 0.35f && userHeight >= paint.textSize * 0.25f
    return averageUserToGuide <= 75f && nearGuide >= 0.28f && enoughSize
}

private fun audioContext(audio: LocalAudioManager): android.content.Context {
    val field = LocalAudioManager::class.java.getDeclaredField("context")
    field.isAccessible = true
    return field.get(audio) as android.content.Context
}
