@file:OptIn(androidx.compose.material3.ExperimentalMaterial3Api::class)
package com.learnlettersnumbers.app

import android.animation.ValueAnimator
import android.content.Context
import android.graphics.Matrix
import android.graphics.Paint
import android.graphics.Path
import android.graphics.PathMeasure
import android.graphics.RectF
import android.view.View
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
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
import androidx.compose.ui.viewinterop.AndroidView
import androidx.compose.ui.platform.LocalLayoutDirection
import kotlin.math.hypot
import kotlin.math.min

private enum class ArabicForm { INITIAL, MEDIAL, FINAL }
private enum class WritingEnglishCase { UPPER, LOWER }

private val arLetters = listOf("ا","ب","ت","ث","ج","ح","خ","د","ذ","ر","ز","س","ش","ص","ض","ط","ظ","ع","غ","ف","ق","ك","ل","م","ن","ه","و","ي")
private val arNames = listOf("الألف","الباء","التاء","الثاء","الجيم","الحاء","الخاء","الدال","الذال","الراء","الزاي","السين","الشين","الصاد","الضاد","الطاء","الظاء","العين","الغين","الفاء","القاف","الكاف","اللام","الميم","النون","الهاء","الواو","الياء")
private val enLetters = ('A'..'Z').toList()

/* Arabic presentation forms are used instead of tatweel placeholders so the lesson
   always shows the actual requested initial/medial/final shape. */
private val arabicForms = mapOf(
    "ا" to arrayOf("ا","ا","ا"), "ب" to arrayOf("ﺑ","ﺒ","ﺐ"), "ت" to arrayOf("ﺗ","ﺘ","ﺖ"),
    "ث" to arrayOf("ﺛ","ﺜ","ﺚ"), "ج" to arrayOf("ﺟ","ﺠ","ﺞ"), "ح" to arrayOf("ﺣ","ﺤ","ﺢ"),
    "خ" to arrayOf("ﺧ","ﺨ","ﺦ"), "د" to arrayOf("د","د","ﺪ"), "ذ" to arrayOf("ذ","ذ","ﺬ"),
    "ر" to arrayOf("ر","ر","ﺮ"), "ز" to arrayOf("ز","ز","ﺰ"), "س" to arrayOf("ﺳ","ﺴ","ﺲ"),
    "ش" to arrayOf("ﺷ","ﺸ","ﺶ"), "ص" to arrayOf("ﺻ","ﺼ","ﺺ"), "ض" to arrayOf("ﺿ","ﻀ","ﺾ"),
    "ط" to arrayOf("ﻃ","ﻄ","ﻂ"), "ظ" to arrayOf("ﻇ","ﻈ","ﻆ"), "ع" to arrayOf("ﻋ","ﻌ","ﻊ"),
    "غ" to arrayOf("ﻏ","ﻐ","ﻎ"), "ف" to arrayOf("ﻓ","ﻔ","ﻒ"), "ق" to arrayOf("ﻗ","ﻘ","ﻖ"),
    "ك" to arrayOf("ﻛ","ﻜ","ﻚ"), "ل" to arrayOf("ﻟ","ﻠ","ﻞ"), "م" to arrayOf("ﻣ","ﻤ","ﻢ"),
    "ن" to arrayOf("ﻧ","ﻨ","ﻦ"), "ه" to arrayOf("ﻫ","ﻬ","ﻪ"), "و" to arrayOf("و","و","ﻮ"),
    "ي" to arrayOf("ﻳ","ﻴ","ﻲ")
)

private fun arabicFormSymbol(index: Int, form: ArabicForm): String {
    val base = arLetters[index]
    val forms = arabicForms[base] ?: arrayOf(base, base, base)
    return forms[form.ordinal]
}

private fun arabicFormName(form: ArabicForm): String = when (form) {
    ArabicForm.INITIAL -> "أولي"
    ArabicForm.MEDIAL -> "وسطي"
    ArabicForm.FINAL -> "أخري"
}

@Composable
fun WritingStrokeLessonScreen(language: String, numbers: Boolean, onBack: () -> Unit, speak: (String, String) -> Unit) {
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
            arabic -> "تعلم كتابة ${arNames[currentIndex}، الشكل ${arabicFormName(form)}"
            englishCase == WritingEnglishCase.UPPER -> "Learn to write capital letter ${enLetters[currentIndex]}"
            else -> "Learn to write lowercase letter ${enLetters[currentIndex].lowercase()}"
        }
        speak(message, language)
    }

    CompositionLocalProvider(LocalLayoutDirection provides if (arabic) LayoutDirection.Rtl else LayoutDirection.Ltr) {
        Scaffold(topBar = {
            TopAppBar(
                title = { Text(if (arabic) "تعلم الكتابة" else "Learn to Write", fontWeight = FontWeight.ExtraBold) },
                navigationIcon = { TextButton(onClick = onBack) { Text(if (arabic) "رجوع" else "Back", fontWeight = FontWeight.Bold, fontSize = 17.sp) } }
            )
        }) { padding ->
            Column(
                Modifier.fillMaxSize().padding(padding).padding(horizontal = 10.dp, vertical = 6.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    if (arabic) "شاهد الحرف كاملاً، اختر شكله، ثم اتبع اليد من نقطة البداية إلى النهاية 👆" else "See the complete letter, choose its case, then follow the hand from the start to the end 👆",
                    fontSize = 17.sp, fontWeight = FontWeight.Bold, textAlign = TextAlign.Center,
                    modifier = Modifier.padding(bottom = 7.dp)
                )

                if (arabic && !numbers) {
                    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        FormButton("أولي", "بداية الحرف", form == ArabicForm.INITIAL, Color(0xFF4C8BF5), Modifier.weight(1f)) { form = ArabicForm.INITIAL; replay++ }
                        FormButton("وسطي", "وسط الحرف", form == ArabicForm.MEDIAL, Color(0xFFFFA726), Modifier.weight(1f)) { form = ArabicForm.MEDIAL; replay++ }
                        FormButton("أخري", "نهاية الحرف", form == ArabicForm.FINAL, Color(0xFF43A047), Modifier.weight(1f)) { form = ArabicForm.FINAL; replay++ }
                    }
                } else if (!arabic && !numbers) {
                    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        FormButton("UPPERCASE", "حروف كبيرة", englishCase == WritingEnglishCase.UPPER, Color(0xFF4C8BF5), Modifier.weight(1f)) { englishCase = WritingEnglishCase.UPPER; replay++ }
                        FormButton("lowercase", "حروف صغيرة", englishCase == WritingEnglishCase.LOWER, Color(0xFFFF8A4C), Modifier.weight(1f)) { englishCase = WritingEnglishCase.LOWER; replay++ }
                    }
                }

                Spacer(Modifier.height(6.dp))
                Card(Modifier.fillMaxWidth(), shape = RoundedCornerShape(18.dp), elevation = CardDefaults.cardElevation(5.dp)) {
                    Row(Modifier.fillMaxWidth().padding(horizontal = 10.dp, vertical = 5.dp), horizontalArrangement = Arrangement.Center, verticalAlignment = Alignment.CenterVertically) {
                        Text("${currentIndex + 1} / $total", fontWeight = FontWeight.ExtraBold, fontSize = 14.sp)
                        Spacer(Modifier.width(10.dp))
                        Text(symbol, fontSize = 40.sp, fontWeight = FontWeight.Black, color = Color(0xFF315CFF))
                        Spacer(Modifier.width(9.dp))
                        Text(title, fontSize = 15.sp, fontWeight = FontWeight.Bold)
                    }
                }

                Spacer(Modifier.height(6.dp))
                Card(
                    Modifier.fillMaxWidth().weight(1f),
                    shape = RoundedCornerShape(28.dp),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFFFFFBF0)),
                    elevation = CardDefaults.cardElevation(9.dp)
                ) {
                    AndroidView(
                        modifier = Modifier.fillMaxSize().padding(7.dp),
                        factory = { context -> TraceTeachingView(context) },
                        update = { view -> view.setLesson(symbol, arabic, replay) }
                    )
                }

                Spacer(Modifier.height(6.dp))
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
        modifier = modifier.height(66.dp).clickable(onClick = onClick),
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = if (selected) color else Color.White),
        elevation = CardDefaults.cardElevation(if (selected) 8.dp else 3.dp)
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
        modifier = modifier.height(60.dp),
        shape = RoundedCornerShape(18.dp),
        colors = ButtonDefaults.buttonColors(containerColor = color, disabledContainerColor = Color(0xFFD9E0E5))
    ) {
        Text(text, fontWeight = FontWeight.ExtraBold, fontSize = 17.sp, textAlign = TextAlign.Center)
    }
}

private data class TeachingSample(val x: Float, val y: Float, val tx: Float, val ty: Float)

private class TraceTeachingView(context: Context) : View(context) {
    private val glyphPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = android.graphics.Color.rgb(125, 139, 158)
        alpha = 82
        style = Paint.Style.FILL
        typeface = android.graphics.Typeface.DEFAULT_BOLD
        textAlign = Paint.Align.CENTER
    }
    private val startPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply { color = android.graphics.Color.rgb(39, 174, 96); style = Paint.Style.FILL }
    private val whitePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply { color = android.graphics.Color.WHITE; style = Paint.Style.FILL }
    private val handPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = android.graphics.Color.rgb(255, 174, 0)
        style = Paint.Style.FILL
        textAlign = Paint.Align.CENTER
    }
    private var symbol = "A"
    private var arabic = false
    private var progress = 0f
    private var lastKey = ""
    private var animator: ValueAnimator? = null
    private var samples: List<TeachingSample> = emptyList()
    private val glyphPath = Path()

    fun setLesson(newSymbol: String, newArabic: Boolean, replay: Int) {
        val key = "$newSymbol|$newArabic|$replay"
        if (key == lastKey) return
        lastKey = key
        symbol = newSymbol
        arabic = newArabic
        progress = 0f
        rebuildSamples()
        animator?.cancel()
        animator = ValueAnimator.ofFloat(0f, 1f).apply {
            duration = 5600L
            addUpdateListener {
                progress = it.animatedValue as Float
                invalidate()
            }
            start()
        }
        invalidate()
    }

    override fun onDetachedFromWindow() {
        animator?.cancel()
        animator = null
        super.onDetachedFromWindow()
    }

    private fun startHint(letter: String): Pair<Float, Float> {
        if (!arabic) return when (letter.lowercase()) {
            "a" -> .32f to .20f; "b", "d", "p", "q", "r" -> .62f to .18f
            "c", "e", "o", "s" -> .72f to .35f; "f", "t" -> .58f to .16f
            "g" -> .72f to .42f; "h", "k", "l", "m", "n" -> .28f to .18f
            "i", "j" -> .52f to .16f; else -> .28f to .20f
        }
        val base = arLetters.firstOrNull { letter.contains(it) } ?: letter
        return when (base) {
            "ا" -> .52f to .16f; "ب", "ت", "ث", "ي" -> .78f to .55f
            "ج", "ح", "خ" -> .78f to .30f; "د", "ذ", "ر", "ز" -> .78f to .25f
            "س", "ش" -> .80f to .40f; "ص", "ض" -> .78f to .30f
            "ط", "ظ" -> .65f to .18f; "ع", "غ" -> .76f to .34f
            "ف", "ق" -> .76f to .24f; "ك" -> .72f to .20f
            "ل" -> .60f to .16f; "م", "ن", "ه" -> .76f to .35f
            "و" -> .70f to .28f; else -> .72f to .30f
        }
    }

    private fun rebuildSamples() {
        if (width <= 0 || height <= 0) return
        glyphPaint.textSize = min(width.toFloat(), height.toFloat()) * if (arabic) .82f else .76f
        glyphPath.reset()
        glyphPaint.getTextPath(symbol, 0, symbol.length, width / 2f, height * .68f, glyphPath)
        val bounds = RectF()
        glyphPath.computeBounds(bounds, true)
        val maxW = width * .80f
        val maxH = height * .76f
        val scale = min(maxW / bounds.width().coerceAtLeast(1f), maxH / bounds.height().coerceAtLeast(1f))
        val matrix = Matrix().apply {
            setScale(scale, scale)
            postTranslate(width / 2f - (bounds.left + bounds.right) * scale / 2f, height * .54f - (bounds.top + bounds.bottom) * scale / 2f)
        }
        glyphPath.transform(matrix)
        samples = buildSamples(glyphPath, startHint(symbol))
    }

    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        rebuildSamples()
    }

    private fun buildSamples(path: Path, hint: Pair<Float, Float>): List<TeachingSample> {
        val contours = mutableListOf<Path>()
        val measure = PathMeasure(path, false)
        do {
            if (measure.length > 1f) {
                val contour = Path()
                measure.getSegment(0f, measure.length, contour, true)
                contours.add(contour)
            }
        } while (measure.nextContour())
        if (contours.isEmpty()) return emptyList()

        val ordered = contours.sortedByDescending { PathMeasure(it, false).length }
        val out = mutableListOf<TeachingSample>()
        ordered.forEachIndexed { contourIndex, contour ->
            val m = PathMeasure(contour, false)
            val length = m.length
            if (length <= 1f) return@forEachIndexed
            var start = 0f
            if (contourIndex == 0 && m.isClosed) {
                val targetX = hint.first * width
                val targetY = hint.second * height
                var best = Float.MAX_VALUE
                for (i in 0 until 240) {
                    val d = length * i / 240f
                    val p = FloatArray(2)
                    m.getPosTan(d, p, null)
                    val score = hypot(p[0] - targetX, p[1] - targetY)
                    if (score < best) { best = score; start = d }
                }
            }
            val count = 260
            for (i in 0..count) {
                var d = start + length * i / count.toFloat()
                if (m.isClosed) d %= length
                if (d < 0f) d += length
                val p = FloatArray(2)
                val t = FloatArray(2)
                m.getPosTan(d.coerceAtMost(length), p, t)
                out.add(TeachingSample(p[0], p[1], t[0], t[1]))
            }
        }
        return out
    }

    override fun onDraw(canvas: android.graphics.Canvas) {
        super.onDraw(canvas)
        canvas.drawColor(android.graphics.Color.rgb(242, 247, 255))
        if (glyphPath.isEmpty) return

        canvas.drawPath(glyphPath, glyphPaint)
        if (samples.isEmpty()) return

        val first = samples.first()
        canvas.drawCircle(first.x, first.y, 16f, startPaint)
        canvas.drawCircle(first.x, first.y, 7f, whitePaint)
        canvas.drawCircle(first.x, first.y, 4f, startPaint)

        val pos = progress * (samples.size - 1).toFloat()
        val ia = pos.toInt().coerceIn(0, samples.lastIndex)
        val ib = (ia + 1).coerceAtMost(samples.lastIndex)
        val f = pos - ia
        val a = samples[ia]
        val b = samples[ib]
        val x = a.x + (b.x - a.x) * f
        val y = a.y + (b.y - a.y) * f
        val angle = Math.toDegrees(kotlin.math.atan2((a.ty + (b.ty - a.ty) * f).toDouble(), (a.tx + (b.tx - a.tx) * f).toDouble())).toFloat()

        handPaint.textSize = min(width.toFloat(), height.toFloat()) * .12f
        canvas.save()
        canvas.rotate(angle, x, y)
        canvas.drawText("☝", x, y - (handPaint.ascent() + handPaint.descent()) / 2f, handPaint)
        canvas.restore()
    }
}
