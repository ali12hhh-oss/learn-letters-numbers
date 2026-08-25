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

private data class WritingStep(
    val symbol: String,
    val name: String,
    val instruction: String,
    val language: String
)

private val arabicSteps = listOf(
    WritingStep("ا", "الألف", "ابدأ من أعلى الحرف واسحب القلم إلى الأسفل.", "ar"),
    WritingStep("ب", "الباء", "ابدأ من اليمين، ارسم الجسم ثم ضع النقطة أسفل الحرف.", "ar"),
    WritingStep("ت", "التاء", "ارسم الجسم من اليمين ثم ضع نقطتين فوق الحرف.", "ar"),
    WritingStep("ث", "الثاء", "ارسم الجسم ثم ضع ثلاث نقاط فوق الحرف.", "ar"),
    WritingStep("ج", "الجيم", "ابدأ من اليمين وارسم القوس ثم النقطة أسفل الحرف.", "ar"),
    WritingStep("ح", "الحاء", "ابدأ من اليمين وارسم القوس بسلاسة.", "ar"),
    WritingStep("خ", "الخاء", "ارسم شكل الحاء ثم ضع النقطة فوقه.", "ar"),
    WritingStep("د", "الدال", "ابدأ من اليمين وارسم الانحناءة إلى اليسار.", "ar"),
    WritingStep("ذ", "الذال", "اكتب الدال ثم أضف النقطة فوقه.", "ar"),
    WritingStep("ر", "الراء", "ابدأ من اليمين وانزل بانحناءة قصيرة.", "ar"),
    WritingStep("ز", "الزاي", "اكتب الراء ثم أضف النقطة فوقه.", "ar"),
    WritingStep("س", "السين", "ابدأ من اليمين وارسم أسنان السين الثلاثة.", "ar"),
    WritingStep("ش", "الشين", "اكتب السين ثم أضف ثلاث نقاط فوقه.", "ar"),
    WritingStep("ص", "الصاد", "ابدأ من اليمين وارسم جسم الصاد بانحناءة واسعة.", "ar"),
    WritingStep("ض", "الضاد", "اكتب الصاد ثم أضف النقطة فوقه.", "ar"),
    WritingStep("ط", "الطاء", "ابدأ من الأعلى ثم أكمل جسم الطاء.", "ar"),
    WritingStep("ظ", "الظاء", "اكتب الطاء ثم أضف النقطة فوقه.", "ar"),
    WritingStep("ع", "العين", "ابدأ من اليمين وارسم منحنى العين إلى الأسفل.", "ar"),
    WritingStep("غ", "الغين", "اكتب العين ثم أضف النقطة فوقه.", "ar"),
    WritingStep("ف", "الفاء", "ارسم جسم الفاء ثم ضع النقطة فوقه.", "ar"),
    WritingStep("ق", "القاف", "ارسم جسم القاف ثم ضع نقطتين فوقه.", "ar"),
    WritingStep("ك", "الكاف", "ابدأ من الأعلى وارسم ساق الكاف ثم جسمه.", "ar"),
    WritingStep("ل", "اللام", "ابدأ من الأعلى وانزل بخط مائل إلى اليسار.", "ar"),
    WritingStep("م", "الميم", "ابدأ من اليمين وارسم انحناءة الميم.", "ar"),
    WritingStep("ن", "النون", "ارسم جسم النون ثم ضع النقطة فوقه.", "ar"),
    WritingStep("ه", "الهاء", "ابدأ من اليمين وارسم دائرة الهاء بسلاسة.", "ar"),
    WritingStep("و", "الواو", "ابدأ من الأعلى وارسم الانحناءة ثم الذيل.", "ar"),
    WritingStep("ي", "الياء", "ارسم جسم الياء ثم ضع نقطتين تحته.", "ar")
)

private val englishSteps = ('A'..'Z').map { ch ->
    val lower = ch.lowercase()
    val instruction = when (ch) {
        'A' -> "Start at the top, go down, then cross the middle."
        'B' -> "Start at the top, draw the stem, then make the two curves."
        'C' -> "Start at the top and curve around to the bottom."
        'D' -> "Start at the top, draw the stem, then curve to the bottom."
        'E' -> "Draw the vertical line, then the top, middle, and bottom lines."
        'F' -> "Draw the vertical line, then the top and middle lines."
        'G' -> "Start at the top, curve around, then finish the inner stroke."
        'H' -> "Draw both vertical lines, then connect them in the middle."
        'I' -> "Draw the top, vertical stroke, and bottom."
        'J' -> "Start at the top, go down, then curve at the bottom."
        'K' -> "Draw the vertical line, then the two diagonal strokes."
        'L' -> "Draw the vertical line, then the bottom line."
        'M' -> "Start at the top and draw the two outer lines and center strokes."
        'N' -> "Draw the first vertical, diagonal, then second vertical."
        'O' -> "Start at the top and draw one smooth oval."
        'P' -> "Draw the vertical line, then the upper curve."
        'Q' -> "Draw an oval, then add the small diagonal tail."
        'R' -> "Draw the stem and upper curve, then the diagonal leg."
        'S' -> "Start at the top and make a smooth S curve."
        'T' -> "Draw the top line, then the vertical stroke."
        'U' -> "Start at the top, go down, curve, and go back up."
        'V' -> "Start at the top and meet at the bottom point."
        'W' -> "Start at the top and make four connected diagonal strokes."
        'X' -> "Draw one diagonal stroke, then cross it with the other."
        'Y' -> "Draw the two upper strokes, then the center downstroke."
        else -> "Draw the upper diagonal, then the lower diagonal strokes."
    }
    WritingStep("$ch $lower", "$ch / $lower", instruction, "en")
}


private fun arabicDigits(n: Int): String = n.toString().map { ch ->
    when (ch) {
        '0' -> '٠'; '1' -> '١'; '2' -> '٢'; '3' -> '٣'; '4' -> '٤'
        '5' -> '٥'; '6' -> '٦'; '7' -> '٧'; '8' -> '٨'; else -> '٩'
    }
}.joinToString("")

private fun numberSteps(language: String): List<WritingStep> = (1..100).map { n ->
    val symbol = if (language == "ar") arabicDigits(n) else n.toString()
    val instruction = if (language == "ar")
        "ابدأ من النقطة الخضراء واتبع السهم خطوة بخطوة لكتابة الرقم $symbol."
    else
        "Start at the green dot and follow the arrow step by step to write $symbol."
    WritingStep(symbol, symbol, instruction, language)
}

@Composable
fun WritingTutorialScreen(
    language: String,
    onBack: () -> Unit,
    speak: (String, String) -> Unit
) {
    var mode by remember { mutableStateOf("letters") }
    val steps = if (mode == "letters") {
        if (language == "ar") arabicSteps else englishSteps
    } else numberSteps(language)
    var index by remember(mode) { mutableStateOf(0) }
    var replayKey by remember { mutableStateOf(0) }
    val item = steps[index]

    LaunchedEffect(index, replayKey) {
        val message = if (language == "ar") {
            "تعلم كتابة ${item.name}. ${item.instruction}"
        } else {
            "Learn to write ${item.name}. ${item.instruction}"
        }
        speak(message, item.language)
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(if (language == "ar") "تعلم الكتابة" else "Learn to Write") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "رجوع")
                    }
                }
            )
        }
    ) { padding ->
        Column(
            Modifier.fillMaxSize().padding(padding).padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                if (language == "ar") {
                    if (mode == "letters") "شاهد السهم ثم جرّب كتابة الحرف بنفسك ✏️" else "شاهد السهم ثم جرّب كتابة الرقم بنفسك ✏️"
                } else {
                    if (mode == "letters") "Watch the arrow, then write the letter yourself ✏️" else "Watch the arrow, then write the number yourself ✏️"
                },
                fontSize = 21.sp,
                fontWeight = FontWeight.Bold
            )
            Spacer(Modifier.height(12.dp))
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                Button(
                    Modifier.weight(1f),
                    onClick = { mode = "letters"; replayKey++ }
                ) { Text(if (language == "ar") "الحروف" else "Letters") }
                Button(
                    Modifier.weight(1f),
                    onClick = { mode = "numbers"; replayKey++ }
                ) { Text(if (language == "ar") "الأرقام" else "Numbers") }
            }
            Spacer(Modifier.height(12.dp))
            Card(
                Modifier.fillMaxWidth().weight(1f),
                shape = RoundedCornerShape(28.dp),
                colors = CardDefaults.cardColors(containerColor = Color(0xFFFFF8E8)),
                elevation = CardDefaults.cardElevation(8.dp)
            ) {
                Column(
                    Modifier.fillMaxSize().padding(14.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        item.symbol,
                        fontSize = 112.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF315CFF)
                    )
                    Text(
                        if (language == "ar")
                            "نقطة البداية → اتجاه القلم → النهاية"
                        else
                            "Start point → stroke direction → finish",
                        fontSize = 14.sp
                    )
                    Spacer(Modifier.height(10.dp))
                    AnimatedWritingGuide(key = "$index-$replayKey")
                    Spacer(Modifier.height(10.dp))
                    Text(item.instruction, fontSize = 18.sp, fontWeight = FontWeight.SemiBold)
                }
            }
            Spacer(Modifier.height(12.dp))
            Row(
                Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                OutlinedButton(
                    Modifier.weight(1f),
                    enabled = index > 0,
                    onClick = { index-- }
                ) { Text("السابق") }
                Button(
                    Modifier.weight(1f),
                    onClick = {
                        replayKey++
                        speak(item.instruction, item.language)
                    }
                ) { Text("🔊 أعد الشرح") }
                Button(
                    Modifier.weight(1f),
                    enabled = index < steps.lastIndex,
                    onClick = { index++ }
                ) { Text("التالي") }
            }
        }
    }
}

@Composable
private fun AnimatedWritingGuide(key: String) {
    var progress by remember(key) { mutableStateOf(0f) }

    LaunchedEffect(key) {
        animate(
            initialValue = 0f,
            targetValue = 1f,
            animationSpec = tween(2400, easing = LinearEasing)
        ) { value, _ -> progress = value }
    }

    Canvas(
        Modifier.fillMaxWidth().height(105.dp).background(Color(0xFFF1F6FF))
    ) {
        val start = if (key.hashCode() % 2 == 0) Offset(size.width * .16f, size.height * .55f) else Offset(size.width * .84f, size.height * .55f)
        val end = if (key.hashCode() % 2 == 0) Offset(size.width * .84f, size.height * .55f) else Offset(size.width * .16f, size.height * .55f)
        val current = Offset(
            start.x + (end.x - start.x) * progress,
            start.y + (end.y - start.y) * progress
        )

        drawLine(
            color = Color(0xFFB8C7E8),
            start = start,
            end = end,
            strokeWidth = 12f,
            pathEffect = PathEffect.dashPathEffect(floatArrayOf(18f, 18f))
        )
        drawCircle(Color(0xFF27AE60), 14f, start)
        drawCircle(Color(0xFF315CFF), 18f, current)

        val arrowSize = 20f
        drawLine(
            Color(0xFF315CFF),
            current,
            Offset(current.x - arrowSize, current.y - arrowSize / 2),
            strokeWidth = 7f,
            cap = StrokeCap.Round
        )
        drawLine(
            Color(0xFF315CFF),
            current,
            Offset(current.x - arrowSize, current.y + arrowSize / 2),
            strokeWidth = 7f,
            cap = StrokeCap.Round
        )
    }
}
