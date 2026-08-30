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
                    if (arabic) "شاهد الحرف كاملاً، اختر شكله، ثم اتبع إصبع اليد من نقطة البداية إلى النهاية 👆" else "See the complete letter, choose its case, then follow the hand from the start point to the end 👆",
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

/**
 * Teaching board for ALL letters.
 * The old implementation drew one straight line from start to end. That has been removed.
 * The hand now follows the real outline of the displayed glyph itself. The guide path is
 * intentionally invisible; only the complete letter, green start marker and moving hand are shown.
 */
@Composable
private fun TraceTeachingBoard(symbol: String, replay: Int, arabic: Boolean) {
    val progress = remember(symbol, replay) { Animatable(0f) }

    LaunchedEffect(symbol, replay) {
        progress.snapTo(0f)
        progress.animateTo(1f, animationSpec = tween(5200, easing = LinearEasing))
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
            val targetW = w * 0.76f
            val targetH = h * 0.70f
            val textSize = min(w, h) * if (arabic) 0.78f else 0.72f

            val paint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                this.textSize = textSize
                typeface = android.graphics.Typeface.DEFAULT_BOLD
                style = Paint.Style.FILL
            }

            // Build the actual glyph outline for every Arabic form and every
            // English upper/lower-case letter. There is no generic straight line.
            val rawPath = AndroidPath()
            paint.getTextPath(symbol, 0, symbol.length, 0f, 0f, rawPath)

            val bounds = android.graphics.RectF()
            rawPath.computeBounds(bounds, true)
            val bw = bounds.width().coerceAtLeast(1f)
            val bh = bounds.height().coerceAtLeast(1f)
            val scale = min(targetW / bw, targetH / bh)

            val matrix = Matrix().apply {
                setScale(scale, scale)
                postTranslate(
                    w / 2f - (bounds.left + bounds.right) * scale / 2f,
                    h * 0.52f - (bounds.top + bounds.bottom) * scale / 2f
                )
            }
            val glyphPath = AndroidPath(rawPath)
            glyphPath.transform(matrix)

            // Keep the complete letter visible throughout the lesson.
            paint.color = android.graphics.Color.rgb(95, 113, 132)
            paint.alpha = 70
            drawContext.canvas.nativeCanvas.drawPath(glyphPath, paint)

            // Measure every real contour of the glyph. The hand travels along
            // those contours; the path itself is never drawn as a guide line.
            val contours = mutableListOf<Float>()
            var measure = PathMeasure(glyphPath, false)
            do {
                contours += measure.length
            } while (measure.nextContour())

            val totalLength = contours.sum().coerceAtLeast(1f)
            val targetDistance = totalLength * progress.value

            var remaining = targetDistance
            var point = floatArrayOf(w / 2f, h / 2f)
            var angle = 0f
            measure = PathMeasure(glyphPath, false)
            var found = false
            while (true) {
                val len = measure.length
                if (remaining <= len || !measure.nextContour()) {
                    val pos = FloatArray(2)
                    val tan = FloatArray(2)
                    measure.getPosTan(remaining.coerceIn(0f, len), pos, tan)
                    point = pos
                    angle = Math.toDegrees(kotlin.math.atan2(tan[1].toDouble(), tan[0].toDouble())).toFloat()
                    found = true
                    break
                }
                remaining -= len
            }

            // Exact starting point of the first real contour.
            val startMeasure = PathMeasure(glyphPath, false)
            val startPos = FloatArray(2)
            startMeasure.getPosTan(0f, startPos, null)
            drawCircle(Color(0xFF27AE60), 19f, Offset(startPos[0], startPos[1]))
            drawCircle(Color.White, 8f, Offset(startPos[0], startPos[1]))
            drawCircle(Color(0xFF27AE60), 5f, Offset(startPos[0], startPos[1]))

            if (found) {
                val handPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                    textSize = min(w, h) * 0.105f
                    textAlign = Paint.Align.CENTER
                    color = android.graphics.Color.WHITE
                }
                val handX = point[0]
                val handY = point[1]
                val canvas = drawContext.canvas.nativeCanvas
                canvas.save()
                canvas.rotate(angle, handX, handY)
                val fm = handPaint.fontMetrics
                val centeredY = handY - (fm.ascent + fm.descent) / 2f
                canvas.drawText("☝️", handX, centeredY, handPaint)
                canvas.restore()
            }
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
                if (arabic) "الحرف كامل وواضح — لا يوجد خط إرشاد مستقيم" else "The complete letter is visible — no straight guide line",
                fontSize = 13.sp,
                fontWeight = FontWeight.Bold
            )
        }
    }
}
