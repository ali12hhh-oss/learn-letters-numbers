package com.learnlettersnumbers.app

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.speech.RecognitionListener
import android.speech.RecognizerIntent
import android.speech.SpeechRecognizer
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.awaitEachGesture
import androidx.compose.foundation.gestures.awaitFirstDown
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.google.mlkit.common.model.DownloadConditions
import com.google.mlkit.common.model.RemoteModelManager
import com.google.mlkit.vision.digitalink.recognition.DigitalInkRecognition
import com.google.mlkit.vision.digitalink.recognition.DigitalInkRecognitionModel
import com.google.mlkit.vision.digitalink.recognition.DigitalInkRecognitionModelIdentifier
import com.google.mlkit.vision.digitalink.recognition.DigitalInkRecognizerOptions
import com.google.mlkit.vision.digitalink.recognition.Ink
import com.google.mlkit.vision.digitalink.recognition.RecognitionContext
import com.google.mlkit.vision.digitalink.recognition.WritingArea
import java.text.Normalizer
import java.util.Locale

private data class ArabicWord(val text: String, val letters: Int)

private val arabicWords = listOf(
    ArabicWord("دار", 3), ArabicWord("دور", 3), ArabicWord("بان", 3), ArabicWord("باب", 3),
    ArabicWord("بيت", 3), ArabicWord("قلم", 3), ArabicWord("قمر", 3), ArabicWord("شمس", 3),
    ArabicWord("أسد", 3), ArabicWord("نمر", 3), ArabicWord("جمل", 3), ArabicWord("فيل", 3),
    ArabicWord("موز", 3), ArabicWord("تين", 3), ArabicWord("عنب", 3), ArabicWord("درس", 3),
    ArabicWord("حرف", 3), ArabicWord("لون", 3), ArabicWord("علم", 3), ArabicWord("نور", 3),
    ArabicWord("كتاب", 4), ArabicWord("دفتر", 4), ArabicWord("حروف", 4), ArabicWord("تفاح", 4),
    ArabicWord("رمان", 4), ArabicWord("حصان", 4), ArabicWord("سمكة", 4), ArabicWord("طائر", 4),
    ArabicWord("نحلة", 4), ArabicWord("بقرة", 4), ArabicWord("وردة", 4), ArabicWord("زهرة", 4)
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
            Modifier.fillMaxSize().background(Color(0xFFF3FAFF)).padding(horizontal = 10.dp, vertical = 8.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Row(Modifier.fillMaxWidth().height(52.dp), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                Button(onClick = onBack, shape = RoundedCornerShape(16.dp), modifier = Modifier.height(46.dp)) { Text("رجوع", fontSize = 16.sp) }
                Text("القراءة", fontSize = 29.sp, fontWeight = FontWeight.ExtraBold, color = Color(0xFF2357A6))
            }
            Spacer(Modifier.height(5.dp))
            Row(Modifier.fillMaxWidth().height(54.dp), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                WordModeButton("📖 اقرءا كلمات", mode == "read", Color(0xFF4C8BF5), Modifier.weight(1f)) { mode = "read" }
                WordModeButton("✏️ اكتب كلمات", mode == "write", Color(0xFF6BCB77), Modifier.weight(1f)) { mode = "write" }
            }
            Spacer(Modifier.height(5.dp))
            Row(Modifier.fillMaxWidth().height(44.dp), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                WordLengthButton("3 حروف", length == 3, Color(0xFFFF8A4C), Modifier.weight(1f)) { length = 3; index = 0 }
                WordLengthButton("4 حروف", length == 4, Color(0xFF9B72E8), Modifier.weight(1f)) { length = 4; index = 0 }
            }
            Spacer(Modifier.height(7.dp))
            if (mode == "read") {
                ArabicWordReading(
                    current.text, audio, repo,
                    Modifier.weight(1f),
                    onPrevious = { index = if (index > 0) index - 1 else words.lastIndex },
                    onNext = { index = if (index < words.lastIndex) index + 1 else 0 }
                )
            } else {
                ArabicWordWriting(
                    current.text, audio, repo,
                    Modifier.weight(1f),
                    onPrevious = { index = if (index > 0) index - 1 else words.lastIndex },
                    onNext = { index = if (index < words.lastIndex) index + 1 else 0 }
                )
            }
        }
    }
}

@Composable
private fun WordModeButton(text: String, selected: Boolean, color: Color, modifier: Modifier, onClick: () -> Unit) {
    Card(onClick = onClick, modifier = modifier.fillMaxHeight(), shape = RoundedCornerShape(18.dp), colors = CardDefaults.cardColors(containerColor = if (selected) color else Color.White), elevation = CardDefaults.cardElevation(if (selected) 7.dp else 2.dp)) {
        Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) { Text(text, fontSize = 15.sp, fontWeight = FontWeight.ExtraBold, color = if (selected) Color.White else color, textAlign = TextAlign.Center) }
    }
}

@Composable
private fun WordLengthButton(text: String, selected: Boolean, color: Color, modifier: Modifier, onClick: () -> Unit) {
    Button(onClick = onClick, modifier = modifier.fillMaxHeight(), shape = RoundedCornerShape(16.dp), colors = ButtonDefaults.buttonColors(containerColor = if (selected) color else Color(0xFFE8EEF5))) {
        Text(text, fontWeight = FontWeight.ExtraBold, color = if (selected) Color.White else Color(0xFF34526F))
    }
}

@Composable
private fun ArabicWordReading(word: String, audio: LocalAudioManager, repo: ProgressRepository, modifier: Modifier, onPrevious: () -> Unit, onNext: () -> Unit) {
    val context = LocalContext.current
    var heard by remember(word) { mutableStateOf("") }
    var status by remember(word) { mutableStateOf("اضغط «اقرأ الآن» ثم اقرأ الكلمة بصوت واضح") }
    var correct by remember(word) { mutableStateOf<Boolean?>(null) }
    var listening by remember(word) { mutableStateOf(false) }
    var failedAttempts by remember(word) { mutableIntStateOf(0) }
    val recognizer = remember(context) { if (SpeechRecognizer.isRecognitionAvailable(context)) SpeechRecognizer.createSpeechRecognizer(context) else null }
    val launcher = rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()) { granted -> if (granted) startRecognition(recognizer) else status = "نحتاج إذن الميكروفون" }
    DisposableEffect(recognizer) { onDispose { recognizer?.destroy() } }

    fun registerFailure(message: String) {
        failedAttempts += 1
        correct = false
        status = message
    }

    LaunchedEffect(recognizer, word) {
        recognizer?.setRecognitionListener(object : RecognitionListener {
            override fun onReadyForSpeech(params: Bundle?) { listening = true; status = "استمع إليك... اقرأ الكلمة الآن" }
            override fun onBeginningOfSpeech() { status = "أكمل قراءة الكلمة..." }
            override fun onRmsChanged(rmsdB: Float) = Unit
            override fun onBufferReceived(buffer: ByteArray?) = Unit
            override fun onEndOfSpeech() { listening = false; status = "جارٍ التحقق..." }
            override fun onError(error: Int) { listening = false; registerFailure("لم أفهم الكلمة، حاول مرة أخرى") }
            override fun onResults(results: Bundle?) {
                listening = false
                val options = results?.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION).orEmpty()
                heard = options.firstOrNull().orEmpty()
                if (options.any { arabicWordMatches(it, word) }) {
                    correct = true
                    failedAttempts = 0
                    status = "أحسنت! قراءة صحيحة 👏"
                    repo.recordLesson("Arabic word reading", word, true)
                    repo.addStars(1)
                } else registerFailure(if (heard.isBlank()) "لم أتعرف على الكلمة" else "سمعت: «$heard» — حاول مرة أخرى")
            }
            override fun onPartialResults(partialResults: Bundle?) = Unit
            override fun onEvent(eventType: Int, params: Bundle?) = Unit
        })
    }

    Card(modifier, shape = RoundedCornerShape(28.dp), colors = CardDefaults.cardColors(containerColor = Color.White), elevation = CardDefaults.cardElevation(10.dp)) {
        Column(Modifier.fillMaxSize().padding(14.dp), horizontalAlignment = Alignment.CenterHorizontally) {
            Column(Modifier.weight(1f).fillMaxWidth(), horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.Center) {
                Text("اقرأ الكلمة بصوتك", fontSize = 20.sp, fontWeight = FontWeight.Bold, color = Color(0xFF34526F))
                Spacer(Modifier.height(8.dp))
                Text(word, fontSize = 76.sp, fontWeight = FontWeight.Black, color = Color(0xFF1F5D8C), textAlign = TextAlign.Center)
                Spacer(Modifier.height(14.dp))
                Button(onClick = { if (androidx.core.content.ContextCompat.checkSelfPermission(context, Manifest.permission.RECORD_AUDIO) == PackageManager.PERMISSION_GRANTED) startRecognition(recognizer) else launcher.launch(Manifest.permission.RECORD_AUDIO) }, enabled = !listening && recognizer != null, modifier = Modifier.fillMaxWidth().height(54.dp), shape = RoundedCornerShape(18.dp), colors = ButtonDefaults.buttonColors(containerColor = if (listening) Color.Gray else Color(0xFF4C8BF5))) {
                    Text(if (listening) "🎙️ أستمع..." else "🎙️ اقرأ الآن", fontSize = 19.sp, fontWeight = FontWeight.ExtraBold)
                }
                if (failedAttempts >= 2) {
                    Spacer(Modifier.height(8.dp))
                    Button(onClick = { audio.speakOffline(word, "ar") }, modifier = Modifier.fillMaxWidth().height(48.dp), shape = RoundedCornerShape(16.dp), colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF26A69A))) {
                        Text("🔊 اسمع الكلمة", fontSize = 18.sp, fontWeight = FontWeight.ExtraBold)
                    }
                }
                Spacer(Modifier.height(8.dp))
                Text(status, fontSize = 16.sp, fontWeight = FontWeight.Bold, color = when (correct) { true -> Color(0xFF16833D); false -> Color(0xFFC62828); else -> Color(0xFF5C6B73) }, textAlign = TextAlign.Center)
                if (heard.isNotBlank()) Text("سمعت: $heard", fontSize = 14.sp, color = Color(0xFF53636D), textAlign = TextAlign.Center)
            }
            WordNavigationRow(onPrevious, onNext)
        }
    }
}

@Composable
private fun ArabicWordWriting(word: String, audio: LocalAudioManager, repo: ProgressRepository, modifier: Modifier, onPrevious: () -> Unit, onNext: () -> Unit) {
    val strokes = remember(word) { mutableStateListOf<List<Offset>>() }
    var currentStroke by remember(word) { mutableStateOf<List<Offset>>(emptyList()) }
    var result by remember(word) { mutableStateOf<Boolean?>(null) }
    var recognized by remember(word) { mutableStateOf("") }
    var status by remember(word) { mutableStateOf("فحص نموذج الكتابة العربية...") }
    var modelReady by remember(word) { mutableStateOf(false) }
    var modelDownloading by remember(word) { mutableStateOf(false) }
    var checking by remember(word) { mutableStateOf(false) }
    var failedAttempts by remember(word) { mutableIntStateOf(0) }

    val model = remember { try { DigitalInkRecognitionModel.builder(DigitalInkRecognitionModelIdentifier.AR).build() } catch (_: Exception) { null } }
    val digitalRecognizer = remember(model) { model?.let { DigitalInkRecognition.getClient(DigitalInkRecognizerOptions.builder(it).build()) } }
    DisposableEffect(digitalRecognizer) { onDispose { digitalRecognizer?.close() } }

    fun markModelReady(message: String = "النموذج جاهز — اكتب الكلمة داخل اللوحة") { modelReady = true; modelDownloading = false; status = message }
    fun prepareModel() {
        val currentModel = model ?: run { modelReady = false; modelDownloading = false; status = "نموذج الكتابة العربية غير متاح"; return }
        modelDownloading = true
        status = "فحص نموذج الكتابة العربية..."
        val manager = RemoteModelManager.getInstance()
        manager.isModelDownloaded(currentModel).addOnSuccessListener { downloaded ->
            if (downloaded) markModelReady() else {
                status = "جاري تنزيل نموذج الكتابة العربية..."
                manager.download(currentModel, DownloadConditions.Builder().build())
                    .addOnSuccessListener { markModelReady() }
                    .addOnFailureListener { error ->
                        modelReady = false; modelDownloading = false
                        val detail = error.message?.take(90).orEmpty()
                        status = if (detail.isBlank()) "تعذر تنزيل النموذج. تحقق من الإنترنت ثم أعد المحاولة." else "تعذر تنزيل النموذج: $detail"
                    }
            }
        }.addOnFailureListener { error ->
            modelReady = false; modelDownloading = false
            val detail = error.message?.take(90).orEmpty()
            status = if (detail.isBlank()) "تعذر فحص النموذج. أعد المحاولة." else "تعذر فحص النموذج: $detail"
        }
    }
    LaunchedEffect(model) { prepareModel() }

    fun clearBoard() {
        strokes.clear(); currentStroke = emptyList(); result = null; recognized = ""
        status = if (modelReady) "اكتب «$word» ثم اضغط تحقق" else status
    }

    fun checkWriting() {
        if (checking || !modelReady || digitalRecognizer == null) return
        val finalStrokes: List<List<Offset>> = strokes.toList() + currentStroke.let { if (it.isEmpty()) emptyList() else listOf(it) }
        currentStroke = emptyList()
        if (finalStrokes.isEmpty()) { status = "اكتب كلمة «$word» أولاً"; result = false; return }
        checking = true; result = null; status = "جارٍ التعرف على الكتابة..."
        val allPoints = finalStrokes.flatten()
        val minX = allPoints.minOf { it.x }; val maxX = allPoints.maxOf { it.x }
        val minY = allPoints.minOf { it.y }; val maxY = allPoints.maxOf { it.y }
        val sourceW = (maxX - minX).coerceAtLeast(1f); val sourceH = (maxY - minY).coerceAtLeast(1f)
        val inkBuilder = Ink.builder(); var timestamp = System.currentTimeMillis()
        finalStrokes.forEach { points ->
            val strokeBuilder = Ink.Stroke.builder()
            points.forEach { point ->
                val x = ((point.x - minX) / sourceW * 850f).coerceIn(0f, 850f)
                val y = ((point.y - minY) / sourceH * 300f).coerceIn(0f, 300f)
                strokeBuilder.addPoint(Ink.Point.create(x, y, timestamp++))
            }
            inkBuilder.addStroke(strokeBuilder.build()); timestamp += 20
        }
        val context = RecognitionContext.builder().setWritingArea(WritingArea(900f, 350f)).build()
        digitalRecognizer.recognize(inkBuilder.build(), context).addOnSuccessListener { recognition ->
            val candidates = recognition.candidates.take(10).map { it.text }
            recognized = candidates.joinToString("، ")
            val target = normalizeArabicForWriting(word)
            val ok = candidates.any { normalizeArabicForWriting(it) == target }
            result = ok; checking = false
            if (ok) {
                failedAttempts = 0; status = "أحسنت! كتابة صحيحة 👏"
                repo.recordLesson("Arabic word writing", word, true); repo.addStars(1)
            } else {
                failedAttempts += 1
                status = if (failedAttempts >= 2) "حاول مرة أخرى — ظهرت الكلمة في منتصف اللوحة للمساعدة" else "لم أتعرف على الكلمة، حاول مرة أخرى"
            }
        }.addOnFailureListener { error ->
            failedAttempts += 1; result = false; checking = false
            status = error.message?.let { "تعذر التعرف: ${it.take(90)}" } ?: "حدث خطأ أثناء التعرف"
        }
    }

    Card(modifier, shape = RoundedCornerShape(28.dp), colors = CardDefaults.cardColors(containerColor = Color.White), elevation = CardDefaults.cardElevation(10.dp)) {
        Column(Modifier.fillMaxSize().padding(10.dp), horizontalAlignment = Alignment.CenterHorizontally) {
            Text("اكتب الكلمة", fontSize = 20.sp, fontWeight = FontWeight.ExtraBold, color = Color(0xFF34526F))
            Text(word, fontSize = 52.sp, fontWeight = FontWeight.Black, color = Color(0xFF1F5D8C), textAlign = TextAlign.Center)
            Box(Modifier.fillMaxWidth().weight(1f).padding(vertical = 5.dp).background(Color(0xFFFCFEFF), RoundedCornerShape(20.dp))) {
                if (failedAttempts >= 2) Text(word, modifier = Modifier.align(Alignment.Center), fontSize = 76.sp, fontWeight = FontWeight.Bold, color = Color(0x421F5D8C), textAlign = TextAlign.Center)
                Canvas(Modifier.fillMaxSize().pointerInput(word) {
                    awaitEachGesture {
                        val down = awaitFirstDown(requireUnconsumed = false); down.consume()
                        currentStroke = listOf(down.position)
                        var active = true
                        while (active) {
                            val event = awaitPointerEvent()
                            val change = event.changes.firstOrNull { it.id == down.id }
                            if (change == null) active = false else {
                                if (change.position != currentStroke.lastOrNull()) currentStroke = currentStroke + change.position
                                change.consume(); if (!change.pressed) active = false
                            }
                        }
                        if (currentStroke.isNotEmpty()) strokes.add(currentStroke)
                        currentStroke = emptyList()
                    }
                }) {
                    drawLine(Color(0xFFD8E3EC), Offset(24f, size.height / 2f), Offset(size.width - 24f, size.height / 2f), strokeWidth = 2f)
                    val all = strokes.toList() + listOfNotNull(currentStroke.takeIf { it.isNotEmpty() })
                    all.forEach { points ->
                        if (points.size == 1) drawCircle(Color(0xFF2357A6), radius = 13f, center = points[0]) else {
                            val path = Path()
                            points.forEachIndexed { i, p -> if (i == 0) path.moveTo(p.x, p.y) else path.lineTo(p.x, p.y) }
                            drawPath(path, color = Color(0xFF2357A6), style = androidx.compose.ui.graphics.drawscope.Stroke(width = 24f, cap = StrokeCap.Round, join = StrokeJoin.Round))
                        }
                    }
                }
            }
            Text(status, modifier = Modifier.fillMaxWidth().heightIn(min = 28.dp, max = 42.dp), fontSize = 13.sp, fontWeight = FontWeight.Bold, color = when (result) { true -> Color(0xFF16833D); false -> Color(0xFFC62828); else -> Color(0xFF5C6B73) }, textAlign = TextAlign.Center, maxLines = 2)
            if (recognized.isNotBlank()) Text("اقتراحات: $recognized", modifier = Modifier.fillMaxWidth(), fontSize = 11.sp, color = Color(0xFF53636D), textAlign = TextAlign.Center, maxLines = 1)
            if (!modelReady && !modelDownloading) OutlinedButton(onClick = { prepareModel() }, modifier = Modifier.fillMaxWidth().height(38.dp), shape = RoundedCornerShape(12.dp)) { Text("إعادة تجهيز النموذج", fontWeight = FontWeight.Bold, fontSize = 13.sp) }
            Spacer(Modifier.height(4.dp))
            WordActionRow(onClear = ::clearBoard, onCheck = ::checkWriting, checkEnabled = modelReady && !checking, onPrevious = onPrevious, onNext = onNext, checking = checking)
        }
    }
}

@Composable
private fun WordNavigationRow(onPrevious: () -> Unit, onNext: () -> Unit) {
    Row(Modifier.fillMaxWidth().height(50.dp), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        OutlinedButton(onClick = onPrevious, modifier = Modifier.weight(1f).fillMaxHeight(), shape = RoundedCornerShape(15.dp)) { Text("السابق", fontWeight = FontWeight.ExtraBold, fontSize = 16.sp) }
        Button(onClick = onNext, modifier = Modifier.weight(1f).fillMaxHeight(), shape = RoundedCornerShape(15.dp), colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFFF8A4C))) { Text("التالي", fontWeight = FontWeight.ExtraBold, fontSize = 16.sp) }
    }
}

@Composable
private fun WordActionRow(onClear: () -> Unit, onCheck: () -> Unit, checkEnabled: Boolean, onPrevious: () -> Unit, onNext: () -> Unit, checking: Boolean) {
    Row(Modifier.fillMaxWidth().height(50.dp), horizontalArrangement = Arrangement.spacedBy(6.dp), verticalAlignment = Alignment.CenterVertically) {
        OutlinedButton(onClick = onPrevious, modifier = Modifier.weight(1f).fillMaxHeight(), shape = RoundedCornerShape(14.dp), contentPadding = PaddingValues(horizontal = 2.dp)) { Text("السابق", fontWeight = FontWeight.ExtraBold, fontSize = 14.sp) }
        OutlinedButton(onClick = onClear, modifier = Modifier.weight(1f).fillMaxHeight(), shape = RoundedCornerShape(14.dp), contentPadding = PaddingValues(horizontal = 2.dp)) { Text("مسح", fontWeight = FontWeight.ExtraBold, fontSize = 14.sp) }
        Button(onClick = onCheck, enabled = checkEnabled, modifier = Modifier.weight(1f).fillMaxHeight(), shape = RoundedCornerShape(14.dp), contentPadding = PaddingValues(horizontal = 2.dp), colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF4C8BF5))) { Text(if (checking) "يفحص..." else "تحقق", fontWeight = FontWeight.ExtraBold, fontSize = 14.sp) }
        Button(onClick = onNext, modifier = Modifier.weight(1f).fillMaxHeight(), shape = RoundedCornerShape(14.dp), contentPadding = PaddingValues(horizontal = 2.dp), colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFFF8A4C))) { Text("التالي", fontWeight = FontWeight.ExtraBold, fontSize = 14.sp) }
    }
}

private fun startRecognition(recognizer: SpeechRecognizer?) {
    if (recognizer == null) return
    val intent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
        putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
        putExtra(RecognizerIntent.EXTRA_LANGUAGE, "ar-IQ")
        putExtra(RecognizerIntent.EXTRA_LANGUAGE_PREFERENCE, "ar-IQ")
        putExtra(RecognizerIntent.EXTRA_MAX_RESULTS, 5)
        putExtra(RecognizerIntent.EXTRA_PARTIAL_RESULTS, false)
        putExtra(RecognizerIntent.EXTRA_PROMPT, "اقرأ الكلمة الظاهرة بصوت واضح")
    }
    recognizer.startListening(intent)
}

private fun arabicWordMatches(recognized: String, target: String): Boolean = normalizeArabicForSpeech(recognized) == normalizeArabicForSpeech(target)

private fun normalizeArabicForSpeech(value: String): String {
    val decomposed = Normalizer.normalize(value.trim().lowercase(Locale.ROOT), Normalizer.Form.NFD)
    return decomposed.replace(Regex("[\\u064B-\\u0652]"), "").replace("\u0640", "").replace("أ", "ا").replace("إ", "ا").replace("آ", "ا").replace("ى", "ي").replace(Regex("\\s+"), "")
}

private fun normalizeArabicForWriting(value: String): String {
    val decomposed = Normalizer.normalize(value.trim().lowercase(Locale.ROOT), Normalizer.Form.NFD)
    return decomposed.replace(Regex("[\\u064B-\\u0652]"), "").replace("\u0640", "").replace("أ", "ا").replace("إ", "ا").replace("آ", "ا").replace("ى", "ي").replace(Regex("\\s+"), "")
}