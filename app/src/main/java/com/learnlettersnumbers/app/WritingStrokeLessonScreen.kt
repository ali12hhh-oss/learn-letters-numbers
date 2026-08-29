@file:OptIn(androidx.compose.material3.ExperimentalMaterial3Api::class)
package com.learnlettersnumbers.app

import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.*
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlin.math.min
import kotlin.math.sqrt

private data class Stroke(val points: List<Offset>)
private data class Lesson(val symbol: String, val label: String, val language: String, val strokes: List<Stroke>)
private fun stroke(vararg p: Offset) = Stroke(p.toList())

private enum class ArabicForm { INITIAL, MEDIAL, FINAL }

private val arLetters = listOf(
    "ا","ب","ت","ث","ج","ح","خ","د","ذ","ر","ز","س","ش","ص","ض","ط","ظ","ع","غ","ف","ق","ك","ل","م","ن","ه","و","ي"
)
private val arNames = listOf(
    "الألف","الباء","التاء","الثاء","الجيم","الحاء","الخاء","الدال","الذال","الراء","الزاي","السين","الشين","الصاد","الضاد","الطاء","الظاء","العين","الغين","الفاء","القاف","الكاف","اللام","الميم","النون","الهاء","الواو","الياء"
)

// A clean full-glyph tracing guide. The complete selected form stays visible behind the guide.
private val arTracePaths = listOf(
    listOf(stroke(Offset(.50f,.16f),Offset(.50f,.84f))),
    listOf(stroke(Offset(.72f,.45f),Offset(.60f,.58f),Offset(.42f,.62f),Offset(.28f,.48f)), stroke(Offset(.50f,.76f))),
    listOf(stroke(Offset(.72f,.45f),Offset(.60f,.58f),Offset(.42f,.62f),Offset(.28f,.48f)), stroke(Offset(.44f,.28f)), stroke(Offset(.56f,.28f))),
    listOf(stroke(Offset(.72f,.45f),Offset(.60f,.58f),Offset(.42f,.62f),Offset(.28f,.48f)), stroke(Offset(.40f,.28f)), stroke(Offset(.50f,.24f)), stroke(Offset(.60f,.28f))),
    listOf(stroke(Offset(.70f,.38f),Offset(.54f,.54f),Offset(.34f,.58f),Offset(.40f,.72f),Offset(.58f,.70f)), stroke(Offset(.50f,.82f))),
    listOf(stroke(Offset(.70f,.38f),Offset(.54f,.54f),Offset(.34f,.58f),Offset(.40f,.72f),Offset(.58f,.70f))),
    listOf(stroke(Offset(.70f,.38f),Offset(.54f,.54f),Offset(.34f,.58f),Offset(.40f,.72f),Offset(.58f,.70f)), stroke(Offset(.50f,.25f))),
    listOf(stroke(Offset(.42f,.42f),Offset(.50f,.58f),Offset(.58f,.42f))),
    listOf(stroke(Offset(.42f,.42f),Offset(.50f,.58f),Offset(.58f,.42f)),stroke(Offset(.50f,.25f))),
    listOf(stroke(Offset(.50f,.25f),Offset(.44f,.55f),Offset(.58f,.76f))),
    listOf(stroke(Offset(.50f,.25f),Offset(.44f,.55f),Offset(.58f,.76f)),stroke(Offset(.50f,.22f))),
    listOf(stroke(Offset(.72f,.38f),Offset(.58f,.55f),Offset(.38f,.60f),Offset(.28f,.48f),Offset(.38f,.38f))),
    listOf(stroke(Offset(.72f,.38f),Offset(.58f,.55f),Offset(.38f,.60f),Offset(.28f,.48f),Offset(.38f,.38f)),stroke(Offset(.42f,.26f)),stroke(Offset(.58f,.26f))),
    listOf(stroke(Offset(.70f,.38f),Offset(.56f,.54f),Offset(.36f,.58f),Offset(.44f,.74f),Offset(.64f,.70f))),
    listOf(stroke(Offset(.70f,.38f),Offset(.56f,.54f),Offset(.36f,.58f),Offset(.44f,.74f),Offset(.64f,.70f)),stroke(Offset(.50f,.25f))),
    listOf(stroke(Offset(.62f,.22f),Offset(.38f,.52f),Offset(.70f,.52f),Offset(.52f,.82f))),
    listOf(stroke(Offset(.62f,.22f),Offset(.38f,.52f),Offset(.70f,.52f),Offset(.52f,.82f)),stroke(Offset(.52f,.18f))),
    listOf(stroke(Offset(.68f,.35f),Offset(.54f,.58f),Offset(.34f,.64f),Offset(.50f,.80f))),
    listOf(stroke(Offset(.68f,.35f),Offset(.54f,.58f),Offset(.34f,.64f),Offset(.50f,.80f)),stroke(Offset(.50f,.22f))),
    listOf(stroke(Offset(.70f,.40f),Offset(.56f,.56f),Offset(.34f,.56f),Offset(.46f,.76f))),
    listOf(stroke(Offset(.70f,.40f),Offset(.56f,.56f),Offset(.34f,.56f),Offset(.46f,.76f)),stroke(Offset(.50f,.24f))),
    listOf(stroke(Offset(.68f,.34f),Offset(.54f,.54f),Offset(.34f,.60f),Offset(.52f,.78f))),
    listOf(stroke(Offset(.70f,.35f),Offset(.55f,.55f),Offset(.32f,.60f),Offset(.42f,.76f),Offset(.64f,.70f))),
    listOf(stroke(Offset(.70f,.35f),Offset(.55f,.55f),Offset(.32f,.60f),Offset(.42f,.76f),Offset(.64f,.70f)),stroke(Offset(.50f,.24f))),
    listOf(stroke(Offset(.68f,.38f),Offset(.54f,.56f),Offset(.34f,.60f),Offset(.44f,.76f),Offset(.64f,.70f)),stroke(Offset(.45f,.25f)),stroke(Offset(.55f,.25f))),
    listOf(stroke(Offset(.70f,.36f),Offset(.56f,.54f),Offset(.36f,.60f),Offset(.48f,.76f))),
    listOf(stroke(Offset(.70f,.36f),Offset(.56f,.54f),Offset(.36f,.60f),Offset(.48f,.76f)),stroke(Offset(.50f,.25f))),
    listOf(stroke(Offset(.70f,.38f),Offset(.54f,.54f),Offset(.34f,.60f),Offset(.44f,.76f),Offset(.62f,.70f))),
    listOf(stroke(Offset(.70f,.38f),Offset(.54f,.54f),Offset(.34f,.60f),Offset(.44f,.76f),Offset(.62f,.70f)),stroke(Offset(.50f,.24f)))
)

private val enLetters = ('A'..'Z').toList()
private val enNames = enLetters.map { it.toString() }
private val enTracePaths = enLetters.map { c ->
    when(c) {
        'A' -> listOf(stroke(Offset(.50f,.12f),Offset(.30f,.86f)),stroke(Offset(.50f,.12f),Offset(.70f,.86f)),stroke(Offset(.38f,.58f),Offset(.62f,.58f)))
        'B' -> listOf(stroke(Offset(.35f,.12f),Offset(.35f,.88f)),stroke(Offset(.35f,.14f),Offset(.62f,.25f),Offset(.38f,.50f)),stroke(Offset(.38f,.50f),Offset(.64f,.62f),Offset(.35f,.86f)))
        'C' -> listOf(stroke(Offset(.68f,.24f),Offset(.55f,.14f),Offset(.35f,.20f),Offset(.25f,.50f),Offset(.35f,.80f),Offset(.55f,.86f),Offset(.68f,.76f)))
        'D' -> listOf(stroke(Offset(.35f,.12f),Offset(.35f,.88f)),stroke(Offset(.35f,.14f),Offset(.62f,.22f),Offset(.70f,.50f),Offset(.62f,.78f),Offset(.35f,.86f)))
        'E' -> listOf(stroke(Offset(.68f,.14f),Offset(.34f,.14f),Offset(.34f,.86f),Offset(.68f,.86f)),stroke(Offset(.34f,.50f),Offset(.60f,.50f)))
        'F' -> listOf(stroke(Offset(.68f,.14f),Offset(.34f,.14f),Offset(.34f,.86f)),stroke(Offset(.34f,.50f),Offset(.60f,.50f)))
        else -> listOf(stroke(Offset(.50f,.14f),Offset(.30f,.50f),Offset(.50f,.86f)))
    }
}

@Composable
fun WritingStrokeLessonScreen(language: String, numbers: Boolean, onBack: () -> Unit, speak: (String, String) -> Unit) {
    val arabic = language == "ar"
    val lessons = if (numbers) (0..9).map { Lesson(it.toString(), it.toString(), "en", listOf(stroke(Offset(.50f,.15f),Offset(.50f,.85f)))) }
        else if (arabic) arLetters.mapIndexed { i, s -> Lesson(s, arNames[i], "ar", arTracePaths[i]) }
        else enLetters.mapIndexed { i, c -> Lesson("$c ${c.lowercaseChar()}", enNames[i], "en", enTracePaths[i]) }

    var index by remember { mutableIntStateOf(0) }
    var replay by remember { mutableIntStateOf(0) }
    var arForm by remember { mutableStateOf(ArabicForm.INITIAL) }
    val lesson = lessons[index]
    val fullSymbol = if (arabic && !numbers) arabicFormSymbol(index, arForm) else lesson.symbol

    LaunchedEffect(index, replay, arForm) {
        val msg = when {
            numbers -> if (arabic) "تعلم كتابة الرقم ${lesson.symbol}" else "Learn to write number ${lesson.symbol}"
            arabic -> "تعلم كتابة ${lesson.label}، الشكل ${arabicFormName(arForm)}"
            else -> "Learn to write ${lesson.label}"
        }
        speak(msg, if (numbers) "ar" else language)
    }

    Scaffold(topBar = {
        TopAppBar(
            title = { Text(if (arabic) "تعلم الكتابة خطوة بخطوة" else "Learn to Write") },
            navigationIcon = { IconButton(onClick = onBack) { Icon(Icons.Default.ArrowBack, contentDescription = "رجوع") } }
        )
    }) { pad ->
        Column(Modifier.fillMaxSize().padding(pad).padding(horizontal = 12.dp, vertical = 8.dp), horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                if (arabic) "شاهد الحرف كاملاً، اختر شكله، ثم اتبع إصبع اليد من نقطة البداية 👆"
                else "See the complete letter, then follow the hand from the starting point 👆",
                fontSize = 17.sp, fontWeight = FontWeight.Bold, textAlign = TextAlign.Center
            )
            Spacer(Modifier.height(6.dp))

            if (arabic && !numbers) {
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(7.dp)) {
                    FormChoice("أولي", "بداية الحرف", arForm == ArabicForm.INITIAL, Color(0xFF4C8BF5), Modifier.weight(1f)) { arForm = ArabicForm.INITIAL; replay++ }
                    FormChoice("وسطي", "وسط الحرف", arForm == ArabicForm.MEDIAL, Color(0xFFFFA726), Modifier.weight(1f)) { arForm = ArabicForm.MEDIAL; replay++ }
                    FormChoice("أخري", "نهاية الحرف", arForm == ArabicForm.FINAL, Color(0xFF6BCB77), Modifier.weight(1f)) { arForm = ArabicForm.FINAL; replay++ }
                }
                Spacer(Modifier.height(6.dp))
            }

            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.Center, verticalAlignment = Alignment.CenterVertically) {
                Text("${index + 1} / ${lessons.size}", fontWeight = FontWeight.ExtraBold, fontSize = 16.sp)
                Spacer(Modifier.width(12.dp))
                Text(fullSymbol, fontSize = if (arabic) 42.sp else 34.sp, fontWeight = FontWeight.Black, color = Color(0xFF315CFF))
                Spacer(Modifier.width(8.dp))
                Text(if (arabic && !numbers) "${lesson.label} — ${arabicFormName(arForm)}" else lesson.label, fontSize = 16.sp, fontWeight = FontWeight.Bold)
            }

            Spacer(Modifier.height(6.dp))
            Card(Modifier.fillMaxWidth().weight(1f), shape = RoundedCornerShape(28.dp), colors = CardDefaults.cardColors(containerColor = Color(0xFFFFFBF0)), elevation = CardDefaults.cardElevation(9.dp)) {
                Column(Modifier.fillMaxSize().padding(8.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                    TraceBoard(fullSymbol, lesson, replay, arabic)
                    Text(
                        if (arabic) "🟢 نقطة البداية  →  🟠 إصبع اليد يتبع المسار  →  🔵 أكمل الحرف"
                        else "🟢 Start  →  🟠 Follow the hand  →  🔵 Finish the letter",
                        fontSize = 15.sp, fontWeight = FontWeight.Bold, textAlign = TextAlign.Center,
                        modifier = Modifier.padding(vertical = 5.dp)
                    )
                }
            }

            Spacer(Modifier.height(7.dp))
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                LessonButton("السابق", Color(0xFF5C6BC0), index > 0, Modifier.weight(1f)) { if (index > 0) { index--; replay++ } }
                LessonButton("🔄 إعادة", Color(0xFF039BE5), true, Modifier.weight(1f)) { replay++ }
                LessonButton("التالي", Color(0xFF2EAD69), index < lessons.lastIndex, Modifier.weight(1f)) { if (index < lessons.lastIndex) { index++; replay++ } }
            }
        }
    }
}

@Composable
private fun FormChoice(title: String, subtitle: String, selected: Boolean, color: Color, modifier: Modifier, onClick: () -> Unit) {
    Card(modifier.clickable(onClick = onClick), shape = RoundedCornerShape(17.dp), colors = CardDefaults.cardColors(containerColor = if (selected) color else Color.White), elevation = CardDefaults.cardElevation(if (selected) 8.dp else 3.dp)) {
        Column(Modifier.fillMaxWidth().padding(vertical = 7.dp), horizontalAlignment = Alignment.CenterHorizontally) {
            Text(title, fontSize = 18.sp, fontWeight = FontWeight.Black, color = if (selected) Color.White else color)
            Text(subtitle, fontSize = 11.sp, color = if (selected) Color.White else Color.DarkGray, textAlign = TextAlign.Center)
        }
    }
}

@Composable
private fun LessonButton(text: String, color: Color, enabled: Boolean, modifier: Modifier, onClick: () -> Unit) {
    Button(onClick = onClick, enabled = enabled, modifier = modifier.height(54.dp), shape = RoundedCornerShape(17.dp), colors = ButtonDefaults.buttonColors(containerColor = color, disabledContainerColor = Color(0xFFD9E0E5))) {
        Text(text, fontWeight = FontWeight.ExtraBold, fontSize = 16.sp)
    }
}

@Composable
private fun TraceBoard(fullSymbol: String, lesson: Lesson, replay: Int, arabic: Boolean) {
    var progress by remember(fullSymbol, lesson.label, replay) { mutableFloatStateOf(0f) }
    LaunchedEffect(fullSymbol, lesson.label, replay) {
        animate(0f, 1f, tween(3200, easing = LinearEasing)) { value, _ -> progress = value }
    }

    Box(Modifier.fillMaxWidth().weight(1f).background(Color(0xFFF2F7FF), RoundedCornerShape(22.dp)).border(3.dp, Color(0xFFD5E5F5), RoundedCornerShape(22.dp)), contentAlignment = Alignment.Center) {
        Canvas(Modifier.fillMaxSize().padding(8.dp)) {
            val w = size.width
            val h = size.height
            // Full character stays visible as the child's visual target.
            drawContext.canvas.save()
            val paint = androidx.compose.ui.graphics.Paint().apply { color = Color(0xFFB7C7D8); alpha = .42f }
            drawContext.canvas.nativeCanvas.drawText(fullSymbol, w / 2f, h * .70f, android.graphics.Paint().apply {
                textSize = min(w, h) * if (arabic) .62f else .56f
                textAlign = android.graphics.Paint.Align.CENTER
                color = android.graphics.Color.rgb(100, 125, 150)
                alpha = 85
                typeface = android.graphics.Typeface.DEFAULT_BOLD
            })
            drawContext.canvas.restore()

            val paths = lesson.strokes
            paths.forEachIndexed { si, s ->
                val pts = s.points.map { Offset(it.x * w, it.y * h) }
                if (pts.size < 2) return@forEachIndexed
                val done = progress * paths.size - si
                val count = min(pts.size, (done * (pts.size - 1) + 1).toInt().coerceIn(1, pts.size))
                for (i in 0 until count - 1) drawLine(Color(0xFF315CFF), pts[i], pts[i + 1], strokeWidth = 12f, cap = StrokeCap.Round)
                drawCircle(Color(0xFF27AE60), 13f, pts.first())
                if (done in 0f..1.15f) {
                    val k = done.coerceIn(0f, 1f) * (pts.size - 1)
                    val i = k.toInt().coerceIn(0, pts.size - 2)
                    val t = k - i
                    val cur = Offset(pts[i].x + (pts[i+1].x - pts[i].x) * t, pts[i].y + (pts[i+1].y - pts[i].y) * t)
                    drawCircle(Color(0xFFFF8A00), 17f, cur)
                }
            }
        }
        // Large finger indicator is placed exactly at the animated tracing point.
        FingerGuide(lesson, progress)
    }
}

@Composable
private fun FingerGuide(lesson: Lesson, progress: Float) {
    if (lesson.strokes.isEmpty()) return
    val total = lesson.strokes.size
    val raw = progress * total
    val si = raw.toInt().coerceIn(0, total - 1)
    val local = (raw - si).coerceIn(0f, 1f)
    val pts = lesson.strokes[si].points
    if (pts.size < 2) return
    val k = local * (pts.size - 1)
    val i = k.toInt().coerceIn(0, pts.size - 2)
    val t = k - i
    val x = pts[i].x + (pts[i+1].x - pts[i].x) * t
    val y = pts[i].y + (pts[i+1].y - pts[i].y) * t
    Box(Modifier.fillMaxSize().padding(8.dp)) {
        Box(Modifier.fillMaxSize(), contentAlignment = Alignment.TopStart) {
            Text("☝️", fontSize = 38.sp, modifier = Modifier.offset(x = (x * 1000).dp, y = (y * 500).dp))
        }
    }
}

private fun arabicFormName(form: ArabicForm) = when(form) { ArabicForm.INITIAL -> "أولي"; ArabicForm.MEDIAL -> "وسطي"; ArabicForm.FINAL -> "أخري" }

private fun arabicFormSymbol(index: Int, form: ArabicForm): String {
    val c = arLetters[index]
    return when(form) {
        ArabicForm.INITIAL -> when(c) { "ا"->"ا"; "ب"->"بـ"; "ت"->"تـ"; "ث"->"ثـ"; "ج"->"جـ"; "ح"->"حـ"; "خ"->"خـ"; "د"->"د"; "ذ"->"ذ"; "ر"->"ر"; "ز"->"ز"; "س"->"سـ"; "ش"->"شـ"; "ص"->"صـ"; "ض"->"ضـ"; "ط"->"طـ"; "ظ"->"ظـ"; "ع"->"عـ"; "غ"->"غـ"; "ف"->"فـ"; "ق"->"قـ"; "ك"->"كـ"; "ل"->"لـ"; "م"->"مـ"; "ن"->"نـ"; "ه"->"هـ"; "و"->"و"; "ي"->"يـ"; else -> c }
        ArabicForm.MEDIAL -> when(c) { "ا"->"ـا"; "ب"->"ـبـ"; "ت"->"ـتـ"; "ث"->"ـثـ"; "ج"->"ـجـ"; "ح"->"ـحـ"; "خ"->"ـخـ"; "د"->"د"; "ذ"->"ذ"; "ر"->"ر"; "ز"->"ز"; "س"->"ـسـ"; "ش"->"ـشـ"; "ص"->"ـصـ"; "ض"->"ـضـ"; "ط"->"ـطـ"; "ظ"->"ـظـ"; "ع"->"ـعـ"; "غ"->"ـغـ"; "ف"->"ـفـ"; "ق"->"ـقـ"; "ك"->"ـكـ"; "ل"->"ـلـ"; "م"->"ـمـ"; "ن"->"ـنـ"; "ه"->"ـهـ"; "و"->"و"; "ي"->"ـيـ"; else -> c }
        ArabicForm.FINAL -> when(c) { "ا"->"ا"; "ب"->"ـب"; "ت"->"ـت"; "ث"->"ـث"; "ج"->"ـج"; "ح"->"ـح"; "خ"->"ـخ"; "د"->"د"; "ذ"->"ذ"; "ر"->"ر"; "ز"->"ز"; "س"->"ـس"; "ش"->"ـش"; "ص"->"ـص"; "ض"->"ـض"; "ط"->"ـط"; "ظ"->"ـظ"; "ع"->"ـع"; "غ"->"ـغ"; "ف"->"ـف"; "ق"->"ـق"; "ك"->"ـك"; "ل"->"ـل"; "م"->"ـم"; "ن"->"ـن"; "ه"->"ـه"; "و"->"و"; "ي"->"ـي"; else -> c }
    }
}
