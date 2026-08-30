@file:OptIn(androidx.compose.material3.ExperimentalMaterial3Api::class)
package com.learnlettersnumbers.app

import android.graphics.Matrix
import android.graphics.Paint
import android.graphics.Path as AndroidPath
import android.graphics.PathMeasure
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.nativeCanvas
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

private fun arabicFormSymbol(index: Int, form: ArabicForm): String {
    val c = arLetters[index]
    val joinable = setOf("ب","ت","ث","ج","ح","خ","س","ش","ص","ض","ط","ظ","ع","غ","ف","ق","ك","ل","م","ن","ه","ي")
    return when (form) {
        ArabicForm.INITIAL -> if (c in joinable) "$cـ" else c
        ArabicForm.MEDIAL -> if (c in joinable) "ـ${c}ـ" else c
        ArabicForm.FINAL -> if (c in joinable) "ـ$ c".replace("$ ", "") else c
    }
}

private fun arabicFormName(form: ArabicForm): String = when (form) {
    ArabicForm.INITIAL -> "أولي"
    ArabicForm.MEDIAL -> "وسطي"
    ArabicForm.FINAL -> "أخري"
}

@Composable
fun WritingStrokeLessonScreen(
    language: String,
    numbers: Boolean,
    onBack: () -> Unit,
    speak: (String, String) -> Unit
) {
    val arabic = language == "ar"
    var index by remember { mutableIntStateOf(0) }
    var form by remember { mutableStateOf(ArabicForm.INITIAL) }
    var englishCase by remember { mutableStateOf(WritingEnglishCase.UPPER) }
    var replay by remember { mutableIntStateOf(0) }

    val total = when {
        numbers -> 10
        arabic -> arLetters.size
        else -> enLetters.size
    }
    val currentIndex = index.coerceIn(0, total - 1)
    val symbol = when {
        numbers -> (currentIndex + 1).toString()
        arabic -> arabicFormSymbol(currentIndex, form)
        englishCase == WritingEnglishCase.UPPER -> enLetters[currentIndex].toString()
        else -> enLetters[currentIndex].lowercase()
    }
    val title = when {
        numbers -> "${if (arabic) "الرقم" else "Number"} $symbol"
        arabic -> "${arNames[currentIndex]} — ${arabicFormName(form)}"
        englishCase == WritingEnglishCase.UPPER -> "${enLetters[currentIndex]} — حروف كبيرة"
        else -> "${enLetters[currentIndex].lowercase()} — حروف صغيرة"
    }

    LaunchedEffect(currentIndex, form, englishCase, replay) {
        val message = when {
            numbers -> if (arabic) "تعلم كتابة الرقم $symbol" else "Learn to write number $symbol"
            arabic -> "تعلم كتابة ${arNames[currentIndex]}، الشكل ${arabicFormName(form)}"
            englishCase == WritingEnglishCase.UPPER -> "Learn to write capital letter ${enLetters[currentIndex]}"
            else -> "Learn to write lowercase letter ${enLetters[currentIndex].lowercase()}"
        }
        speak(message, language)
    }

    CompositionLocalProvider(LocalLayoutDirection provides if (arabic) LayoutDirection.Rtl else LayoutDirection.Ltr) {
        Scaffold(
            topBar = {
                TopAppBar(
                    title = { Text(if (arabic) "تعلم الكتابة" else "Learn to Write", fontWeight = FontWeight.ExtraBold) },
                    navigationIcon = {
                        TextButton(onClick = onBack) {
                            Text(if (arabic) "رجوع" else "Back", fontWeight = FontWeight.Bold)
                        }
                    }
                )
            }
        ) { padding ->
            Column(
                Modifier.fillMaxSize().padding(padding).padding(10.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    if (arabic) "شاهد الحرف كاملاً، اختر شكله، ثم اتبع اليد من نقطة البداية إلى النهاية 👆"
                    else "See the complete letter, choose its case, then follow the hand from the start to the end 👆",
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
                        FormButton("UPPERCASE", "حروف كبيرة", englishCase == WritingEnglishCase.UPPER, Color(0xFF4C8BF5), Modifier.weight(1f)) { englishCase = WritingEnglishCase.UPPER; replay++ }
                        FormButton("lowercase", "حروف صغيرة", englishCase == WritingEnglishCase.LOWER, Color(0xFFFF8A4C), Modifier.weight(1f)) { englishCase = WritingEnglishCase.LOWER; replay++ }
                    }
                }

                Spacer(Modifier.height(7.dp))
                Card(
                    Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(18.dp),
                    elevation = CardDefaults.cardElevation(5.dp)
                ) {
                    Row(
                        Modifier.fillMaxWidth().padding(horizontal = 12.dp, vertical = 7.dp),
                        horizontalArrangement = Arrangement.Center,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("${currentIndex + 1} / $total", fontWeight = FontWeight.ExtraBold)
                        Spacer(Modifier.width(12.dp))
                        Text(symbol, fontSize = 42.sp, fontWeight = FontWeight.Black, color = Color(0xFF315CFF))
                        Spacer(Modifier.width(10.dp))
                        Text(title, fontSize = 15.sp, fontWeight = FontWeight.Bold)
                    }
                }

                Spacer(Modifier.height(7.dp))
                Card(
                    Modifier.fillMaxWidth().weight(1f),
                    shape = RoundedCornerShape(28.dp),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFFFFFBF0)),
                    elevation = CardDefaults.cardElevation(9.dp)
                ) {
                    TraceTeachingBoard(symbol = symbol, replay = replay, arabic = arabic)
                }

                Spacer(Modifier.height(7.dp))
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    LessonButton(if (arabic) "السابق\nPrevious" else "Previous", Color(0xFF5C6BC0), currentIndex > 0, Modifier.weight(1f)) {
                        if (currentIndex > 0) { index = currentIndex - 1; replay++ }
                    }
                    LessonButton("🔄 ${if (arabic) "إعادة" else "Replay"}", Color(0xFF039BE5), true, Modifier.weight(1f)) { replay++ }
                    LessonButton(if (arabic) "التالي\nNext" else "Next", Color(0xFF2EAD69), currentIndex < total - 1, Modifier.weight(1f)) {
                        if (currentIndex < total - 1) { index = currentIndex + 1; replay++ }
                    }
                }
            }
        }
    }
}

@Composable
private fun FormButton(title: String, subtitle: String, selected: Boolean, color: Color, modifier: Modifier, onClick: () -> Unit) {
    Card(
        modifier = modifier.height(64.dp).clickable(onClick = onClick),
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = if (selected) color else Color.White),
        elevation = CardDefaults.cardElevation(if (selected) 9.dp else 3.dp)
    ) {
        Column(Modifier.fillMaxSize(), horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.Center) {
            Text(title, fontSize = 17.sp, fontWeight = FontWeight.Black, color = if (selected) Color.White else color, textAlign = TextAlign.Center)
            Text(subtitle, fontSize = 12.sp, color = if (selected) Color.White else Color.DarkGray, textAlign = TextAlign.Center)
        }
    }
}

@Composable
private fun LessonButton(text: String, color: Color, enabled: Boolean, modifier: Modifier, onClick: () -> Unit) {
    Button(
        onClick = onClick,
        enabled = enabled,
        modifier = modifier.height(58.dp),
        shape = RoundedCornerShape(18.dp),
        colors = ButtonDefaults.buttonColors(containerColor = color, disabledContainerColor = Color(0xFFD9E0E5))
    ) {
        Text(text, fontWeight = FontWeight.ExtraBold, fontSize = 17.sp, textAlign = TextAlign.Center)
    }
}

private data class TeachingSample(
    val x: Float,
    val y: Float,
    val tangentX: Float,
    val tangentY: Float
)

private fun startHint(symbol: String, arabic: Boolean, width: Float, height: Float): Offset {
    val hint = if (!arabic) {
        when (symbol.lowercase()) {
            "a" -> 0.32f to 0.18f
            "b", "d", "p", "q", "r" -> 0.62f to 0.18f
            "c", "e", "o", "s" -> 0.72f to 0.35f
            "f", "t" -> 0.58f to 0.16f
            "g" -> 0.72f to 0.42f
            "h", "k", "l", "m", "n" -> 0.28f to 0.18f
            "i", "j" -> 0.52f to 0.16f
            else -> 0.28f to 0.20f
        }
    } else {
        val base = symbol.firstOrNull { it in arLetters }?.toString().orEmpty()
        when (base) {
            "ا" -> 0.55f to 0.18f
            "ب", "ت", "ث", "ي" -> 0.76f to 0.56f
            "ج", "ح", "خ" -> 0.76f to 0.30f
            "د", "ذ", "ر", "ز" -> 0.76f to 0.25f
            "س", "ش" -> 0.78f to 0.40f
            "ص", "ض" -> 0.76f to 0.30f
            "ط", "ظ" -> 0.65f to 0.18f
            "ع", "غ" -> 0.75f to 0.34f
            "ف", "ق" -> 0.75f to 0.24f
            "ك" -> 0.72f to 0.20f
            "ل" -> 0.60f to 0.16f
            "م", "ن", "ه" -> 0.75f to 0.35f
            "و" -> 0.70f to 0.28f
            else -> 0.72f to 0.30f
        }
    }
    return Offset(hint.first * width, hint.second * height)
}

private fun sampleContour(measure: PathMeasure, startDistance: Float, steps: Int): List<TeachingSample> {
    val length = measure.length
    if (length <= 0f) return emptyList()
    val result = ArrayList<TeachingSample>(steps + 1)
    for (i in 0..steps) {
        val d = (startDistance + length * i / steps.toFloat()).coerceAtMost(length)
        val position = FloatArray(2)
        val tangent = FloatArray(2)
        measure.getPosTan(d, position, tangent)
        result.add(TeachingSample(position[0], position[1], tangent[0], tangent[1]))
    }
    return result
}

private fun buildTeachingSamples(path: AndroidPath, hint: Offset): List<TeachingSample> {
    val contours = mutableListOf<AndroidPath>()
    val measure = PathMeasure(path, false)
    do {
        if (measure.length > 1f) {
            val contour = AndroidPath()
            measure.getSegment(0f, measure.length, contour, true)
            contours.add(contour)
        }
    } while (measure.nextContour())

    if (contours.isEmpty()) return emptyList()

    val ordered = contours.sortedByDescending { contour -> PathMeasure(contour, false).length }
    val output = mutableListOf<TeachingSample>()

    ordered.forEachIndexed { contourIndex, contour ->
        val contourMeasure = PathMeasure(contour, false)
        val length = contourMeasure.length
        if (length <= 1f) return@forEachIndexed

        var start = 0f
        if (contourIndex == 0 && contourMeasure.isClosed) {
            var bestDistance = 0f
            var bestScore = Float.MAX_VALUE
            for (i in 0 until 180) {
                val d = length * i / 180f
                val p = FloatArray(2)
                contourMeasure.getPosTan(d, p, null)
                val dx = p[0] - hint.x
                val dy = p[1] - hint.y
                val score = dx * dx + dy * dy
                if (score < bestScore) {
                    bestScore = score
                    bestDistance = d
                }
            }
            start = bestDistance
        }

        val samples = if (contourMeasure.isClosed) {
            val result = ArrayList<TeachingSample>(221)
            for (i in 0..220) {
                var d = (start + length * i / 220f) % length
                if (d < 0f) d += length
                val p = FloatArray(2)
                val t = FloatArray(2)
                contourMeasure.getPosTan(d, p, t)
                result.add(TeachingSample(p[0], p[1], t[0], t[1]))
            }
            result
        } else {
            sampleContour(contourMeasure, start, 220)
        }

        if (samples.isNotEmpty()) {
            if (output.isNotEmpty()) output.add(samples.first())
            output.addAll(samples)
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
        Modifier.fillMaxSize().padding(8.dp).background(Color(0xFFF2F7FF), RoundedCornerShape(24.dp)).border(3.dp, Color(0xFFD5E5F5), RoundedCornerShape(24.dp))
    ) {
        Canvas(Modifier.fillMaxSize().padding(8.dp)) {
            val width = size.width
            val height = size.height
            val textSize = min(width, height) * if (arabic) 0.78f else 0.72f
            val paint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                this.textSize = textSize
                typeface = android.graphics.Typeface.DEFAULT_BOLD
                textAlign = Paint.Align.CENTER
                style = Paint.Style.FILL
                color = android.graphics.Color.rgb(72, 89, 110)
                alpha = 72
            }

            val rawPath = AndroidPath()
            paint.getTextPath(symbol, 0, symbol.length, width / 2f, height * 0.68f, rawPath)
            val bounds = android.graphics.RectF()
            rawPath.computeBounds(bounds, true)

            val maxWidth = width * 0.82f
            val maxHeight = height * 0.76f
            val scaleX = maxWidth / bounds.width().coerceAtLeast(1f)
            val scaleY = maxHeight / bounds.height().coerceAtLeast(1f)
            val scale = min(scaleX, scaleY)

            val matrix = Matrix().apply {
                setScale(scale, scale)
                postTranslate(
                    width / 2f - (bounds.left + bounds.right) * scale / 2f,
                    height * 0.54f - (bounds.top + bounds.bottom) * scale / 2f
                )
            }
            val glyphPath = AndroidPath(rawPath)
            glyphPath.transform(matrix)

            drawContext.canvas.nativeCanvas.drawPath(glyphPath, paint)

            val hint = startHint(symbol, arabic, width, height)
            val samples: List<TeachingSample> = buildTeachingSamples(glyphPath, hint)
            if (samples.isEmpty()) return@Canvas

            val maxIndex = samples.lastIndex.toFloat()
            val position = progress.value * maxIndex
            val indexA = position.toInt().coerceIn(0, samples.lastIndex)
            val indexB = (indexA + 1).coerceAtMost(samples.lastIndex)
            val fraction = position - indexA.toFloat()
            val a = samples[indexA]
            val b = samples[indexB]

            val x = a.x + (b.x - a.x) * fraction
            val y = a.y + (b.y - a.y) * fraction
            val tx = a.tangentX + (b.tangentX - a.tangentX) * fraction
            val ty = a.tangentY + (b.tangentY - a.tangentY) * fraction

            val start = samples.first()
            drawCircle(Color(0xFF27AE60), 20f, Offset(start.x, start.y))
            drawCircle(Color.White, 10f, Offset(start.x, start.y))
            drawCircle(Color(0xFF27AE60), 6f, Offset(start.x, start.y))

            val angle = Math.toDegrees(atan2(ty.toDouble(), tx.toDouble())).toFloat()
            val handPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                textSize = min(width, height) * 0.115f
                textAlign = Paint.Align.CENTER
                color = android.graphics.Color.BLACK
            }
            val nativeCanvas = drawContext.canvas.nativeCanvas
            nativeCanvas.save()
            nativeCanvas.rotate(angle, x, y)
            val metrics = handPaint.fontMetrics
            val centeredY = y - (metrics.ascent + metrics.descent) / 2f
            nativeCanvas.drawText("☝", x, centeredY, handPaint)
            nativeCanvas.restore()
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
