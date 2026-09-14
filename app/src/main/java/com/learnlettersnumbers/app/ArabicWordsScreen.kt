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
import com.google.mlkit.vision.digitalink.recognition.DigitalInkRecognizer
import com.google.mlkit.vision.digitalink.recognition.DigitalInkRecognizerOptions
import com.google.mlkit.vision.digitalink.recognition.Ink
import com.google.mlkit.vision.digitalink.recognition.RecognitionContext
import com.google.mlkit.vision.digitalink.recognition.WritingArea
import java.text.Normalizer
import java.util.Locale

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
        Column(Modifier.fillMaxSize().background(Color(0xFFF3FAFF)).padding(12.dp), horizontalAlignment = Alignment.CenterHorizontally) {
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
            if (mode == "read") ArabicWordReading(current.text, audio, repo) { index = if (index < words.lastIndex) index + 1 else 0 }
            else ArabicWordWriting(current.text, audio, repo) { index = if (index < words.lastIndex) index + 1 else 0 }
        }
    }
}

@Composable
private fun WordModeButton(text: String, selected: Boolean, color: Color, modifier: Modifier, onClick: () -> Unit) {
    Card(onClick = onClick, modifier = modifier.height(58.dp), shape = RoundedCornerShape(18.dp), colors = CardDefaults.cardColors(containerColor = if (selected) color else Color.White), elevation = CardDefaults.cardElevation(if (selected) 7.dp else 2.dp)) {
        Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) { Text(text, fontSize = 15.sp, fontWeight = FontWeight.ExtraBold, color = if (selected) Color.White else color, textAlign = TextAlign.Center) }
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
    val context = LocalContext.current
    var heard by remember(word) { mutableStateOf("") }
    var status by remember(word) { mutableStateOf("اضغط على الميكروفون ثم اقرأ الكلمة بصوت واضح") }
    var correct by remember(word) { mutableStateOf<Boolean?>(null) }
    var listening by remember(word) { mutableStateOf(false) }
    val recognizer = remember(context) { if (SpeechRecognizer.isRecognitionAvailable(context)) SpeechRecognizer.createSpeechRecognizer(context) else null }
    val launcher = rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()) { granted -> if (granted) startRecognition(recognizer) else status = "نحتاج إذن الميكروفون حتى يسمع التطبيق قراءة الطفل" }
    DisposableEffect(recognizer) { onDispose { recognizer?.destroy() } }
    LaunchedEffect(recognizer, word) {
        recognizer?.setRecognitionListener(object : RecognitionListener {
            override fun onReadyForSpeech(params: Bundle?) { listening = true; status = "استمع إليك... اقرأ الكلمة الآن" }
            override fun onBeginningOfSpeech() { status = "أكمل قراءة الكلمة..." }
            override fun onRmsChanged(rmsdB: Float) = Unit
            override fun onBufferReceived(buffer: ByteArray?) = Unit
            override fun onEndOfSpeech() { listening = false; status = "جارٍ التحقق..." }
            override fun onError(error: Int) { listening = false; correct = false; status = "لم أفهم الكلمة، حاول مرة أخرى"; audio.speakOffline("حاول مرة أخرى", "ar") }
            override fun onResults(results: Bundle?) {
                listening = false
                val options = results?.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION).orEmpty()
                heard = options.firstOrNull().orEmpty()
                val ok = options.any { arabicWordMatches(it, word) }
                correct = ok; status = if (ok) "أحسنت! قراءة صحيحة 👏" else "حاول مرة أخرى 🌟"
                if (ok) { repo.recordLesson("Arabic word reading", word, true); repo.addStars(1); audio.speakOffline("أحسنت", "ar") } else audio.speakOffline("حاول مرة أخرى", "ar")
            }
            override fun onPartialResults(partialResults: Bundle?) = Unit
            override fun onEvent(eventType: Int, params: Bundle?) = Unit
        })
    }
    Card(Modifier.fillMaxWidth().heightIn(min = 330.dp, max = 470.dp), shape = RoundedCornerShape(28.dp), colors = CardDefaults.cardColors(containerColor = Color.White), elevation = CardDefaults.cardElevation(10.dp)) {
        Column(Modifier.fillMaxSize().padding(16.dp), horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.Center) {
            Text("اقرأ الكلمة بصوتك", fontSize = 20.sp, fontWeight = FontWeight.Bold, color = Color(0xFF34526F))
            Spacer(Modifier.height(18.dp)); Text(word, fontSize = 78.sp, fontWeight = FontWeight.Black, color = Color(0xFF1F5D8C), textAlign = TextAlign.Center)
            Spacer(Modifier.height(12.dp)); Button(onClick = { audio.speakOffline(word, "ar") }, shape = RoundedCornerShape(18.dp)) { Text("🔊 اسمع الكلمة", fontSize = 17.sp) }
            Spacer(Modifier.height(10.dp)); Button(onClick = { if (androidx.core.content.ContextCompat.checkSelfPermission(context, Manifest.permission.RECORD_AUDIO) == PackageManager.PERMISSION_GRANTED) startRecognition(recognizer) else launcher.launch(Manifest.permission.RECORD_AUDIO) }, enabled = !listening && recognizer != null, modifier = Modifier.fillMaxWidth().height(56.dp), shape = RoundedCornerShape(20.dp), colors = ButtonDefaults.buttonColors(containerColor = if (listening) Color.Gray else Color(0xFF4C8BF5))) { Text(if (listening) "🎙️ أستمع..." else "🎙️ اقرأ الآن", fontSize = 20.sp, fontWeight = FontWeight.ExtraBold) }
            Spacer(Modifier.height(10.dp)); Text(status, fontSize = 19.sp, fontWeight = FontWeight.Bold, color = when (correct) { true -> Color(0xFF16833D); false -> Color(0xFFC62828); else -> Color(0xFF5C6B73) }, textAlign = TextAlign.Center)
            if (heard.isNotBlank()) Text("سمعت: $heard", fontSize = 15.sp, color = Color(0xFF53636D), modifier = Modifier.padding(top = 6.dp))
            Spacer(Modifier.height(10.dp)); Button(onClick = onNext, shape = RoundedCornerShape(18.dp), colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFFF8A4C))) { Text("الكلمة التالية", fontWeight = FontWeight.ExtraBold) }
        }
    }
}

private fun startRecognition(recognizer: SpeechRecognizer?) {
    if (recognizer == null) return
    val intent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
        putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM); putExtra(RecognizerIntent.EXTRA_LANGUAGE, "ar-IQ"); putExtra(RecognizerIntent.EXTRA_LANGUAGE_PREFERENCE, "ar-IQ"); putExtra(RecognizerIntent.EXTRA_MAX_RESULTS, 5); putExtra(RecognizerIntent.EXTRA_PARTIAL_RESULTS, false)
    }
    recognizer.startListening(intent)
}

private fun arabicWordMatches(recognized: String, target: String): Boolean {
    val a = normalizeArabic(recognized); val b = normalizeArabic(target); return a == b || a.contains(b) || b.contains(a)
}
private fun normalizeArabic(value: String): String {
    val decomposed = Normalizer.normalize(value.trim().lowercase(Locale.ROOT), Normalizer.Form.NFD)
    return decomposed.replace(Regex("[\\u064B-\\u0652]"), "").replace("\u0640", "").replace(Regex("^ال"), "").replace("أ", "ا").replace("إ", "ا").replace("آ", "ا").replace("ى", "ي").replace(Regex("\\s+"), "")
}

@Composable
private fun ArabicWordWriting(word: String, audio: LocalAudioManager, repo: ProgressRepository, onNext: () -> Unit) {
    val strokes = remember(word) { mutableStateListOf<List<Offset>>() }
    var current by remember(word) { mutableStateOf<List<Offset>>(emptyList()) }
    var result by remember(word) { mutableStateOf<Boolean?>(null) }
    var guide by remember(word) { mutableStateOf(true) }
    var recognized by remember(word) { mutableStateOf("") }
    var status by remember(word) { mutableStateOf("اكتب الكلمة داخل اللوحة ثم اضغط «تحقق من الكتابة»") }
    var modelReady by remember(word) { mutableStateOf(false) }
    var modelDownloading by remember(word) { mutableStateOf(false) }
    var checking by remember(word) { mutableStateOf(false) }

    val model = remember {
        try { DigitalInkRecognitionModelIdentifier.fromLanguageTag("ar")?.let { DigitalInkRecognitionModel.builder(it).build() } } catch (_: Exception) { null }
    }
    val digitalRecognizer = remember(model) { model?.let { DigitalInkRecognition.getClient(DigitalInkRecognizerOptions.builder(it).build()) } }
    DisposableEffect(digitalRecognizer) { onDispose { digitalRecognizer?.close() } }

    LaunchedEffect(model) {
        if (model == null) { status = "نموذج الكتابة العربية غير متاح"; return@LaunchedEffect }
        val manager = RemoteModelManager.getInstance(); modelDownloading = true
        manager.isModelDownloaded(model).addOnSuccessListener { downloaded ->
            if (downloaded) { modelReady = true; modelDownloading = false; status = "النموذج جاهز — اكتب الكلمة ثم اضغط تحقق" }
            else manager.download(model, DownloadConditions.Builder().build()).addOnSuccessListener { modelReady = true; modelDownloading = false; status = "النموذج جاهز — اكتب الكلمة ثم اضغط تحقق" }.addOnFailureListener { modelDownloading = false; status = "تعذر تنزيل نموذج الكتابة. اتصل بالإنترنت ثم حاول مرة أخرى" }
        }.addOnFailureListener { modelDownloading = false; status = "تعذر فحص نموذج الكتابة" }
    }

    fun clearBoard() { strokes.clear(); current = emptyList(); result = null; recognized = ""; status = if (modelReady) "اكتب الكلمة ثم اضغط تحقق" else status }
    fun verifyWriting() {
        if (checking || strokes.isEmpty() || digitalRecognizer == null || model == null || !modelReady) return
        checking = true; result = null; status = "جارٍ تحليل خط الطفل..."
        val inkBuilder = Ink.builder()
        strokes.forEach { points ->
            if (points.isNotEmpty()) {
                val strokeBuilder = Ink.Stroke.builder()
                points.forEachIndexed { i, p -> strokeBuilder.addPoint(Ink.Point.create(p.x, p.y, System.currentTimeMillis() + i)) }
                inkBuilder.addStroke(strokeBuilder.build())
            }
        }
        val recognitionContext = RecognitionContext.builder().setWritingArea(WritingArea(900f, 300f)).build()
        digitalRecognizer.recognize(inkBuilder.build(), recognitionContext).addOnSuccessListener { recognition ->
            val candidates = recognition.candidates.take(5).map { it.text }; val best = candidates.firstOrNull().orEmpty(); recognized = candidates.joinToString("، ")
            val ok = candidates.any { arabicWordMatches(it, word) }; result = ok
            status = if (ok) "أحسنت! كتبت «$word» بشكل صحيح 👏" else if (best.isNotBlank()) "قرأت: «$best» — حاول كتابة «$word» مرة أخرى" else "لم أتعرف على الكلمة — حاول مرة أخرى"
            if (ok) { repo.recordLesson("Arabic word writing", word, true); repo.addStars(1); audio.speakOffline("أحسنت", "ar") } else audio.speakOffline("حاول مرة أخرى", "ar")
            checking = false
        }.addOnFailureListener { checking = false; result = false; status = "حدث خطأ أثناء تحليل الكتابة. تأكد من اتصال الإنترنت لتنزيل النموذج ثم أعد المحاولة" }
    }

    Column(Modifier.fillMaxWidth()) {
        Card(Modifier.fillMaxWidth(), shape = RoundedCornerShape(22.dp), colors = CardDefaults.cardColors(containerColor = Color.White), elevation = CardDefaults.cardElevation(7.dp)) {
            Column(Modifier.padding(10.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                Text("اكتب الكلمة التالية", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = Color(0xFF45606F)); Text(word, fontSize = 48.sp, fontWeight = FontWeight.Black, color = Color(0xFF2357A6), textAlign = TextAlign.Center)
                Text(if (modelDownloading) "⬇️ يتم تجهيز نموذج التعرف على الكتابة..." else if (modelReady) "✓ التعرف الذكي على الكتابة جاهز" else "⚠️ نموذج التعرف غير جاهز", fontSize = 13.sp, color = if (modelReady) Color(0xFF16833D) else Color(0xFF8A5A00), fontWeight = FontWeight.Bold)
            }
        }
        Spacer(Modifier.height(8.dp))
        Card(Modifier.fillMaxWidth().height(300.dp), shape = RoundedCornerShape(26.dp), colors = CardDefaults.cardColors(containerColor = Color.White), elevation = CardDefaults.cardElevation(9.dp)) {
            Box(Modifier.fillMaxSize().padding(8.dp)) {
                if (guide) Text(word, modifier = Modifier.fillMaxSize(), textAlign = TextAlign.Center, fontSize = 105.sp, fontWeight = FontWeight.Black, color = Color(0x22355782))
                Canvas(Modifier.fillMaxSize().background(Color(0xFFFFFEF8), RoundedCornerShape(20.dp)).pointerInput(word) {
                    detectDragGestures(onDragStart = { current = listOf(it) }, onDrag = { change, _ -> change.consume(); current = current + change.position }, onDragEnd = { if (current.isNotEmpty()) strokes.add(current); current = emptyList() }, onDragCancel = { current = emptyList() })
                }) {
                    (strokes + listOf(current)).forEach { pts ->
                        if (pts.size == 1) drawCircle(Color(0xFF245B8A), 7f, pts.first())
                        else if (pts.size > 1) { val path = Path().apply { moveTo(pts[0].x, pts[0].y); for (i in 1 until pts.size) lineTo(pts[i].x, pts[i].y) }; drawPath(path, Color(0xFF245B8A), style = androidx.compose.ui.graphics.drawscope.Stroke(width = 20f, cap = StrokeCap.Round, join = StrokeJoin.Round)) }
                    }
                }
            }
        }
        Spacer(Modifier.height(6.dp)); Text(status, fontSize = 16.sp, fontWeight = FontWeight.Bold, color = when (result) { true -> Color(0xFF16833D); false -> Color(0xFFC62828); else -> Color(0xFF5C6B73) }, textAlign = TextAlign.Center, modifier = Modifier.fillMaxWidth())
        if (recognized.isNotBlank()) Text("النتائج المحتملة: $recognized", fontSize = 13.sp, color = Color(0xFF53636D), textAlign = TextAlign.Center, modifier = Modifier.fillMaxWidth().padding(top = 3.dp))
        Spacer(Modifier.height(6.dp))
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            OutlinedButton(onClick = { guide = !guide }, modifier = Modifier.weight(1f), shape = RoundedCornerShape(16.dp)) { Text(if (guide) "إخفاء الدليل" else "إظهار الدليل") }
            OutlinedButton(onClick = ::clearBoard, modifier = Modifier.weight(1f), shape = RoundedCornerShape(16.dp)) { Text("مسح") }
        }
        Spacer(Modifier.height(6.dp))
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            Button(onClick = ::verifyWriting, enabled = modelReady && !checking && strokes.isNotEmpty(), modifier = Modifier.weight(1.4f).height(52.dp), shape = RoundedCornerShape(18.dp), colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF4C8BF5))) { Text(if (checking) "جاري التحقق..." else "✓ تحقق من الكتابة", fontWeight = FontWeight.ExtraBold) }
            Button(onClick = onNext, enabled = result == true, modifier = Modifier.weight(1f).height(52.dp), shape = RoundedCornerShape(18.dp), colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFFF8A4C))) { Text("الكلمة التالية", fontWeight = FontWeight.ExtraBold) }
        }
    }
}
