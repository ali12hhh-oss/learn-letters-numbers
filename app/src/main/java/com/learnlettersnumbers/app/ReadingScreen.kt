package com.learnlettersnumbers.app

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.consumePositionChange
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.platform.LocalLayoutDirection

private enum class ReadingMode { LETTERS, NUMBERS, WORDS }
private enum class ReadingLetterForm { INITIAL, MEDIAL, FINAL }
private data class ReadingStroke(val points: List<Offset>, val color: Color)

private val readingArabicLetters = listOf("ا","ب","ت","ث","ج","ح","خ","د","ذ","ر","ز","س","ش","ص","ض","ط","ظ","ع","غ","ف","ق","ك","ل","م","ن","ه","و","ي")
private val readingInitial = listOf("ا","بـ","تـ","ثـ","جـ","حـ","خـ","د","ذ","ر","ز","سـ","شـ","صـ","ضـ","طـ","ظـ","عـ","غـ","فـ","قـ","كـ","لـ","مـ","نـ","هـ","و","يـ")
private val readingMedial = listOf("ـا","ـبـ","ـتـ","ـثـ","ـجـ","ـحـ","ـخـ","د","ذ","ر","ز","ـسـ","ـشـ","ـصـ","ـضـ","ـطـ","ـظـ","ـعـ","ـغـ","ـفـ","ـقـ","ـكـ","ـلـ","ـمـ","ـنـ","ـهـ","و","ـيـ")
private val readingFinal = listOf("ا","ـب","ـت","ـث","ـج","ـح","ـخ","د","ذ","ر","ز","ـس","ـش","ـص","ـض","ـط","ـظ","ـع","ـغ","ـف","ـق","ـك","ـل","ـم","ـن","ـه","و","ـي")
private val readingWords = listOf("دا","اب","با","تا","ثا","جا","حا","خا","سا","شا","صا","ضا","طا","ظا","عا","غا","فا","قا","كا","لا","ما","نا","ها","وا","يا","دو","بو","تو","مو","نو","لي","مي","في","قي","كي","لو","هو","دي","ري","شي","سو","شو","فو","كو","من","هل","بل","قد","لم","لن","عن","رب","حب","جد","خذ","زر","سر","شد","صد","عد","غد")

private fun readingArabicDigits(n: Int): String = n.toString().map { "٠١٢٣٤٥٦٧٨٩"[it - '0'] }.joinToString("")

@Composable
internal fun ReadingScreen(audio: LocalAudioManager, onTap: () -> Unit, onBack: () -> Unit, soundsEnabled: () -> Boolean = { true }) {
    var mode by remember { mutableStateOf(ReadingMode.LETTERS) }
    var form by remember { mutableStateOf(ReadingLetterForm.INITIAL) }
    var index by remember { mutableIntStateOf(0) }
    var inkColor by remember { mutableStateOf(Color(0xFF3F51B5)) }
    var strokes by remember { mutableStateOf(emptyList<ReadingStroke>()) }
    var current by remember { mutableStateOf(emptyList<Offset>()) }
    var showIsolatedLetters by remember { mutableStateOf(false) }

    val total = when (mode) { ReadingMode.LETTERS -> 28; ReadingMode.NUMBERS -> 100; ReadingMode.WORDS -> readingWords.size }
    val currentIndex = index.coerceIn(0, total - 1)
    val target = when (mode) {
        ReadingMode.LETTERS -> if (showIsolatedLetters) readingArabicLetters[currentIndex] else when (form) {
            ReadingLetterForm.INITIAL -> readingInitial[currentIndex]
            ReadingLetterForm.MEDIAL -> readingMedial[currentIndex]
            ReadingLetterForm.FINAL -> readingFinal[currentIndex]
        }
        ReadingMode.NUMBERS -> readingArabicDigits(currentIndex + 1)
        ReadingMode.WORDS -> readingWords[currentIndex]
    }

    LaunchedEffect(mode, currentIndex, form, showIsolatedLetters) {
        if (!soundsEnabled()) return@LaunchedEffect
        when (mode) {
            ReadingMode.LETTERS -> audio.playRequired("ar_letter_%02d_sound".format(currentIndex + 1))
            ReadingMode.NUMBERS -> audio.playRequired("ar_number_%03d".format(currentIndex + 1))
            ReadingMode.WORDS -> {
                // In WORDS mode we must pronounce the two-letter combination as ONE Arabic unit.
                // Do not concatenate isolated letter sounds (بَ + اَ), because that can turn
                // the exercise into a letter name or an unintended word such as "ماء".
                audio.speakOffline(readingWords[currentIndex], "ar")
            }
        }
    }

    fun clear() { strokes = emptyList(); current = emptyList() }
    fun next() { index = (currentIndex + 1) % total; clear(); onTap() }
    fun previous() { index = (currentIndex - 1 + total) % total; clear(); onTap() }

    CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Rtl) {
        Column(Modifier.fillMaxSize().background(Brush.verticalGradient(listOf(MaterialTheme.colorScheme.background, MaterialTheme.colorScheme.surfaceVariant))).padding(horizontal = 9.dp, vertical = 5.dp), horizontalAlignment = Alignment.CenterHorizontally) {
            Row(Modifier.fillMaxWidth().height(48.dp), verticalAlignment = Alignment.CenterVertically) {
                Button(onClick = { onBack(); onTap() }, modifier = Modifier.height(42.dp), shape = RoundedCornerShape(14.dp)) { Text("رجوع", fontWeight = FontWeight.Bold) }
                Spacer(Modifier.weight(1f))
                Text("القراءة والكتابة", fontSize = 26.sp, fontWeight = FontWeight.Black, color = Color(0xFF155B83))
            }
            Row(Modifier.fillMaxWidth().height(54.dp), horizontalArrangement = Arrangement.spacedBy(7.dp)) {
                ReadingModeButton("الحروف", mode == ReadingMode.LETTERS, Modifier.weight(1f)) { mode = ReadingMode.LETTERS; index = 0; clear(); showIsolatedLetters = false; onTap() }
                ReadingModeButton("الأرقام", mode == ReadingMode.NUMBERS, Modifier.weight(1f)) { mode = ReadingMode.NUMBERS; index = 0; clear(); showIsolatedLetters = false; onTap() }
                ReadingModeButton("كلمات", mode == ReadingMode.WORDS, Modifier.weight(1f)) { mode = ReadingMode.WORDS; index = 0; clear(); showIsolatedLetters = false; onTap() }
            }
            if (mode == ReadingMode.LETTERS) {
                Spacer(Modifier.height(4.dp))
                Row(Modifier.fillMaxWidth().height(50.dp), horizontalArrangement = Arrangement.spacedBy(7.dp)) {
                    ReadingFormButton("الحروف", showIsolatedLetters, Color(0xFF26A69A), Modifier.weight(1f)) { index = 0; showIsolatedLetters = true; onTap() }
                    ReadingFormButton("أولي", !showIsolatedLetters && form == ReadingLetterForm.INITIAL, Color(0xFF4C8BF5), Modifier.weight(1f)) { showIsolatedLetters = false; form = ReadingLetterForm.INITIAL; onTap() }
                    ReadingFormButton("وسطي", !showIsolatedLetters && form == ReadingLetterForm.MEDIAL, Color(0xFFFFA726), Modifier.weight(1f)) { showIsolatedLetters = false; form = ReadingLetterForm.MEDIAL; onTap() }
                    ReadingFormButton("أخري", !showIsolatedLetters && form == ReadingLetterForm.FINAL, Color(0xFF43A047), Modifier.weight(1f)) { showIsolatedLetters = false; form = ReadingLetterForm.FINAL; onTap() }
                }
            }
            Row(Modifier.fillMaxWidth().height(50.dp), horizontalArrangement = Arrangement.Center, verticalAlignment = Alignment.CenterVertically) {
                Text("${currentIndex + 1} / $total", fontWeight = FontWeight.ExtraBold, fontSize = 13.sp)
                Spacer(Modifier.width(10.dp))
                Text(target, fontSize = 34.sp, fontWeight = FontWeight.Black, color = Color(0xFF294C78), textAlign = TextAlign.Center)
            }
            Card(Modifier.fillMaxWidth().weight(1f), shape = RoundedCornerShape(25.dp), colors = CardDefaults.cardColors(containerColor = Color(0xFFF7FBFF)), elevation = CardDefaults.cardElevation(9.dp)) {
                Column(Modifier.fillMaxSize().padding(6.dp)) {
                    Canvas(Modifier.fillMaxWidth().weight(1f).background(Color.White, RoundedCornerShape(20.dp)).border(2.dp, Color(0xFFD9E8F0), RoundedCornerShape(20.dp)).pointerInput(inkColor) {
                        detectTapGestures(onTap = { point -> strokes = strokes + ReadingStroke(listOf(point), inkColor); onTap() })
                    }.pointerInput(inkColor) {
                        detectDragGestures(onDragStart = { point -> current = listOf(point) }, onDrag = { change, _ -> change.consumePositionChange(); current = current + change.position }, onDragEnd = { if (current.isNotEmpty()) strokes = strokes + ReadingStroke(current, inkColor); current = emptyList() }, onDragCancel = { current = emptyList() })
                    }) {
                        strokes.forEach { s -> if (s.points.size == 1) drawCircle(color = s.color, radius = 12f, center = s.points.first()) else drawPath(pathOfReading(s.points), color = s.color, style = Stroke(width = 36f, cap = StrokeCap.Round, join = StrokeJoin.Round)) }
                        if (current.size == 1) drawCircle(color = inkColor, radius = 12f, center = current.first())
                        if (current.size > 1) drawPath(pathOfReading(current), color = inkColor, style = Stroke(width = 36f, cap = StrokeCap.Round, join = StrokeJoin.Round))
                    }
                    Spacer(Modifier.height(5.dp))
                    Row(Modifier.fillMaxWidth().height(38.dp), horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        ReadingColorButton("أزرق", Color(0xFF3F51B5), inkColor == Color(0xFF3F51B5), Modifier.weight(1f)) { inkColor = Color(0xFF3F51B5) }
                        ReadingColorButton("أخضر", Color(0xFF2E9B62), inkColor == Color(0xFF2E9B62), Modifier.weight(1f)) { inkColor = Color(0xFF2E9B62) }
                        ReadingColorButton("وردي", Color(0xFFE64A78), inkColor == Color(0xFFE64A78), Modifier.weight(1f)) { inkColor = Color(0xFFE64A78) }
                    }
                }
            }
            Spacer(Modifier.height(5.dp))
            Row(Modifier.fillMaxWidth().height(56.dp), horizontalArrangement = Arrangement.spacedBy(7.dp)) {
                ReadingNavButton("السابق", Color(0xFF5C6BC0), Modifier.weight(1f)) { previous() }
                ReadingNavButton("مسح", Color(0xFFE85D5D), Modifier.weight(1f)) { clear(); onTap() }
                ReadingNavButton("التالي", Color(0xFF2EAD69), Modifier.weight(1f)) { next() }
                ReadingNavButton("🔊", Color(0xFF039BE5), Modifier.weight(0.75f)) {
                    if (soundsEnabled()) {
                        when (mode) {
                            ReadingMode.LETTERS -> audio.playRequired("ar_letter_%02d_sound".format(currentIndex + 1))
                            ReadingMode.NUMBERS -> audio.playRequired("ar_number_%03d".format(currentIndex + 1))
                            ReadingMode.WORDS -> audio.speakOffline(readingWords[currentIndex], "ar")
                        }
                    }
                    onTap()
                }
            }
        }
    }
}

private fun pathOfReading(points: List<Offset>): Path = Path().apply { if (points.isEmpty()) return@apply; moveTo(points.first().x, points.first().y); for (i in 1 until points.size) lineTo(points[i].x, points[i].y) }

@Composable private fun ReadingModeButton(text: String, selected: Boolean, modifier: Modifier, onClick: () -> Unit) { Button(onClick = onClick, modifier = modifier.fillMaxHeight(), shape = RoundedCornerShape(15.dp), colors = ButtonDefaults.buttonColors(containerColor = if (selected) Color(0xFF4C8BF5) else Color.White, contentColor = if (selected) Color.White else Color(0xFF315CFF))) { Text(text, fontWeight = FontWeight.ExtraBold) } }

@Composable private fun ReadingFormButton(text: String, selected: Boolean, color: Color, modifier: Modifier, onClick: () -> Unit) { Button(onClick = onClick, modifier = modifier.fillMaxHeight(), shape = RoundedCornerShape(15.dp), colors = ButtonDefaults.buttonColors(containerColor = if (selected) color else Color.White, contentColor = if (selected) Color.White else color)) { Text(text, fontWeight = FontWeight.ExtraBold) } }

@Composable private fun ReadingColorButton(text: String, color: Color, selected: Boolean, modifier: Modifier, onClick: () -> Unit) { Button(onClick = onClick, modifier = modifier.fillMaxHeight(), shape = RoundedCornerShape(12.dp), colors = ButtonDefaults.buttonColors(containerColor = if (selected) color else Color.White, contentColor = if (selected) Color.White else color)) { Text(text, fontSize = 11.sp, fontWeight = FontWeight.Bold) } }

@Composable private fun ReadingNavButton(text: String, color: Color, modifier: Modifier, onClick: () -> Unit) { Button(onClick = onClick, modifier = modifier.fillMaxHeight(), shape = RoundedCornerShape(15.dp), colors = ButtonDefaults.buttonColors(containerColor = color)) { Text(text, fontWeight = FontWeight.ExtraBold) } }