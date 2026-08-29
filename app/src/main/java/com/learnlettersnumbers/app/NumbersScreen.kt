package com.learnlettersnumbers.app

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
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
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.platform.LocalLayoutDirection
import kotlin.random.Random

private enum class NumberMode { NUMBERS, OPERATIONS }
private enum class OperationMode { ADD, SUBTRACT }
private enum class PictureKind { FLOWERS, FRUITS, ANIMALS, BIKES }

@Composable
internal fun NumbersScreen(
    audio: LocalAudioManager,
    onTap: () -> Unit,
    onBack: () -> Unit,
    soundsEnabled: () -> Boolean = { true }
) {
    var mode by remember { mutableStateOf(NumberMode.NUMBERS) }
    var operation by remember { mutableStateOf(OperationMode.ADD) }
    var selected by remember { mutableIntStateOf(1) }
    var exampleSeed by remember { mutableIntStateOf(0) }

    val example = remember(operation, exampleSeed) { makeExample(operation, exampleSeed) }

    LaunchedEffect(mode, selected, operation, exampleSeed) {
        if (soundsEnabled()) {
            if (mode == NumberMode.NUMBERS) audio.playRequired("ar_number_%03d".format(selected))
            else audio.playOperationExample(NumbersExampleAudio(example.a, example.b, example.result))
        }
    }

    CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Rtl) {
        Box(
            Modifier.fillMaxSize().background(
                Brush.verticalGradient(listOf(MaterialTheme.colorScheme.background, MaterialTheme.colorScheme.surfaceVariant))
            )
        ) {
            Column(Modifier.fillMaxSize().padding(14.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                    Number3DButton("رجوع", Color(0xFF7E57C2), Modifier.width(100.dp)) { onBack(); onTap() }
                    Spacer(Modifier.weight(1f))
                    Text("الأرقام والعمليات", fontSize = 28.sp, fontWeight = FontWeight.ExtraBold, color = Color(0xFF0C5C86))
                }

                Spacer(Modifier.height(10.dp))
                Row(
                    Modifier.fillMaxWidth().background(MaterialTheme.colorScheme.surface.copy(alpha = .82f), RoundedCornerShape(20.dp)).padding(6.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    NumberModeButton("الأرقام ١–١٠٠", mode == NumberMode.NUMBERS, Modifier.weight(1f)) {
                        mode = NumberMode.NUMBERS; onTap()
                    }
                    NumberModeButton("الجمع والطرح", mode == NumberMode.OPERATIONS, Modifier.weight(1f)) {
                        mode = NumberMode.OPERATIONS; onTap()
                    }
                }

                Text(
                    if (mode == NumberMode.NUMBERS) "رائع! استخدم السابق والتالي لتعلّم الأرقام 🎈" else "هيا نتعلم الجمع والطرح بالصور! 🌟",
                    modifier = Modifier.padding(vertical = 8.dp).background(Color(0xFFFFE89A), RoundedCornerShape(18.dp)).padding(horizontal = 18.dp, vertical = 8.dp),
                    color = Color(0xFF713D00), fontSize = 17.sp, fontWeight = FontWeight.ExtraBold
                )

                if (mode == NumberMode.NUMBERS) {
                    Box(
                        Modifier.fillMaxWidth().weight(1f).shadow(12.dp, RoundedCornerShape(30.dp))
                            .background(MaterialTheme.colorScheme.surface, RoundedCornerShape(30.dp))
                            .border(5.dp, numberColor(selected), RoundedCornerShape(30.dp))
                            .clickable {
                                if (soundsEnabled()) audio.playRequired("ar_number_%03d".format(selected))
                                onTap()
                            },
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.Center) {
                            Text(
                                arabicDigits(selected),
                                fontSize = 128.sp,
                                fontWeight = FontWeight.Black,
                                color = numberColor(selected),
                                textAlign = TextAlign.Center
                            )
                            Spacer(Modifier.height(10.dp))
                            Text(
                                numberWords(selected),
                                fontSize = 25.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF155E8A),
                                textAlign = TextAlign.Center
                            )
                            Spacer(Modifier.height(8.dp))
                            Text("اضغط على الرقم لسماع النطق 🔊", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = Color(0xFF6B7280))
                        }
                    }

                    Spacer(Modifier.height(12.dp))
                    Row(
                        Modifier.fillMaxWidth().height(62.dp),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        NumberNavigationButton(
                            text = "السابق",
                            enabled = selected > 1,
                            color = Color(0xFF7E57C2),
                            modifier = Modifier.weight(1f)
                        ) {
                            if (selected > 1) {
                                selected--
                                onTap()
                            }
                        }
                        NumberNavigationButton(
                            text = "التالي",
                            enabled = selected < 100,
                            color = Color(0xFF039BE5),
                            modifier = Modifier.weight(1f)
                        ) {
                            if (selected < 100) {
                                selected++
                                onTap()
                            }
                        }
                    }
                } else {
                    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                        OperationButton("الجمع +", operation == OperationMode.ADD, Modifier.weight(1f), Color(0xFF2EAD69)) {
                            operation = OperationMode.ADD; onTap()
                        }
                        OperationButton("الطرح −", operation == OperationMode.SUBTRACT, Modifier.weight(1f), Color(0xFFE85D5D)) {
                            operation = OperationMode.SUBTRACT; onTap()
                        }
                    }
                    Spacer(Modifier.height(10.dp))
                    OperationCard(example, onTap) {
                        if (soundsEnabled()) audio.playOperationExample(NumbersExampleAudio(example.a, example.b, example.result))
                    }
                    Spacer(Modifier.height(10.dp))
                    Number3DButton("مثال جديد ✨", Color(0xFF039BE5), Modifier.fillMaxWidth()) {
                        exampleSeed++; onTap()
                    }
                    Spacer(Modifier.height(8.dp))
                    Text(
                        if (operation == OperationMode.ADD) "نجمع الأشياء معاً لنكتشف العدد الجديد!" else "نطرح الأشياء لنكتشف كم بقي!",
                        modifier = Modifier.background(MaterialTheme.colorScheme.surface.copy(alpha = .9f), RoundedCornerShape(16.dp)).padding(10.dp),
                        fontSize = 16.sp, fontWeight = FontWeight.Bold, color = Color(0xFF28506A), textAlign = TextAlign.Center
                    )
                }
            }
        }
    }
}

private data class OperationExample(
    val a: Int,
    val b: Int,
    val result: Int,
    val kind: PictureKind,
    val spoken: String
)

private fun makeExample(mode: OperationMode, seed: Int): OperationExample {
    val rnd = Random(seed + 41)
    val a = rnd.nextInt(1, 6)
    val b = if (mode == OperationMode.SUBTRACT) rnd.nextInt(1, a + 1) else rnd.nextInt(1, 6)
    val kind = PictureKind.entries[(seed.coerceAtLeast(0)) % PictureKind.entries.size]
    val noun = when (kind) {
        PictureKind.FLOWERS -> "زهرة"
        PictureKind.FRUITS -> "تفاحة"
        PictureKind.ANIMALS -> "قطة"
        PictureKind.BIKES -> "دراجة"
    }
    val result = if (mode == OperationMode.ADD) a + b else a - b
    val spoken = if (mode == OperationMode.ADD) {
        "لدينا ${numberWords(a)} $noun، وأضفنا ${numberWords(b)}، فأصبح لدينا ${numberWords(result)} ${pluralNoun(noun, result)}."
    } else {
        "لدينا ${numberWords(a)} $noun، أخذنا منها ${numberWords(b)}، فبقي لدينا ${numberWords(result)} ${pluralNoun(noun, result)}."
    }
    return OperationExample(a, b, result, kind, spoken)
}

private fun pluralNoun(noun: String, n: Int): String = when (noun) {
    "زهرة" -> if (n == 1) "زهرة" else "زهرات"
    "تفاحة" -> if (n == 1) "تفاحة" else "تفاحات"
    "قطة" -> if (n == 1) "قطة" else "قطط"
    "دراجة" -> if (n == 1) "دراجة" else "دراجات"
    else -> noun
}

@Composable
private fun OperationCard(example: OperationExample, onTap: () -> Unit, speak: () -> Unit) {
    val accent = when (example.kind) {
        PictureKind.FLOWERS -> Color(0xFFEC407A)
        PictureKind.FRUITS -> Color(0xFFFFA726)
        PictureKind.ANIMALS -> Color(0xFF42A5F5)
        PictureKind.BIKES -> Color(0xFF26A69A)
    }
    Column(
        Modifier.fillMaxWidth().shadow(12.dp, RoundedCornerShape(30.dp)).background(MaterialTheme.colorScheme.surface, RoundedCornerShape(30.dp)).border(4.dp, accent, RoundedCornerShape(30.dp)).clickable { speak(); onTap() }.padding(12.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Row(horizontalArrangement = Arrangement.Center, verticalAlignment = Alignment.CenterVertically) {
            PictureGroup(example.a, example.kind, accent)
            Text(if (example.spoken.contains("أضفنا")) "+" else "−", fontSize = 48.sp, fontWeight = FontWeight.Black, color = Color(0xFF7A4A00), modifier = Modifier.padding(horizontal = 8.dp))
            PictureGroup(example.b, example.kind, accent)
        }
        Spacer(Modifier.height(8.dp))
        Text(
            "${arabicDigits(example.a)} ${if (example.spoken.contains("أضفنا")) "+" else "−"} ${arabicDigits(example.b)} = ${arabicDigits(example.result)}",
            fontSize = 34.sp, fontWeight = FontWeight.Black, color = accent
        )
        Spacer(Modifier.height(6.dp))
        Text(
            example.spoken,
            modifier = Modifier.fillMaxWidth().background(Color(0xFFFFF7D6), RoundedCornerShape(18.dp)).padding(12.dp),
            fontSize = 18.sp, fontWeight = FontWeight.Bold, textAlign = TextAlign.Center, color = Color(0xFF5E3B17)
        )
        Spacer(Modifier.height(7.dp))
        OutlinedButton(onClick = { speak(); onTap() }, shape = RoundedCornerShape(16.dp)) { Text("🔊 اسمع الشرح", fontWeight = FontWeight.ExtraBold) }
    }
}

@Composable
private fun PictureGroup(count: Int, kind: PictureKind, accent: Color) {
    Row(horizontalArrangement = Arrangement.spacedBy(4.dp), verticalAlignment = Alignment.CenterVertically) {
        repeat(count) { PictureIcon(kind, accent, Modifier.size(30.dp)) }
    }
}

@Composable
private fun PictureIcon(kind: PictureKind, accent: Color, modifier: Modifier) {
    Canvas(modifier) {
        val w = size.width; val h = size.height
        when (kind) {
            PictureKind.FLOWERS -> {
                drawCircle(Color(0xFFFFD54F), radius = w*.15f, center = Offset(w*.5f,h*.45f))
                listOf(Offset(.5f,.2f), Offset(.25f,.42f), Offset(.75f,.42f), Offset(.35f,.7f), Offset(.65f,.7f)).forEach { p -> drawCircle(accent, w*.15f, Offset(w*p.x,h*p.y)) }
                drawLine(Color(0xFF43A047), Offset(w*.5f,h*.58f), Offset(w*.5f,h), strokeWidth = w*.08f)
            }
            PictureKind.FRUITS -> {
                drawCircle(accent, w*.34f, Offset(w*.5f,h*.55f))
                drawLine(Color(0xFF5D4037), Offset(w*.5f,h*.28f), Offset(w*.58f,h*.15f), strokeWidth = w*.07f)
                drawOval(Color(0xFF43A047), Offset(w*.55f,h*.12f), Size(w*.28f,h*.13f))
            }
            PictureKind.ANIMALS -> {
                drawCircle(Color(0xFFFFCC80), w*.34f, Offset(w*.5f,h*.56f))
                drawCircle(Color(0xFFFFCC80), w*.15f, Offset(w*.26f,h*.28f)); drawCircle(Color(0xFFFFCC80), w*.15f, Offset(w*.74f,h*.28f))
                drawCircle(Color.Black, w*.045f, Offset(w*.42f,h*.52f)); drawCircle(Color.Black, w*.045f, Offset(w*.58f,h*.52f))
                drawCircle(Color(0xFF5D4037), w*.07f, Offset(w*.5f,h*.68f))
            }
            PictureKind.BIKES -> {
                drawCircle(Color.Transparent, w*.22f, Offset(w*.25f,h*.7f), style=Stroke(width=w*.06f)); drawCircle(Color.Transparent, w*.22f, Offset(w*.75f,h*.7f), style=Stroke(width=w*.06f))
                drawLine(accent, Offset(w*.25f,h*.7f), Offset(w*.48f,h*.42f), strokeWidth=w*.06f); drawLine(accent, Offset(w*.48f,h*.42f), Offset(w*.75f,h*.7f), strokeWidth=w*.06f); drawLine(accent, Offset(w*.25f,h*.7f), Offset(w*.75f,h*.7f), strokeWidth=w*.06f); drawLine(accent, Offset(w*.48f,h*.42f), Offset(w*.38f,h*.28f), strokeWidth=w*.06f); drawLine(accent, Offset(w*.38f,h*.28f), Offset(w*.52f,h*.28f), strokeWidth=w*.06f)
            }
        }
    }
}

@Composable
private fun NumberNavigationButton(text: String, enabled: Boolean, color: Color, modifier: Modifier, onClick: () -> Unit) {
    val scale by animateFloatAsState(if (enabled) 1f else .97f, spring(), label = "nav_$text")
    Box(
        modifier.scale(scale).shadow(if (enabled) 7.dp else 2.dp, RoundedCornerShape(18.dp))
            .background(if (enabled) color else color.copy(alpha = .28f), RoundedCornerShape(18.dp))
            .border(2.dp, if (enabled) color else color.copy(alpha = .2f), RoundedCornerShape(18.dp))
            .clickable(enabled = enabled, onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        Text(text, fontSize = 20.sp, fontWeight = FontWeight.Black, color = if (enabled) Color.White else Color.Gray)
    }
}

@Composable
private fun NumberModeButton(text: String, selected: Boolean, modifier: Modifier, onClick: () -> Unit) {
    Box(modifier.height(52.dp).shadow(if (selected) 8.dp else 3.dp, RoundedCornerShape(17.dp)).background(if (selected) Color(0xFF039BE5) else Color(0xFFEAF8FF), RoundedCornerShape(17.dp)).clickable(onClick = onClick), contentAlignment = Alignment.Center) {
        Text(text, fontSize = 18.sp, fontWeight = FontWeight.ExtraBold, color = if (selected) Color.White else Color(0xFF075B86), textAlign = TextAlign.Center)
    }
}

@Composable
private fun OperationButton(text: String, selected: Boolean, modifier: Modifier, color: Color, onClick: () -> Unit) {
    val scale by animateFloatAsState(if (selected) 1.04f else 1f, spring(), label = text)
    Box(modifier.height(56.dp).scale(scale).shadow(if (selected) 9.dp else 4.dp, RoundedCornerShape(18.dp)).background(if (selected) color else MaterialTheme.colorScheme.surface, RoundedCornerShape(18.dp)).border(2.dp, color, RoundedCornerShape(18.dp)).clickable(onClick = onClick), contentAlignment = Alignment.Center) {
        Text(text, fontSize = 20.sp, fontWeight = FontWeight.Black, color = if (selected) Color.White else color)
    }
}

@Composable
private fun Number3DButton(text: String, color: Color, modifier: Modifier, onClick: () -> Unit) {
    var pressed by remember { mutableStateOf(false) }
    val scale by animateFloatAsState(if (pressed) .94f else 1f, spring(dampingRatio=.55f, stiffness=650f), label=text)
    Box(modifier.scale(scale).shadow(8.dp, RoundedCornerShape(18.dp)).background(Brush.verticalGradient(listOf(color.copy(.92f), color.copy(.68f))), RoundedCornerShape(18.dp)).border(2.dp, Color.White.copy(.7f), RoundedCornerShape(18.dp)).clickable { pressed=true; onClick(); pressed=false }.padding(horizontal=16.dp, vertical=12.dp), contentAlignment=Alignment.Center) {
        Text(text, fontSize=18.sp, fontWeight=FontWeight.Black, color=Color.White)
    }
}

private fun arabicDigits(n: Int): String = n.toString().map { c -> if (c in '0'..'9') ('٠'.code + (c - '0')).toChar() else c }.joinToString("")

private fun numberWords(n: Int): String {
    val ones = listOf("صفر","واحد","اثنان","ثلاثة","أربعة","خمسة","ستة","سبعة","ثمانية","تسعة")
    val teens = listOf("عشرة","أحد عشر","اثنا عشر","ثلاثة عشر","أربعة عشر","خمسة عشر","ستة عشر","سبعة عشر","ثمانية عشر","تسعة عشر")
    val tens = listOf("","","عشرون","ثلاثون","أربعون","خمسون","ستون","سبعون","ثمانون","تسعون")
    return when {
        n < 10 -> ones[n]
        n < 20 -> teens[n-10]
        n == 100 -> "مئة"
        n % 10 == 0 -> tens[n/10]
        else -> "${ones[n%10]} و${tens[n/10]}"
    }
}

private fun numberColor(n: Int): Color = when (n % 6) {
    0 -> Color(0xFF7E57C2); 1 -> Color(0xFF039BE5); 2 -> Color(0xFF43A047); 3 -> Color(0xFFFF8F00); 4 -> Color(0xFFEC407A); else -> Color(0xFF00897B)
}
