@file:OptIn(androidx.compose.material3.ExperimentalMaterial3Api::class)
package com.learnlettersnumbers.app

import android.graphics.Matrix
import android.graphics.Paint
import android.graphics.Path as AndroidPath
import android.graphics.PathMeasure
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.platform.LocalLayoutDirection
import kotlin.math.atan2
import kotlin.math.min

private enum class ArabicForm { INITIAL, MEDIAL, FINAL }
private enum class WritingEnglishCase { UPPER, LOWER }

private val arLetters = listOf("ا","ب","ت","ث","ج","ح","خ","د","ذ","ر","ز","س","ش","ص","ض","ط","ظ","ع","غ","ف","ق","ك","ل","م","ن","ه","و","ي")
private val arNames = listOf("الألف","الباء","التاء","الثاء","الجيم","الحاء","الخاء","الدال","الذال","الراء","الزاي","السين","الشين","الصاد","الضاد","الطاء","الظاء","العين","الغين","الفاء","القاف","الكاف","اللام","الميم","النون","الهاء","الواو","الياء")
private val enLetters = ('A'..'Z').toList()

private fun arabicFormSymbol(i: Int, form: ArabicForm): String {
    val c = arLetters[i]
    val join = setOf("ب","ت","ث","ج","ح","خ","س","ش","ص","ض","ط","ظ","ع","غ","ف","ق","ك","ل","م","ن","ه","ي")
    return when (form) {
        ArabicForm.INITIAL -> if (c in join) "${c}ـ" else c
        ArabicForm.MEDIAL -> if (c in join) "ـ${c}ـ" else c
        ArabicForm.FINAL -> if (c in join) "ـ${c}" else c
    }
}

private fun arabicFormName(form: ArabicForm) = when (form) {
    ArabicForm.INITIAL -> "أولي"
    ArabicForm.MEDIAL -> "وسطي"
    ArabicForm.FINAL -> "أخري"
}

@Composable
fun WritingStrokeLessonScreen(language: String, numbers: Boolean, onBack: () -> Unit, speak: (String, String) -> Unit) {
    val arabic = language == "ar"
    var index by remember { mutableIntStateOf(0) }
    var form by remember { mutableStateOf(ArabicForm.INITIAL) }
    var enCase by remember { mutableStateOf(WritingEnglishCase.UPPER) }
    var replay by remember { mutableIntStateOf(0) }

    val total = when {
        numbers -> 10
        arabic -> arLetters.size
        else -> enLetters.size
    }
    val safeIndex = index.coerceIn(0, total - 1)
    val symbol = when {
        numbers -> safeIndex.toString()
        arabic -> arabicFormSymbol(safeIndex, form)
        enCase == WritingEnglishCase.UPPER -> enLetters[safeIndex].toString()
        else -> enLetters[safeIndex].lowercase()
    }
    val name = when {
        numbers -> "الرقم $symbol"
        arabic -> "${arNames[safeIndex]} — ${arabicFormName(form)}"
        enCase == WritingEnglishCase.UPPER -> "${enLetters[safeIndex]} — حروف كبيرة"
        else -> "${enLetters[safeIndex].lowercase()} — حروف صغيرة"
    }

    LaunchedEffect(safeIndex, form, enCase, replay) {
        val message = when {
            numbers -> if (arabic) "تعلم كتابة الرقم $symbol" else "Learn to write number $symbol"
            arabic -> "تعلم كتابة ${arNames[safeIndex]}، الشكل ${arabicFormName(form)}"
            enCase == WritingEnglishCase.UPPER -> "Learn to write capital letter ${enLetters[safeIndex]}"
            else -> "Learn to write lowercase letter ${enLetters[safeIndex].lowercase()}"
        }
        speak(message, language)
    }

    CompositionLocalProvider(LocalLayoutDirection provides if (arabic) LayoutDirection.Rtl else LayoutDirection.Ltr) {
        Scaffold(topBar = {
            TopAppBar(
                title = { Text(if (arabic) "تعلم الكتابة" else "Learn to Write", fontWeight = FontWeight.ExtraBold) },
                navigationIcon = { TextButton(onClick = onBack) { Text(if (arabic) "رجوع" else "Back", fontWeight = FontWeight.Bold) } }
            )
        }) { padding ->
            Column(Modifier.fillMaxSize().padding(padding).padding(10.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    if (arabic) "شاهد الحرف كاملاً، اختر شكله، ثم اتبع حركة اليد من نقطة البداية إلى النهاية 👆" else "See the complete letter, choose its case, then follow the hand from the start point to the end 👆",
                    fontSize = 17.sp,
                    fontWeight = FontWeight.Bold,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.padding(bottom = 8.dp)
                )

                if (arabic && !numbers) {
                    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        FormButton("أولي", "بداية الحرف", form == ArabicForm.INITIAL, Color(0xFF4C8BF5), Modifier.weight(1f)) { form = ArabicForm.INITIAL; replay++ }
                        FormButton("وسطي", "وسط الحرف", form == ArabicForm.MEDIAL, Color(0xFFFFA726), Modifier.weight(1f)) { form = ArabicForm.MEDIAL; replay++ }
                        FormButton("أخري", "نهاية الحرف", form == ArabicForm.FINAL, Color(0xFF43A047), Modifier.weight(1f)) { form = ArabicForm.FINAL; replay++ }
                    }
                }
                if (!arabic && !numbers) {
                    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        FormButton("UPPERCASE", "حروف كبيرة", enCase == WritingEnglishCase.UPPER, Color(0xFF4C8BF5), Modifier.weight(1f)) { enCase = WritingEnglishCase.UPPER; replay++ }
                        FormButton("lowercase", "حروف صغيرة", enCase == WritingEnglishCase.LOWER, Color(0xFFFFA8A8), Modifier.weight(1f)) { enCase = WritingEnglishCase.LOWER; replay++ }
                    }
                }

                Spacer(Modifier.height(7.dp))
                Card(Modifier.fillMaxWidth(), shape = RoundedCornerShape(18.dp), elevation = CardDefaults.cardElevation(5.dp)) {
                    Row(Modifier.fillMaxWidth().padding(horizontal = 12.dp, vertical = 7.dp), horizontalArrangement = Arrangement.Center, verticalAlignment = Alignment.CenterVertically) {
                        Text("${safeIndex + 1} / $total", fontWeight = FontWeight.ExtraBold)
                        Spacer(Modifier.width(12.dp))
                        Text(symbol, fontSize = 42.sp, fontWeight = FontWeight.Black, color = Color(0xFF315CFF))
                        Spacer(Modifier.width(10.dp))
                        Text(name, fontSize = 15.sp, fontWeight = FontWeight.Bold)
                    }
                }

                Spacer(Modifier.height(7.dp))
                Card(Modifier.fillMaxWidth().weight(1f), shape = RoundedCornerShape(28.dp), colors = CardDefaults.cardColors(containerColor = Color(0xFFFFFBF0)), elevation = CardDefaults.cardElevation(9.dp)) {
                    TraceTeachingBoard(symbol = symbol, replay = replay, arabic = arabic)
                }

                Spacer(Modifier.height(7.dp))
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    LessonButton(if (arabic) "السابق\nPrevious" else "Previous", Color(0xFF5C6BC0), safeIndex > 0, Modifier.weight(1f)) { if (safeIndex > 0) { index = safeIndex - 1; replay++ } }
                    LessonButton("🔄 ${if (arabic) "إعادة" else "Replay"}", Color(0xFF039BE5), true, Modifier.weight(1f)) { replay++ }
                    LessonButton(if (arabic) "التالي\nNext" else "Next", Color(0xFF2EAD69), safeIndex < total - 1, Modifier.weight(1f)) { if (safeIndex < total - 1) { index = safeIndex + 1; replay++ } }
                }
            }
        }
    }
}

@Composable
private fun FormButton(title: String, subtitle: String, selected: Boolean, color: Color, modifier: Modifier, onClick: () -> Unit) {
    Card(modifier = modifier.height(64.dp).clickable(onClick = onClick), shape = RoundedCornerShape(18.dp), colors = CardDefaults.cardColors(containerColor = if (selected) color else Color.White), elevation = CardDefaults.cardElevation(if (selected) 9.dp else 3.dp)) {
        Column(Modifier.fillMaxSize(), horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.Center) {
            Text(title, fontSize = 17.sp, fontWeight = FontWeight.Black, color = if (selected) Color.White else color, textAlign = TextAlign.Center)
            Text(subtitle, fontSize = 12.sp, color = if (selected) Color.White else Color.DarkGray, textAlign = TextAlign.Center)
        }
    }
}

@Composable
private fun LessonButton(text: String, color: Color, enabled: Boolean, modifier: Modifier, onClick: () -> Unit) {
    Button(onClick = onClick, enabled = enabled, modifier = modifier.height(58.dp), shape = RoundedCornerShape(18.dp), colors = ButtonDefaults.buttonColors(containerColor = color, disabledContainerColor = Color(0xFFD9E0E5))) {
        Text(text, fontWeight = FontWeight.ExtraBold, fontSize = 17.sp, textAlign = TextAlign.Center)
    }
}

private data class TeachingSample(
    val x: Float,
    val y: Float,
    val tangentX: Float,
    val tangentY: Float
)

private fun startHint(symbol: String, arabic: Boolean, w: Float, h: Float): Pair<Float, Float> {
    if (!arabic) {
        return when (symbol.lowercase()) {
            "a" -> .32f to .18f
            "b", "d", "p", "q", "r" -> .62f to .18f
            "c", "e", "o", "s" -> .72f to .35f
            "f", "t" -> .58f to .16f
            "g" -> .72f to .42f
            "h", "k", "l", "m", "n" -> .28f to .18f
            "i", "j" -> .52f to .16f
            "u", "v", "w", "x", "y", "z" -> .28f to .20f
            else -> .5f to .2f
        }.let { (x, y) -> x * w to y * h }
    }

    val base = symbol.firstOrNull { it in arLetters }?.toString() ?: symbol.firstOrNull()?.toString().orEmpty()
    val hint = when (base) {
        "ا" -> .55f to .18f
        "ب", "ت", "ث" -> .76f to .56f
        "ج", "ح", "خ" -> .76f to .30f
        "د", "ذ", "ر", "ز" -> .76f to .25f
        "س", "ش" -> .78f to .40f
        "ص", "ض" -> .76f to .30f
        "ط", "ظ" -> .65f to .18f
        "ع", "غ" -> .75f to .34f
        "ف", "ق" -> .75f to .24f
        "ك" -> .72f to .20f
        "ل" -> .60f to .16f
        "م" -> .75f to .35f
        "ن" -> .75f to .35f
        "ه" -> .70f to .35f
        "و" -> .70f to .28f
        "ي" -> .76f to .55f
        else -> .72f to .30f
    }
    return hint.first * w to hint.second * h
}

private fun sampleContour(measure: PathMeasure, startDistance: Float, steps: Int): List<TeachingSample> {
    val length = measure.length
    if (length <= 0f) return emptyList()
    val closed = measure.isClosed
    val result = ArrayList<TeachingSample>(steps + 1)
    for (i in 0..steps) {
        val raw = startDistance + length * i / steps.toFloat()
        val d = if (closed) {
            var v = raw % length
            if (v < 0f) v += length
            v
        } else raw.coerceIn(0f, length)
        val pos = FloatArray(2)
        val tan = FloatArray(2)
        measure.getPosTan(d, pos, tan)
        result += TeachingSample(pos[0], pos[1], tan[0], tan[1])
    }
    return result
}

private fun distanceSquared(a: TeachingSample, x: Float, y: Float): Float {
    val dx = a.x - x
    val dy = a.y - y
    return dx * dx + dy * dy
}

private fun buildTeachingSamples(glyphPath: AndroidPath, hintX: Float, hintY: Float): List<TeachingSample> {
    val contours = mutableListOf<Pair<Float, AndroidPath>>()
    val probe = PathMeasure(glyphPath, false)
    do {
        val len = probe.length
        if (len > 1f) {
            val segment = AndroidPath()
            probe.getSegment(0f, len, segment, true)
            contours += len to segment
        }
    } while (probe.nextContour())

    if (contours.isEmpty()) return emptyList()

    // The main body is always first. Dots/inner contours are taught afterwards.
    contours.sortByDescending { it.first }
    val output = mutableListOf<TeachingSample>()

    contours.forEachIndexed { index, (_, contour) ->
        val measure = PathMeasure(contour, false)
        val length = measure.length
        if (length <= 1f) return@forEachIndexed

        var startDistance = 0f
        if (index == 0 && measure.isClosed) {
            val steps = 180
            var best = Float.MAX_VALUE
            for (i in 0 until steps) {
                val d = length * i / steps.toFloat()
                val p = FloatArray(2)
                measure.getPosTan(d, p, null)
                val dx = p[0] - hintX
                val dy = p[1] - hintY
                val score = dx * dx + dy * dy
                if (score < best) {
                    best = score
                    startDistance = d
                }
            }
        }

        val samples = sampleContour(measure, startDistance, 220)
        if (samples.isNotEmpty()) {
            if (output.isNotEmpty()) output += samples.first()
            output += samples
        }
    }
    return output
}

@Composable
private fun TraceTeachingBoard(symbol: String, replay: Int, arabic: Boolean) {
    val progress = remember(symbol, replay) { Animatable(0f) }

    LaunchedEffect(symbol, replay) {
        progress.snapTo(0f)
        progress.animateTo(1f, animationSpec = tween(5600, easing = LinearEasing))
    }

    Box(
        Modifier
            .fillMaxSize()
            .padding(8.dp)
            .background(Color(0xFFF2F7FF), RoundedCornerShape(24.dp))
            .border(3.dp, Color(0xFFD5E5F5), RoundedCornerShape(24.dp))
    ) {
        Canvas(Modifier.fillMaxSize().padding(8.dp)) {
            val w = size.width
            val h = size.height
            val textSize = min(w, h) * if (arabic) 0.78f else 0.72f
            val paint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                this.textSize = textSize
                typeface = android.graphics.Typeface.DEFAULT_BOLD
                textAlign = Paint.Align.CENTER
                style = Paint.Style.FILL
                color = android.graphics.Color.rgb(72, 89, 110)
                alpha = 72
            }

            // Draw the complete target glyph first. The child always sees the
            // real letter; only the hand moves, there is deliberately NO line.
            val rawPath = AndroidPath()
            paint.getTextPath(symbol, 0, symbol.length, w / 2f, h * 0.68f, rawPath)
            val bounds = android.graphics.RectF()
            rawPath.computeBounds(bounds, true)
            val maxW = w * 0.82f
            val maxH = h * 0.76f
            val scale = min(maxW / bounds.width().coerceAtLeast(1f), maxH / bounds.height().coerceAtLeast(1f))
            val matrix = Matrix().apply {
                setScale(scale, scale)
                postTranslate(
                    w / 2f - (bounds.left + bounds.right) * scale / 2f,
                    h * 0.54f - (bounds.top + bounds.bottom) * scale / 2f
                )
            }
            val glyphPath = AndroidPath(rawPath)
            glyphPath.transform(matrix)
            drawContext.canvas.nativeCanvas.drawPath(glyphPath, paint)

            val (hintX, hintY) = startHint(symbol, arabic, w, h)
            val samples = buildTeachingSamples(glyphPath, hintX, hintY)

            if (samples.isEmpty()) return@Canvas

            val indexFloat = progress.value * (samples.lastIndex.toFloat())
            val sampleIndex = indexFloat.toInt().coerceIn(0, samples.lastIndex)
            val nextIndex = (sampleIndex + 1).coerceAtMost(samples.lastIndex)
            val fraction = indexFloat - sampleIndex
            val a = samples[sampleIndex]
            val b = samples[nextIndex]
            val x = a.x + (b.x - a.x) * fraction
            val y = a.y + (b.y - a.y) * fraction
            val tx = a.tangentX + (b.tangentX - a.tangentX) * fraction
            val ty = a.tangentY + (b.tangentY - a.tangentY) * fraction

            // One clearly visible green starting point.
            val start = samples.first()
            drawCircle(Color(0xFF27AE60), 20f, Offset(start.x, start.y))
            drawCircle(Color.White, 10f, Offset(start.x, start.y))
            drawCircle(Color(0xFF27AE60), 6f, Offset(start.x, start.y))

            // The hand itself is the guide. It moves over the real letter path.
            val angle = Math.toDegrees(atan2(ty.toDouble(), tx.toDouble())).toFloat()
            val handPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                textSize = min(w, h) * 0.115f
                textAlign = Paint.Align.CENTER
                color = android.graphics.Color.WHITE
            }
            val canvas = drawContext.canvas.nativeCanvas
            canvas.save()
            canvas.rotate(angle, x, y)
            val fm = handPaint.fontMetrics
            val centeredY = y - (fm.ascent + fm.descent) / 2f
            canvas.drawText("☝️", x, centeredY, handPaint)
            canvas.restore()
        }

        Column(
            Modifier.align(Alignment.TopCenter).padding(top = 10.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                if (arabic) "ابدأ من 🟢 ثم اتبع اليد فوق الحرف حتى النهاية 👆" else "Start at 🟢 and follow the hand over the letter to the end 👆",
                fontSize = 16.sp,
                fontWeight = FontWeight.ExtraBold,
                textAlign = TextAlign.Center
            )
            Text(
                if (arabic) "الحرف كامل وواضح — بدون خط إرشاد" else "The complete letter stays visible — no guide line",
                fontSize = 13.sp,
                fontWeight = FontWeight.Bold
            )
        }
    }
}
