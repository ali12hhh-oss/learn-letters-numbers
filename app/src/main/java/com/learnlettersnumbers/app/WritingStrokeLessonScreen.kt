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
import androidx.compose.ui.graphics.*
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlin.math.max
import kotlin.math.min

private data class Stroke(val points: List<Offset>)
private data class Lesson(val symbol: String, val label: String, val language: String, val strokes: List<Stroke>)
private fun stroke(vararg p: Offset) = Stroke(p.toList())
private enum class ArabicForm { INITIAL, MEDIAL, FINAL }
private enum class EnglishCase { UPPER, LOWER }

private val arLetters = listOf("ا","ب","ت","ث","ج","ح","خ","د","ذ","ر","ز","س","ش","ص","ض","ط","ظ","ع","غ","ف","ق","ك","ل","م","ن","ه","و","ي")
private val arNames = listOf("الألف","الباء","التاء","الثاء","الجيم","الحاء","الخاء","الدال","الذال","الراء","الزاي","السين","الشين","الصاد","الضاد","الطاء","الظاء","العين","الغين","الفاء","القاف","الكاف","اللام","الميم","النون","الهاء","الواو","الياء")

private val arTracePaths = listOf(
 listOf(stroke(Offset(.50f,.16f),Offset(.50f,.84f))),
 listOf(stroke(Offset(.72f,.45f),Offset(.60f,.58f),Offset(.42f,.62f),Offset(.28f,.48f)),stroke(Offset(.50f,.76f))),
 listOf(stroke(Offset(.72f,.45f),Offset(.60f,.58f),Offset(.42f,.62f),Offset(.28f,.48f)),stroke(Offset(.44f,.28f)),stroke(Offset(.56f,.28f))),
 listOf(stroke(Offset(.72f,.45f),Offset(.60f,.58f),Offset(.42f,.62f),Offset(.28f,.48f)),stroke(Offset(.40f,.28f)),stroke(Offset(.50f,.24f)),stroke(Offset(.60f,.28f))),
 listOf(stroke(Offset(.70f,.38f),Offset(.54f,.54f),Offset(.34f,.58f),Offset(.40f,.72f),Offset(.58f,.70f)),stroke(Offset(.50f,.82f))),
 listOf(stroke(Offset(.70f,.38f),Offset(.54f,.54f),Offset(.34f,.58f),Offset(.40f,.72f),Offset(.58f,.70f))),
 listOf(stroke(Offset(.70f,.38f),Offset(.54f,.54f),Offset(.34f,.58f),Offset(.40f,.72f),Offset(.58f,.70f)),stroke(Offset(.50f,.25f))),
 listOf(stroke(Offset(.42f,.42f),Offset(.50f,.58f),Offset(.58f,.42f))),
 listOf(stroke(Offset(.42f,.42f),Offset(.50f,.58f),Offset(.58f,.42f))),
 listOf(stroke(Offset(.52f,.18f),Offset(.42f,.48f),Offset(.48f,.78f))),
 listOf(stroke(Offset(.52f,.18f),Offset(.42f,.48f),Offset(.48f,.78f))),
 listOf(stroke(Offset(.70f,.30f),Offset(.50f,.18f),Offset(.30f,.30f),Offset(.25f,.55f),Offset(.50f,.78f),Offset(.72f,.62f))),
 listOf(stroke(Offset(.70f,.30f),Offset(.50f,.18f),Offset(.30f,.30f),Offset(.25f,.55f),Offset(.50f,.78f),Offset(.72f,.62f)),stroke(Offset(.50f,.20f))),
 listOf(stroke(Offset(.70f,.34f),Offset(.58f,.20f),Offset(.35f,.25f),Offset(.25f,.50f),Offset(.35f,.75f),Offset(.58f,.80f),Offset(.70f,.66f))),
 listOf(stroke(Offset(.70f,.34f),Offset(.58f,.20f),Offset(.35f,.25f),Offset(.25f,.50f),Offset(.35f,.75f),Offset(.58f,.80f),Offset(.70f,.66f))),
 listOf(stroke(Offset(.30f,.30f),Offset(.70f,.30f),Offset(.55f,.52f),Offset(.35f,.78f))),
 listOf(stroke(Offset(.30f,.30f),Offset(.70f,.30f),Offset(.55f,.52f),Offset(.35f,.78f))),
 listOf(stroke(Offset(.35f,.75f),Offset(.28f,.55f),Offset(.34f,.32f),Offset(.55f,.20f),Offset(.72f,.34f),Offset(.60f,.52f),Offset(.40f,.55f))),
 listOf(stroke(Offset(.35f,.75f),Offset(.28f,.55f),Offset(.34f,.32f),Offset(.55f,.20f),Offset(.72f,.34f),Offset(.60f,.52f),Offset(.40f,.55f))),
 listOf(stroke(Offset(.40f,.28f),Offset(.60f,.22f),Offset(.72f,.38f),Offset(.62f,.52f),Offset(.40f,.55f),Offset(.30f,.72f))),
 listOf(stroke(Offset(.40f,.28f),Offset(.60f,.22f),Offset(.72f,.38f),Offset(.62f,.52f),Offset(.40f,.55f),Offset(.30f,.72f))),
 listOf(stroke(Offset(.30f,.25f),Offset(.70f,.25f)),stroke(Offset(.50f,.25f),Offset(.50f,.80f))),
 listOf(stroke(Offset(.70f,.25f),Offset(.30f,.25f),Offset(.50f,.80f))),
 listOf(stroke(Offset(.65f,.28f),Offset(.35f,.28f),Offset(.30f,.55f),Offset(.50f,.76f),Offset(.70f,.60f))),
 listOf(stroke(Offset(.35f,.25f),Offset(.35f,.75f)),stroke(Offset(.60f,.35f),Offset(.35f,.50f),Offset(.70f,.75f))),
 listOf(stroke(Offset(.35f,.30f),Offset(.50f,.20f),Offset(.68f,.30f),Offset(.70f,.58f),Offset(.50f,.78f),Offset(.32f,.68f)))
)

private val enLetters = ('A'..'Z').toList()
private val enNames = enLetters.map { it.toString() }

private val enUpperTracePaths = listOf(
 listOf(stroke(Offset(.50f,.10f),Offset(.25f,.88f)),stroke(Offset(.50f,.10f),Offset(.75f,.88f)),stroke(Offset(.36f,.58f),Offset(.64f,.58f))),
 listOf(stroke(Offset(.30f,.12f),Offset(.30f,.88f)),stroke(Offset(.30f,.14f),Offset(.62f,.24f),Offset(.38f,.50f)),stroke(Offset(.38f,.50f),Offset(.64f,.64f),Offset(.30f,.86f))),
 listOf(stroke(Offset(.72f,.22f),Offset(.58f,.14f),Offset(.38f,.18f),Offset(.26f,.50f),Offset(.38f,.82f),Offset(.58f,.86f),Offset(.72f,.78f))),
 listOf(stroke(Offset(.30f,.12f),Offset(.30f,.88f)),stroke(Offset(.30f,.14f),Offset(.60f,.22f),Offset(.70f,.50f),Offset(.60f,.78f),Offset(.30f,.86f))),
 listOf(stroke(Offset(.70f,.14f),Offset(.30f,.14f),Offset(.30f,.86f),Offset(.70f,.86f)),stroke(Offset(.30f,.50f),Offset(.60f,.50f))),
 listOf(stroke(Offset(.70f,.14f),Offset(.30f,.14f),Offset(.30f,.86f)),stroke(Offset(.30f,.50f),Offset(.60f,.50f))),
 listOf(stroke(Offset(.70f,.28f),Offset(.58f,.16f),Offset(.38f,.20f),Offset(.26f,.50f),Offset(.38f,.80f),Offset(.60f,.84f),Offset(.70f,.70f)),stroke(Offset(.70f,.56f),Offset(.52f,.56f))),
 listOf(stroke(Offset(.30f,.12f),Offset(.30f,.88f)),stroke(Offset(.70f,.12f),Offset(.70f,.88f)),stroke(Offset(.30f,.50f),Offset(.70f,.50f))),
 listOf(stroke(Offset(.35f,.12f),Offset(.35f,.88f)),stroke(Offset(.65f,.12f),Offset(.65f,.88f)),stroke(Offset(.50f,.12f),Offset(.50f,.88f))),
 listOf(stroke(Offset(.58f,.12f),Offset(.58f,.68f),Offset(.50f,.84f),Offset(.34f,.86f),Offset(.26f,.74f))),
 listOf(stroke(Offset(.30f,.12f),Offset(.30f,.88f)),stroke(Offset(.30f,.50f),Offset(.70f,.12f)),stroke(Offset(.30f,.50f),Offset(.72f,.88f))),
 listOf(stroke(Offset(.30f,.12f),Offset(.30f,.88f)),stroke(Offset(.30f,.12f),Offset(.70f,.88f))),
 listOf(stroke(Offset(.25f,.88f),Offset(.25f,.12f),Offset(.50f,.55f),Offset(.75f,.12f),Offset(.75f,.88f))),
 listOf(stroke(Offset(.30f,.88f),Offset(.30f,.12f),Offset(.70f,.88f),Offset(.70f,.12f))),
 listOf(stroke(Offset(.50f,.12f),Offset(.32f,.20f),Offset(.25f,.50f),Offset(.32f,.80f),Offset(.50f,.88f),Offset(.68f,.80f),Offset(.75f,.50f),Offset(.68f,.20f),Offset(.50f,.12f))),
 listOf(stroke(Offset(.30f,.88f),Offset(.30f,.12f),Offset(.60f,.18f),Offset(.70f,.36f),Offset(.60f,.54f),Offset(.30f,.56f))),
 listOf(stroke(Offset(.50f,.12f),Offset(.32f,.20f),Offset(.25f,.50f),Offset(.34f,.76f),Offset(.50f,.88f),Offset(.62f,.76f),Offset(.75f,.50f),Offset(.68f,.20f),Offset(.50f,.12f)),stroke(Offset(.52f,.58f),Offset(.75f,.88f))),
 listOf(stroke(Offset(.30f,.88f),Offset(.30f,.12f),Offset(.58f,.18f),Offset(.70f,.34f),Offset(.58f,.52f),Offset(.30f,.54f)),stroke(Offset(.46f,.54f),Offset(.72f,.88f))),
 listOf(stroke(Offset(.70f,.20f),Offset(.58f,.14f),Offset(.38f,.18f),Offset(.28f,.34f),Offset(.72f,.70f),Offset(.62f,.84f),Offset(.40f,.86f),Offset(.28f,.76f))),
 listOf(stroke(Offset(.25f,.14f),Offset(.75f,.14f)),stroke(Offset(.50f,.14f),Offset(.50f,.88f))),
 listOf(stroke(Offset(.25f,.12f),Offset(.75f,.12f)),stroke(Offset(.50f,.12f),Offset(.50f,.88f)),stroke(Offset(.75f,.12f),Offset(.75f,.50f))),
 listOf(stroke(Offset(.25f,.12f),Offset(.45f,.88f),Offset(.55f,.88f),Offset(.75f,.12f)),stroke(Offset(.34f,.50f),Offset(.66f,.50f))),
 listOf(stroke(Offset(.30f,.12f),Offset(.30f,.88f)),stroke(Offset(.70f,.12f),Offset(.70f,.88f)),stroke(Offset(.30f,.12f),Offset(.50f,.88f)),stroke(Offset(.70f,.12f),Offset(.50f,.88f))),
 listOf(stroke(Offset(.25f,.12f),Offset(.50f,.50f),Offset(.75f,.12f)),stroke(Offset(.50f,.50f),Offset(.50f,.88f))),
 listOf(stroke(Offset(.25f,.12f),Offset(.75f,.12f),Offset(.50f,.50f),Offset(.50f,.88f)))
)

private val enLowerTracePaths = listOf(
 listOf(stroke(Offset(.62f,.44f),Offset(.50f,.34f),Offset(.34f,.38f),Offset(.28f,.56f),Offset(.34f,.72f),Offset(.50f,.76f),Offset(.62f,.66f)),stroke(Offset(.62f,.28f),Offset(.62f,.82f))),
 listOf(stroke(Offset(.34f,.18f),Offset(.34f,.82f)),stroke(Offset(.34f,.44f),Offset(.46f,.34f),Offset(.62f,.38f),Offset(.66f,.56f),Offset(.58f,.72f),Offset(.42f,.76f),Offset(.34f,.68f))),
 listOf(stroke(Offset(.64f,.44f),Offset(.52f,.36f),Offset(.36f,.40f),Offset(.30f,.56f),Offset(.38f,.72f),Offset(.54f,.74f),Offset(.64f,.66f))),
 listOf(stroke(Offset(.66f,.18f),Offset(.66f,.82f)),stroke(Offset(.66f,.46f),Offset(.54f,.36f),Offset(.38f,.40f),Offset(.32f,.58f),Offset(.40f,.74f),Offset(.56f,.74f),Offset(.66f,.66f))),
 listOf(stroke(Offset(.32f,.60f),Offset(.36f,.46f),Offset(.52f,.38f),Offset(.66f,.48f),Offset(.62f,.66f),Offset(.46f,.74f),Offset(.32f,.66f),Offset(.66f,.66f))),
 listOf(stroke(Offset(.58f,.18f),Offset(.48f,.16f),Offset(.40f,.28f),Offset(.40f,.74f)),stroke(Offset(.28f,.42f),Offset(.58f,.42f))),
 listOf(stroke(Offset(.64f,.42f),Offset(.52f,.34f),Offset(.36f,.40f),Offset(.32f,.58f),Offset(.40f,.72f),Offset(.56f,.72f),Offset(.64f,.62f)),stroke(Offset(.64f,.36f),Offset(.64f,.88f),Offset(.52f,.94f),Offset(.38f,.90f))),
 listOf(stroke(Offset(.34f,.18f),Offset(.34f,.82f)),stroke(Offset(.34f,.46f),Offset(.46f,.36f),Offset(.60f,.40f),Offset(.62f,.56f),Offset(.62f,.82f))),
 listOf(stroke(Offset(.50f,.34f),Offset(.50f,.82f)),stroke(Offset(.50f,.20f),Offset(.50f,.20f))),
 listOf(stroke(Offset(.56f,.36f),Offset(.56f,.78f),Offset(.48f,.88f),Offset(.36f,.84f))),
 listOf(stroke(Offset(.34f,.18f),Offset(.34f,.82f)),stroke(Offset(.34f,.56f),Offset(.58f,.36f)),stroke(Offset(.46f,.48f),Offset(.64f,.78f))),
 listOf(stroke(Offset(.34f,.18f),Offset(.34f,.82f)),stroke(Offset(.34f,.54f),Offset(.64f,.38f))),
 listOf(stroke(Offset(.24f,.82f),Offset(.24f,.44f),Offset(.36f,.36f),Offset(.48f,.44f),Offset(.48f,.82f)),stroke(Offset(.48f,.44f),Offset(.60f,.36f),Offset(.72f,.44f),Offset(.72f,.82f))),
 listOf(stroke(Offset(.34f,.82f),Offset(.34f,.36f),Offset(.48f,.34f),Offset(.62f,.42f),Offset(.62f,.82f))),
 listOf(stroke(Offset(.62f,.44f),Offset(.50f,.34f),Offset(.36f,.40f),Offset(.30f,.58f),Offset(.38f,.74f),Offset(.54f,.74f),Offset(.62f,.64f))),
 listOf(stroke(Offset(.34f,.82f),Offset(.34f,.36f),Offset(.48f,.34f),Offset(.62f,.42f),Offset(.60f,.58f),Offset(.48f,.66f),Offset(.34f,.62f))),
 listOf(stroke(Offset(.62f,.82f),Offset(.62f,.36f),Offset(.48f,.34f),Offset(.34f,.42f),Offset(.38f,.60f),Offset(.52f,.66f),Offset(.62f,.58f)),stroke(Offset(.50f,.66f),Offset(.68f,.82f))),
 listOf(stroke(Offset(.62f,.42f),Offset(.50f,.34f),Offset(.36f,.40f),Offset(.34f,.58f),Offset(.44f,.68f),Offset(.60f,.62f))),
 listOf(stroke(Offset(.52f,.36f),Offset(.40f,.34f),Offset(.34f,.44f),Offset(.62f,.76f),Offset(.50f,.82f),Offset(.36f,.78f))),
 listOf(stroke(Offset(.32f,.36f),Offset(.68f,.36f)),stroke(Offset(.50f,.36f),Offset(.50f,.82f))),
 listOf(stroke(Offset(.34f,.36f),Offset(.66f,.36f)),stroke(Offset(.50f,.36f),Offset(.50f,.82f)),stroke(Offset(.66f,.36f),Offset(.66f,.60f))),
 listOf(stroke(Offset(.28f,.38f),Offset(.46f,.82f),Offset(.54f,.82f),Offset(.72f,.38f)),stroke(Offset(.36f,.58f),Offset(.64f,.58f))),
 listOf(stroke(Offset(.32f,.36f),Offset(.32f,.82f)),stroke(Offset(.68f,.36f),Offset(.68f,.82f)),stroke(Offset(.32f,.36f),Offset(.50f,.82f)),stroke(Offset(.68f,.36f),Offset(.50f,.82f))),
 listOf(stroke(Offset(.30f,.36f),Offset(.50f,.58f),Offset(.70f,.36f)),stroke(Offset(.50f,.58f),Offset(.50f,.82f))),
 listOf(stroke(Offset(.30f,.36f),Offset(.70f,.36f),Offset(.50f,.58f),Offset(.50f,.82f)))
)

@Composable
fun WritingStrokeLessonScreen(language: String, numbers: Boolean, onBack: () -> Unit, speak: (String, String) -> Unit) {
    val arabic = language == "ar"
    var index by remember { mutableIntStateOf(0) }
    var replay by remember { mutableIntStateOf(0) }
    var arForm by remember { mutableStateOf(ArabicForm.INITIAL) }
    var enCase by remember { mutableStateOf(EnglishCase.UPPER) }

    val lessons = if (numbers) {
        (0..9).map { Lesson(it.toString(), it.toString(), language, listOf(stroke(Offset(.50f,.15f),Offset(.50f,.85f)))) }
    } else if (arabic) {
        arLetters.mapIndexed { i, s -> Lesson(s, arNames[i], "ar", arTracePaths.getOrElse(i) { listOf(stroke(Offset(.5f,.2f),Offset(.5f,.8f))) }) }
    } else {
        enLetters.mapIndexed { i, c ->
            val lower = enCase == EnglishCase.LOWER
            Lesson(if (lower) c.lowercaseChar().toString() else c.toString(), c.toString(), "en", if (lower) enLowerTracePaths[i] else enUpperTracePaths[i])
        }
    }

    val lesson = lessons[index.coerceIn(0, lessons.lastIndex)]
    val fullSymbol = if (arabic && !numbers) arabicFormSymbol(index, arForm) else lesson.symbol

    LaunchedEffect(index, replay, arForm, enCase) {
        val message = when {
            numbers -> if (arabic) "تعلم كتابة الرقم ${lesson.symbol}" else "Learn to write number ${lesson.symbol}"
            arabic -> "تعلم كتابة ${lesson.label}، الشكل ${arabicFormName(arForm)}"
            enCase == EnglishCase.UPPER -> "Learn to write capital letter ${lesson.label}"
            else -> "Learn to write lowercase letter ${lesson.label.lowercase()}"
        }
        speak(message, if (numbers) "ar" else language)
    }

    CompositionLocalProvider(androidx.compose.ui.platform.LocalLayoutDirection provides if (arabic) androidx.compose.ui.unit.LayoutDirection.Rtl else androidx.compose.ui.unit.LayoutDirection.Ltr) {
        Scaffold(topBar = {
            TopAppBar(
                title = { Text(if (arabic) "تعلم الكتابة خطوة بخطوة" else "Learn to Write") },
                navigationIcon = { IconButton(onClick = onBack) { Icon(Icons.Default.ArrowBack, contentDescription = "رجوع") } }
            )
        }) { pad ->
            Column(Modifier.fillMaxSize().padding(pad).padding(horizontal = 12.dp, vertical = 8.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    if (arabic) "شاهد الحرف كاملاً، اختر شكله، ثم اتبع إصبع اليد من نقطة البداية 👆"
                    else "See the complete letter, choose UPPERCASE or lowercase, then follow the hand from the starting point 👆",
                    fontSize = 17.sp, fontWeight = FontWeight.Bold, textAlign = TextAlign.Center
                )
                Spacer(Modifier.height(7.dp))

                if (arabic && !numbers) {
                    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(7.dp)) {
                        FormChoice("أولي", "بداية الحرف", arForm == ArabicForm.INITIAL, Color(0xFF4C8BF5), Modifier.weight(1f)) { arForm = ArabicForm.INITIAL; replay++ }
                        FormChoice("وسطي", "وسط الحرف", arForm == ArabicForm.MEDIAL, Color(0xFFFFA726), Modifier.weight(1f)) { arForm = ArabicForm.MEDIAL; replay++ }
                        FormChoice("أخري", "نهاية الحرف", arForm == ArabicForm.FINAL, Color(0xFF6BCB77), Modifier.weight(1f)) { arForm = ArabicForm.FINAL; replay++ }
                    }
                }

                if (!arabic && !numbers) {
                    Spacer(Modifier.height(2.dp))
                    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        FormChoice("UPPERCASE", "حروف كبيرة", enCase == EnglishCase.UPPER, Color(0xFF4C8BF5), Modifier.weight(1f)) { enCase = EnglishCase.UPPER; replay++ }
                        FormChoice("lowercase", "حروف صغيرة", enCase == EnglishCase.LOWER, Color(0xFFFF8A4C), Modifier.weight(1f)) { enCase = EnglishCase.LOWER; replay++ }
                    }
                }

                Spacer(Modifier.height(7.dp))
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.Center, verticalAlignment = Alignment.CenterVertically) {
                    Text("${index + 1} / ${lessons.size}", fontWeight = FontWeight.ExtraBold)
                    Spacer(Modifier.width(12.dp))
                    Text(fullSymbol, fontSize = if (arabic) 42.sp else 42.sp, fontWeight = FontWeight.Black, color = Color(0xFF315CFF))
                    Spacer(Modifier.width(8.dp))
                    Text(
                        when {
                            arabic && !numbers -> "${lesson.label} — ${arabicFormName(arForm)}"
                            !arabic && !numbers -> if (enCase == EnglishCase.UPPER) "${lesson.label} — حروف كبيرة" else "${lesson.label.lowercase()} — حروف صغيرة"
                            else -> lesson.label
                        },
                        fontSize = 16.sp, fontWeight = FontWeight.Bold
                    )
                }
                Spacer(Modifier.height(6.dp))

                Card(Modifier.fillMaxWidth().weight(1f), shape = RoundedCornerShape(28.dp), colors = CardDefaults.cardColors(containerColor = Color(0xFFFFFBF0)), elevation = CardDefaults.cardElevation(9.dp)) {
                    Column(Modifier.fillMaxSize().padding(8.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                        TraceBoard(fullSymbol, lesson, replay, arabic)
                        Text(
                            if (arabic) "🟢 نقطة البداية  →  🟠 إصبع اليد يتبع المسار  →  🔵 أكمل الحرف"
                            else "🟢 Start  →  🟠 Follow the hand  →  🔵 Finish the letter",
                            fontSize = 15.sp, fontWeight = FontWeight.Bold, textAlign = TextAlign.Center, modifier = Modifier.padding(vertical = 5.dp)
                        )
                    }
                }
                Spacer(Modifier.height(7.dp))
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    LessonButton(if (arabic) "السابق" else "Previous", Color(0xFF5C6BC0), index > 0, Modifier.weight(1f)) { if (index > 0) { index--; replay++ } }
                    LessonButton("🔄 إعادة", Color(0xFF039BE5), true, Modifier.weight(1f)) { replay++ }
                    LessonButton(if (arabic) "التالي" else "Next", Color(0xFF2EAD69), index < lessons.lastIndex, Modifier.weight(1f)) { if (index < lessons.lastIndex) { index++; replay++ } }
                }
            }
        }
    }
}

@Composable private fun FormChoice(title: String, subtitle: String, selected: Boolean, color: Color, modifier: Modifier, onClick: () -> Unit) {
    Card(modifier.clickable(onClick = onClick), shape = RoundedCornerShape(17.dp), colors = CardDefaults.cardColors(containerColor = if (selected) color else Color.White), elevation = CardDefaults.cardElevation(if (selected) 8.dp else 3.dp)) {
        Column(Modifier.fillMaxWidth().padding(vertical = 7.dp), horizontalAlignment = Alignment.CenterHorizontally) {
            Text(title, fontSize = 17.sp, fontWeight = FontWeight.Black, color = if (selected) Color.White else color, textAlign = TextAlign.Center)
            Text(subtitle, fontSize = 11.sp, color = if (selected) Color.White else Color.DarkGray, textAlign = TextAlign.Center)
        }
    }
}

@Composable private fun LessonButton(text: String, color: Color, enabled: Boolean, modifier: Modifier, onClick: () -> Unit) {
    Button(onClick = onClick, enabled = enabled, modifier = modifier.height(54.dp), shape = RoundedCornerShape(17.dp), colors = ButtonDefaults.buttonColors(containerColor = color, disabledContainerColor = Color(0xFFD9E0E5))) {
        Text(text, fontWeight = FontWeight.ExtraBold, fontSize = 16.sp)
    }
}

@Composable private fun TraceBoard(fullSymbol: String, lesson: Lesson, replay: Int, arabic: Boolean) {
    var progress by remember(fullSymbol, lesson.label, replay) { mutableFloatStateOf(0f) }
    LaunchedEffect(fullSymbol, lesson.label, replay) { animate(0f, 1f, tween(3200, easing = LinearEasing)) { v, _ -> progress = v } }
    Box(Modifier.fillMaxWidth().weight(1f).background(Color(0xFFF2F7FF), RoundedCornerShape(22.dp)).border(3.dp, Color(0xFFD5E5F5), RoundedCornerShape(22.dp))) {
        Canvas(Modifier.fillMaxSize().padding(8.dp)) {
            val w = size.width
            val h = size.height
            drawContext.canvas.nativeCanvas.drawText(fullSymbol, w / 2f, h * .70f, android.graphics.Paint().apply {
                textSize = min(w, h) * if (arabic) .62f else .56f
                textAlign = android.graphics.Paint.Align.CENTER
                color = android.graphics.Color.rgb(100, 125, 150)
                alpha = 70
                typeface = android.graphics.Typeface.DEFAULT_BOLD
            })
            lesson.strokes.forEachIndexed { si, s ->
                val pts = s.points.map { Offset(it.x * w, it.y * h) }
                if (pts.size < 2) return@forEachIndexed
                val done = progress * lesson.strokes.size - si
                val count = min(pts.size, (done * (pts.size - 1) + 1).toInt().coerceIn(1, pts.size))
                for (i in 0 until count - 1) drawLine(Color(0xFF315CFF), pts[i], pts[i + 1], 12f, cap = StrokeCap.Round)
                drawCircle(Color(0xFF27AE60), 13f, pts.first())
                if (done in 0f..1.15f) {
                    val k = done.coerceIn(0f, 1f) * (pts.size - 1)
                    val i = k.toInt().coerceIn(0, pts.size - 2)
                    val t = k - i
                    drawCircle(Color(0xFFFF8A00), 17f, Offset(pts[i].x + (pts[i + 1].x - pts[i].x) * t, pts[i].y + (pts[i + 1].y - pts[i].y) * t))
                }
            }
        }
        FingerGuide(lesson, progress)
    }
}

@Composable private fun FingerGuide(lesson: Lesson, progress: Float) {
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
    val x = pts[i].x + (pts[i + 1].x - pts[i].x) * t
    val y = pts[i].y + (pts[i + 1].y - pts[i].y) * t
    BoxWithConstraints(Modifier.fillMaxSize().padding(8.dp)) {
        Text("☝️", fontSize = 38.sp, modifier = Modifier.offset(x = (x * maxWidth.value).dp, y = (y * maxHeight.value).dp))
    }
}

private fun arabicFormName(form: ArabicForm) = when (form) { ArabicForm.INITIAL -> "أولي"; ArabicForm.MEDIAL -> "وسطي"; ArabicForm.FINAL -> "أخري" }
private fun arabicFormSymbol(index: Int, form: ArabicForm): String {
    val c = arLetters[index]
    return when (form) {
        ArabicForm.INITIAL -> when (c) { "ا"->"ا";"ب"->"بـ";"ت"->"تـ";"ث"->"ثـ";"ج"->"جـ";"ح"->"حـ";"خ"->"خـ";"د"->"د";"ذ"->"ذ";"ر"->"ر";"ز"->"ز";"س"->"سـ";"ش"->"شـ";"ص"->"صـ";"ض"->"ضـ";"ط"->"طـ";"ظ"->"ظـ";"ع"->"عـ";"غ"->"غـ";"ف"->"فـ";"ق"->"قـ";"ك"->"كـ";"ل"->"لـ";"م"->"مـ";"ن"->"نـ";"ه"->"هـ";"و"->"و";"ي"->"يـ";else->c }
        ArabicForm.MEDIAL -> when (c) { "ا"->"ـا";"ب"->"ـبـ";"ت"->"ـتـ";"ث"->"ـثـ";"ج"->"ـجـ";"ح"->"ـحـ";"خ"->"ـخـ";"د"->"د";"ذ"->"ذ";"ر"->"ر";"ز"->"ز";"س"->"ـسـ";"ش"->"ـشـ";"ص"->"ـصـ";"ض"->"ـضـ";"ط"->"ـطـ";"ظ"->"ـظـ";"ع"->"ـعـ";"غ"->"ـغـ";"ف"->"ـفـ";"ق"->"ـقـ";"ك"->"ـكـ";"ل"->"ـلـ";"م"->"ـمـ";"ن"->"ـنـ";"ه"->"ـهـ";"و"->"و";"ي"->"ـيـ";else->c }
        ArabicForm.FINAL -> when (c) { "ا"->"ا";"ب"->"ـب";"ت"->"ـت";"ث"->"ـث";"ج"->"ـج";"ح"->"ـح";"خ"->"ـخ";"د"->"د";"ذ"->"ذ";"ر"->"ر";"ز"->"ز";"س"->"ـس";"ش"->"ـش";"ص"->"ـص";"ض"->"ـض";"ط"->"ـط";"ظ"->"ـظ";"ع"->"ـع";"غ"->"ـغ";"ف"->"ـف";"ق"->"ـق";"ك"->"ـك";"ل"->"ـل";"م"->"ـم";"ن"->"ـن";"ه"->"ـه";"و"->"و";"ي"->"ـي";else->c }
    }
}
