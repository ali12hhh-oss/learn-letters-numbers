package com.learnlettersnumbers.app

import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlin.math.min

private data class Stroke(
    val points: List<Offset>
)

private data class Lesson(
    val symbol: String,
    val label: String,
    val language: String,
    val strokes: List<Stroke>
)

private fun stroke(vararg p: Offset) = Stroke(p.toList())

private val arLessons = listOf(
    Lesson("ا", "الألف", "ar", listOf(stroke(Offset(.50f,.15f), Offset(.50f,.85f)))),
    Lesson("ب", "الباء", "ar", listOf(
        stroke(Offset(.72f,.45f), Offset(.60f,.58f), Offset(.42f,.62f), Offset(.28f,.48f)),
        stroke(Offset(.50f,.73f))
    )),
    Lesson("ت", "التاء", "ar", listOf(
        stroke(Offset(.72f,.45f), Offset(.60f,.58f), Offset(.42f,.62f), Offset(.28f,.48f)),
        stroke(Offset(.45f,.28f)), stroke(Offset(.55f,.28f))
    )),
    Lesson("ث", "الثاء", "ar", listOf(
        stroke(Offset(.72f,.45f), Offset(.60f,.58f), Offset(.42f,.62f), Offset(.28f,.48f)),
        stroke(Offset(.42f,.28f)), stroke(Offset(.50f,.24f)), stroke(Offset(.58f,.28f))
    )),
    Lesson("ج", "الجيم", "ar", listOf(
        stroke(Offset(.70f,.38f), Offset(.52f,.55f), Offset(.32f,.58f), Offset(.40f,.72f), Offset(.58f,.70f)),
        stroke(Offset(.50f,.82f))
    )),
    Lesson("ح", "الحاء", "ar", listOf(
        stroke(Offset(.70f,.38f), Offset(.52f,.55f), Offset(.32f,.58f), Offset(.40f,.72f), Offset(.58f,.70f))
    )),
    Lesson("خ", "الخاء", "ar", listOf(
        stroke(Offset(.70f,.38f), Offset(.52f,.55f), Offset(.32f,.58f), Offset(.40f,.72f), Offset(.58f,.70f)),
        stroke(Offset(.50f,.25f))
    ))
)

private val enLessons = listOf(
    Lesson("A a", "A / a", "en", listOf(
        stroke(Offset(.50f,.12f), Offset(.30f,.86f)),
        stroke(Offset(.50f,.12f), Offset(.70f,.86f)),
        stroke(Offset(.38f,.58f), Offset(.62f,.58f))
    )),
    Lesson("B b", "B / b", "en", listOf(
        stroke(Offset(.35f,.12f), Offset(.35f,.88f)),
        stroke(Offset(.35f,.15f), Offset(.62f,.25f), Offset(.38f,.50f)),
        stroke(Offset(.38f,.50f), Offset(.64f,.62f), Offset(.35f,.86f))
    )),
    Lesson("C c", "C / c", "en", listOf(
        stroke(Offset(.68f,.24f), Offset(.55f,.14f), Offset(.35f,.20f), Offset(.25f,.50f), Offset(.35f,.80f), Offset(.55f,.86f), Offset(.68f,.76f))
    )),
    Lesson("D d", "D / d", "en", listOf(
        stroke(Offset(.35f,.12f), Offset(.35f,.88f)),
        stroke(Offset(.35f,.14f), Offset(.62f,.22f), Offset(.70f,.50f), Offset(.62f,.78f), Offset(.35f,.86f))
    )),
    Lesson("E e", "E / e", "en", listOf(
        stroke(Offset(.68f,.14f), Offset(.34f,.14f), Offset(.34f,.86f), Offset(.68f,.86f)),
        stroke(Offset(.34f,.50f), Offset(.60f,.50f))
    )),
    Lesson("F f", "F / f", "en", listOf(
        stroke(Offset(.68f,.14f), Offset(.34f,.14f), Offset(.34f,.86f)),
        stroke(Offset(.34f,.50f), Offset(.60f,.50f))
    )),
    Lesson("G g", "G / g", "en", listOf(
        stroke(Offset(.68f,.24f), Offset(.55f,.14f), Offset(.35f,.20f), Offset(.25f,.50f), Offset(.35f,.80f), Offset(.58f,.84f), Offset(.70f,.70f)),
        stroke(Offset(.70f,.70f), Offset(.50f,.70f))
    ))
)

private val digits = (0..9).map { n ->
    val shapes = when(n) {
        0 -> listOf(stroke(Offset(.50f,.15f),Offset(.32f,.28f),Offset(.30f,.65f),Offset(.50f,.85f),Offset(.68f,.65f),Offset(.68f,.28f),Offset(.50f,.15f)))
        1 -> listOf(stroke(Offset(.42f,.30f),Offset(.50f,.18f),Offset(.50f,.84f)))
        2 -> listOf(stroke(Offset(.32f,.30f),Offset(.45f,.17f),Offset(.67f,.28f),Offset(.32f,.84f),Offset(.68f,.84f)))
        3 -> listOf(stroke(Offset(.32f,.22f),Offset(.62f,.18f),Offset(.68f,.45f),Offset(.48f,.50f),Offset(.68f,.55f),Offset(.62f,.84f),Offset(.32f,.80f)))
        4 -> listOf(stroke(Offset(.60f,.15f),Offset(.30f,.60f),Offset(.72f,.60f)),stroke(Offset(.60f,.15f),Offset(.60f,.86f)))
        5 -> listOf(stroke(Offset(.68f,.16f),Offset(.34f,.16f),Offset(.32f,.50f),Offset(.62f,.50f),Offset(.70f,.62f),Offset(.62f,.84f),Offset(.32f,.82f)))
        6 -> listOf(stroke(Offset(.65f,.18f),Offset(.40f,.16f),Offset(.30f,.50f),Offset(.38f,.82f),Offset(.62f,.84f),Offset(.70f,.65f),Offset(.62f,.50f),Offset(.32f,.52f)))
        7 -> listOf(stroke(Offset(.30f,.16f),Offset(.70f,.16f),Offset(.42f,.86f)))
        8 -> listOf(stroke(Offset(.50f,.50f),Offset(.32f,.34f),Offset(.38f,.16f),Offset(.62f,.16f),Offset(.68f,.34f),Offset(.50f,.50f),Offset(.32f,.66f),Offset(.38f,.84f),Offset(.62f,.84f),Offset(.68f,.66f),Offset(.50f,.50f)))
        else -> listOf(stroke(Offset(.68f,.50f),Offset(.58f,.18f),Offset(.35f,.18f),Offset(.30f,.50f),Offset(.38f,.65f),Offset(.62f,.65f),Offset(.68f,.82f),Offset(.30f,.82f)))
    }
    Lesson(n.toString(), n.toString(), "en", shapes)
}

@Composable
fun WritingStrokeLessonScreen(
    language: String,
    numbers: Boolean,
    onBack: () -> Unit,
    speak: (String, String) -> Unit
) {
    val lessons = if (numbers) digits else if (language == "ar") arLessons else enLessons
    var index by remember { mutableStateOf(0) }
    var replay by remember { mutableStateOf(0) }
    val lesson = lessons[index]

    LaunchedEffect(index, replay) {
        val msg = if (numbers)
            if (language == "ar") "تعلم كتابة الرقم ${lesson.symbol}" else "Learn to write number ${lesson.symbol}"
        else
            if (language == "ar") "تعلم كتابة حرف ${lesson.label}" else "Learn to write ${lesson.label}"
        speak(msg, if (numbers) "ar" else language)
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(if (language == "ar") "تعلم الكتابة خطوة بخطوة" else "Learn to Write") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "رجوع")
                    }
                }
            )
        }
    ) { pad ->
        Column(
            Modifier.fillMaxSize().padding(pad).padding(14.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                if (language == "ar") "اتبع نقطة البداية والسهم بالترتيب ✏️"
                else "Follow the starting point and arrow ✏️",
                fontSize = 20.sp, fontWeight = FontWeight.Bold
            )
            Spacer(Modifier.height(8.dp))
            Text("${index + 1} / ${lessons.size}", fontWeight = FontWeight.Bold)

            Card(
                Modifier.fillMaxWidth().weight(1f),
                shape = RoundedCornerShape(28.dp),
                colors = CardDefaults.cardColors(containerColor = Color(0xFFFFFBF0)),
                elevation = CardDefaults.cardElevation(8.dp)
            ) {
                Column(
                    Modifier.fillMaxSize().padding(12.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(lesson.symbol, fontSize = 82.sp, fontWeight = FontWeight.Bold, color = Color(0xFF315CFF))
                    StrokeCanvas(lesson, replay)
                    Spacer(Modifier.height(8.dp))
                    Text(
                        if (language == "ar") "النقطة الخضراء هي البداية. اتبع السهم حتى النهاية."
                        else "The green dot is the start. Follow each arrow to the end.",
                        fontSize = 16.sp, fontWeight = FontWeight.SemiBold
                    )
                }
            }

            Spacer(Modifier.height(10.dp))
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedButton(Modifier.weight(1f), enabled = index > 0, onClick = { index-- }) { Text("السابق") }
                Button(Modifier.weight(1f), onClick = { replay++ }) { Text("🔊 أعد") }
                Button(Modifier.weight(1f), enabled = index < lessons.lastIndex, onClick = { index++ }) { Text("التالي") }
            }
        }
    }
}

@Composable
private fun StrokeCanvas(lesson: Lesson, replay: Int) {
    var progress by remember(lesson.symbol, replay) { mutableStateOf(0f) }
    LaunchedEffect(lesson.symbol, replay) {
        animate(
            initialValue = 0f, targetValue = 1f,
            animationSpec = tween(2200, easing = LinearEasing)
        ) { v, _ -> progress = v }
    }

    Canvas(
        Modifier.fillMaxWidth().weight(1f).background(Color(0xFFF3F7FF))
    ) {
        val w = size.width
        val h = size.height
        lesson.strokes.forEachIndexed { si, s ->
            val pts = s.points.map { Offset(it.x * w, it.y * h) }
            if (pts.size < 2) return@forEachIndexed

            val done = progress * lesson.strokes.size - si
            val count = min(pts.size, (done * (pts.size - 1) + 1).toInt().coerceIn(1, pts.size))
            for (i in 0 until count - 1) {
                drawLine(
                    color = Color(0xFF315CFF),
                    start = pts[i], end = pts[i + 1],
                    strokeWidth = 13f, cap = StrokeCap.Round
                )
            }

            if (si == 0) drawCircle(Color(0xFF27AE60), 16f, pts.first())

            if (done in 0f..1.2f) {
                val k = (done.coerceIn(0f, 1f)) * (pts.size - 1)
                val i = k.toInt().coerceIn(0, pts.size - 2)
                val t = k - i
                val cur = Offset(
                    pts[i].x + (pts[i+1].x - pts[i].x) * t,
                    pts[i].y + (pts[i+1].y - pts[i].y) * t
                )
                drawCircle(Color(0xFFFF8A00), 19f, cur)
                val dx = pts[i+1].x - pts[i].x
                val dy = pts[i+1].y - pts[i].y
                val len = kotlin.math.sqrt(dx*dx + dy*dy).coerceAtLeast(1f)
                val ux = dx / len
                val uy = dy / len
                val left = Offset(cur.x - ux*28f + uy*14f, cur.y - uy*28f - ux*14f)
                val right = Offset(cur.x - ux*28f - uy*14f, cur.y - uy*28f + ux*14f)
                drawLine(Color(0xFFFF8A00), cur, left, strokeWidth = 8f, cap = StrokeCap.Round)
                drawLine(Color(0xFFFF8A00), cur, right, strokeWidth = 8f, cap = StrokeCap.Round)
            }
        }
    }
}
