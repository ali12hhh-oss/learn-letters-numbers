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
import java.text.Normalizer
import java.util.Locale

private data class ArabicWord(val text: String, val letters: Int)
private val arabicWords = listOf(
    ArabicWord("دار",3), ArabicWord("دور",3), ArabicWord("بان",3), ArabicWord("باب",3), ArabicWord("بيت",3),
    ArabicWord("قلم",3), ArabicWord("قمر",3), ArabicWord("شمس",3), ArabicWord("أسد",3), ArabicWord("نمر",3),
    ArabicWord("جمل",3), ArabicWord("فيل",3), ArabicWord("موز",3), ArabicWord("تين",3), ArabicWord("عنب",3),
    ArabicWord("درس",3), ArabicWord("حرف",3), ArabicWord("لون",3), ArabicWord("علم",3), ArabicWord("نور",3),
    ArabicWord("كتاب",4), ArabicWord("دفتر",4), ArabicWord("حروف",4), ArabicWord("تفاح",4), ArabicWord("رمان",4),
    ArabicWord("حصان",4), ArabicWord("سمكة",4), ArabicWord("طائر",4), ArabicWord("نحلة",4), ArabicWord("بقرة",4),
    ArabicWord("وردة",4), ArabicWord("زهرة",4)
)

@Composable
fun ArabicWordsScreen(audio: LocalAudioManager, repo: ProgressRepository, onBack: () -> Unit) {
    var mode by remember { mutableStateOf("read") }
    var length by remember { mutableIntStateOf(3) }
    var index by remember { mutableIntStateOf(0) }
    val words = remember(length) { arabicWords.filter { it.letters == length } }
    val word = words[index.coerceIn(0, words.lastIndex)].text
    val previous = { index = if (index > 0) index - 1 else words.lastIndex }
    val next = { index = if (index < words.lastIndex) index + 1 else 0 }
    CompositionLocalProvider(androidx.compose.ui.platform.LocalLayoutDirection provides androidx.compose.ui.unit.LayoutDirection.Rtl) {
        Column(Modifier.fillMaxSize().background(Color(0xFFF3FAFF)).padding(10.dp), horizontalAlignment = Alignment.CenterHorizontally) {
            Row(Modifier.fillMaxWidth().height(52.dp), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                Button(onClick = onBack, modifier = Modifier.height(46.dp), shape = RoundedCornerShape(16.dp)) { Text("رجوع") }
                Text("القراءة", fontSize = 29.sp, fontWeight = FontWeight.ExtraBold, color = Color(0xFF2357A6))
            }
            Spacer(Modifier.height(5.dp))
            Row(Modifier.fillMaxWidth().height(54.dp), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                ModeButton("📖 اقرءا كلمات", mode == "read", Color(0xFF4C8BF5), Modifier.weight(1f)) { mode = "read" }
                ModeButton("✏️ اكتب كلمات", mode == "write", Color(0xFF6BCB77), Modifier.weight(1f)) { mode = "write" }
            }
            Spacer(Modifier.height(5.dp))
            Row(Modifier.fillMaxWidth().height(44.dp), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                ModeButton("3 حروف", length == 3, Color(0xFFFF8A4C), Modifier.weight(1f)) { length = 3; index = 0 }
                ModeButton("4 حروف", length == 4, Color(0xFF9B72E8), Modifier.weight(1f)) { length = 4; index = 0 }
            }
            Spacer(Modifier.height(7.dp))
            if (mode == "read") ReadingWord(word, audio, repo, Modifier.weight(1f), previous, next)
            else WritingWord(word, audio, Modifier.weight(1f), previous, next)
        }
    }
}

@Composable private fun ModeButton(text: String, selected: Boolean, color: Color, modifier: Modifier, onClick: () -> Unit) {
    Button(onClick = onClick, modifier = modifier.fillMaxHeight(), shape = RoundedCornerShape(16.dp), colors = ButtonDefaults.buttonColors(containerColor = if (selected) color else Color.White)) {
        Text(text, fontWeight = FontWeight.ExtraBold, color = if (selected) Color.White else Color(0xFF34526F), textAlign = TextAlign.Center)
    }
}

@Composable private fun ReadingWord(word: String, audio: LocalAudioManager, repo: ProgressRepository, modifier: Modifier, previous: () -> Unit, next: () -> Unit) {
    val context = LocalContext.current
    var status by remember(word) { mutableStateOf("اضغط «اقرأ الآن» ثم اقرأ الكلمة بصوت واضح") }
    var heard by remember(word) { mutableStateOf("") }
    var correct by remember(word) { mutableStateOf<Boolean?>(null) }
    var failures by remember(word) { mutableIntStateOf(0) }
    var listening by remember(word) { mutableStateOf(false) }
    val recognizer = remember(context) { if (SpeechRecognizer.isRecognitionAvailable(context)) SpeechRecognizer.createSpeechRecognizer(context) else null }
    val permission = rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()) { if (it) startRecognition(recognizer) else status = "نحتاج إذن الميكروفون" }
    DisposableEffect(recognizer) { onDispose { recognizer?.destroy() } }
    LaunchedEffect(recognizer, word) { recognizer?.setRecognitionListener(object : RecognitionListener {
        override fun onReadyForSpeech(p: Bundle?) { listening = true; status = "استمع إليك... اقرأ الكلمة الآن" }
        override fun onBeginningOfSpeech() { status = "أكمل قراءة الكلمة..." }
        override fun onRmsChanged(v: Float) = Unit
        override fun onBufferReceived(b: ByteArray?) = Unit
        override fun onEndOfSpeech() { listening = false; status = "جارٍ التحقق..." }
        override fun onError(e: Int) { listening = false; failures++; correct = false; status = "لم أفهم الكلمة، حاول مرة أخرى" }
        override fun onResults(r: Bundle?) {
            listening = false
            val options = r?.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION).orEmpty()
            heard = options.firstOrNull().orEmpty()
            if (options.any { normalizeArabic(it) == normalizeArabic(word) }) { correct = true; failures = 0; status = "أحسنت! قراءة صحيحة 👏"; repo.recordLesson("Arabic word reading", word, true); repo.addStars(1) }
            else { failures++; correct = false; status = if (heard.isBlank()) "لم أتعرف على الكلمة" else "سمعت: «$heard» — حاول مرة أخرى" }
        }
        override fun onPartialResults(r: Bundle?) = Unit
        override fun onEvent(t: Int, p: Bundle?) = Unit
    }) }
    Card(modifier, shape = RoundedCornerShape(28.dp), colors = CardDefaults.cardColors(Color.White), elevation = CardDefaults.cardElevation(10.dp)) {
        Column(Modifier.fillMaxSize().padding(14.dp), horizontalAlignment = Alignment.CenterHorizontally) {
            Column(Modifier.weight(1f).fillMaxWidth(), horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.Center) {
                Text("اقرأ الكلمة بصوتك", fontSize = 20.sp, fontWeight = FontWeight.Bold, color = Color(0xFF34526F))
                Text(word, fontSize = 76.sp, fontWeight = FontWeight.Black, color = Color(0xFF1F5D8C), textAlign = TextAlign.Center)
                Spacer(Modifier.height(12.dp))
                Button(onClick = { if (androidx.core.content.ContextCompat.checkSelfPermission(context, Manifest.permission.RECORD_AUDIO) == PackageManager.PERMISSION_GRANTED) startRecognition(recognizer) else permission.launch(Manifest.permission.RECORD_AUDIO) }, enabled = !listening && recognizer != null, modifier = Modifier.fillMaxWidth().height(54.dp), shape = RoundedCornerShape(18.dp)) { Text(if (listening) "🎙️ أستمع..." else "🎙️ اقرأ الآن", fontSize = 19.sp, fontWeight = FontWeight.ExtraBold) }
                if (failures >= 2) { Spacer(Modifier.height(8.dp)); Button(onClick = { audio.speakOffline(word, "ar") }, modifier = Modifier.fillMaxWidth().height(48.dp), shape = RoundedCornerShape(16.dp)) { Text("🔊 اسمع الكلمة", fontSize = 18.sp, fontWeight = FontWeight.ExtraBold) } }
                Spacer(Modifier.height(8.dp)); Text(status, fontSize = 16.sp, fontWeight = FontWeight.Bold, color = when(correct){true->Color(0xFF16833D);false->Color(0xFFC62828);else->Color(0xFF5C6B73)}, textAlign = TextAlign.Center)
                if (heard.isNotBlank()) Text("سمعت: $heard", fontSize = 14.sp, color = Color(0xFF53636D))
            }
            Navigation(previous, next)
        }
    }
}

@Composable private fun WritingWord(word: String, audio: LocalAudioManager, modifier: Modifier, previous: () -> Unit, next: () -> Unit) {
    val strokes = remember(word) { mutableStateListOf<List<Offset>>() }
    var current by remember(word) { mutableStateOf<List<Offset>>(emptyList()) }
    var message by remember(word) { mutableStateOf("اكتب الكلمة داخل السبورة") }
    fun clear() { strokes.clear(); current = emptyList(); message = "اكتب الكلمة داخل السبورة" }
    Card(modifier, shape = RoundedCornerShape(28.dp), colors = CardDefaults.cardColors(Color.White), elevation = CardDefaults.cardElevation(10.dp)) {
        Column(Modifier.fillMaxSize().padding(10.dp), horizontalAlignment = Alignment.CenterHorizontally) {
            Text("اكتب الكلمة", fontSize = 20.sp, fontWeight = FontWeight.ExtraBold, color = Color(0xFF34526F))
            Text(word, fontSize = 52.sp, fontWeight = FontWeight.Black, color = Color(0xFF1F5D8C))
            Box(Modifier.fillMaxWidth().weight(1f).padding(vertical = 6.dp).background(Color(0xFFFCFEFF), RoundedCornerShape(20.dp))) {
                Canvas(Modifier.fillMaxSize().pointerInput(word) {
                    awaitEachGesture {
                        val down = awaitFirstDown(requireUnconsumed = false); down.consume(); current = listOf(down.position)
                        var active = true
                        while (active) {
                            val event = awaitPointerEvent(); val change = event.changes.firstOrNull { it.id == down.id }
                            if (change == null) active = false else { if (change.position != current.lastOrNull()) current = current + change.position; change.consume(); if (!change.pressed) active = false }
                        }
                        if (current.isNotEmpty()) strokes.add(current); current = emptyList()
                    }
                }) {
                    drawLine(Color(0xFFD8E3EC), Offset(24f, size.height / 2f), Offset(size.width - 24f, size.height / 2f), 2f)
                    (strokes.toList() + listOfNotNull(current.takeIf { it.isNotEmpty() })).forEach { points ->
                        if (points.size == 1) drawCircle(Color(0xFF2357A6), 13f, points[0]) else { val path = Path(); points.forEachIndexed { i,p -> if(i==0) path.moveTo(p.x,p.y) else path.lineTo(p.x,p.y) }; drawPath(path, Color(0xFF2357A6), style = androidx.compose.ui.graphics.drawscope.Stroke(width=24f, cap=StrokeCap.Round, join=StrokeJoin.Round)) }
                    }
                }
            }
            Text(message, modifier = Modifier.fillMaxWidth().heightIn(min=28.dp,max=42.dp), fontSize=14.sp, fontWeight=FontWeight.Bold, textAlign=TextAlign.Center, color=Color(0xFF5C6B73))
            Row(Modifier.fillMaxWidth().height(52.dp), horizontalArrangement=Arrangement.spacedBy(6.dp)) {
                OutlinedButton(onClick=previous, modifier=Modifier.weight(1f).fillMaxHeight(), contentPadding=PaddingValues(2.dp)) { Text("السابق", fontWeight=FontWeight.ExtraBold, fontSize=14.sp) }
                OutlinedButton(onClick=::clear, modifier=Modifier.weight(1f).fillMaxHeight(), contentPadding=PaddingValues(2.dp)) { Text("مسح", fontWeight=FontWeight.ExtraBold, fontSize=14.sp) }
                Button(onClick={ audio.speakOffline(word, "ar"); message="استمع إلى الكلمة ثم اكتبها" }, modifier=Modifier.weight(1f).fillMaxHeight(), contentPadding=PaddingValues(2.dp)) { Text("اسمع الكلمة", fontWeight=FontWeight.ExtraBold, fontSize=13.sp, textAlign=TextAlign.Center) }
                Button(onClick=next, modifier=Modifier.weight(1f).fillMaxHeight(), contentPadding=PaddingValues(2.dp), colors=ButtonDefaults.buttonColors(containerColor=Color(0xFFFF8A4C))) { Text("التالي", fontWeight=FontWeight.ExtraBold, fontSize=14.sp) }
            }
        }
    }
}

@Composable private fun Navigation(previous: () -> Unit, next: () -> Unit) {
    Row(Modifier.fillMaxWidth().height(52.dp), horizontalArrangement=Arrangement.spacedBy(8.dp)) {
        OutlinedButton(onClick=previous, modifier=Modifier.weight(1f).fillMaxHeight()) { Text("السابق", fontWeight=FontWeight.ExtraBold) }
        Button(onClick=next, modifier=Modifier.weight(1f).fillMaxHeight(), colors=ButtonDefaults.buttonColors(containerColor=Color(0xFFFF8A4C))) { Text("التالي", fontWeight=FontWeight.ExtraBold) }
    }
}

private fun startRecognition(recognizer: SpeechRecognizer?) {
    if (recognizer == null) return
    recognizer.startListening(Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
        putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
        putExtra(RecognizerIntent.EXTRA_LANGUAGE, "ar-IQ")
        putExtra(RecognizerIntent.EXTRA_LANGUAGE_PREFERENCE, "ar-IQ")
        putExtra(RecognizerIntent.EXTRA_MAX_RESULTS, 5)
        putExtra(RecognizerIntent.EXTRA_PARTIAL_RESULTS, false)
        putExtra(RecognizerIntent.EXTRA_PROMPT, "اقرأ الكلمة الظاهرة بصوت واضح")
    })
}

private fun normalizeArabic(value: String): String {
    val s = Normalizer.normalize(value.trim().lowercase(Locale.ROOT), Normalizer.Form.NFD)
    return s.replace(Regex("[\\u064B-\\u0652]"), "").replace("\u0640", "").replace("أ", "ا").replace("إ", "ا").replace("آ", "ا").replace("ى", "ي").replace(Regex("\\s+"), "")
}
