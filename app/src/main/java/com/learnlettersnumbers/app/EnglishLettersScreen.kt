package com.learnlettersnumbers.app

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.platform.LocalLayoutDirection

private enum class EnglishCase { LOWER, UPPER }

private val englishLetters = ('A'..'Z').toList()
private val letterColors = listOf(
    0xFFEF5350,0xFF42A5F5,0xFF66BB6A,0xFFFFA726,0xFFAB47BC,0xFF26A69A,
    0xFFEC407A,0xFF7E57C2,0xFF29B6F6,0xFFFF7043,0xFF5C6BC0,0xFF9CCC65,
    0xFFFFCA28,0xFF26C6DA,0xFF8D6E63,0xFFEC407A,0xFFFFB300,0xFF42A5F5,
    0xFF66BB6A,0xFFFF8A65,0xFF7E57C2,0xFF29B6F6,0xFFAB47BC,0xFF26A69A,
    0xFFFFA726,0xFF5C6BC0
).map { Color(it) }

// Friendly, colorful emoji illustrations for children.
private val letterEmojis = listOf(
    "🍎", "⚽", "🐱", "🐶", "🐘", "🐟", "🦒", "🍦", "🍨", "🐸", "🦁", "🌙", "🐭",
    "🍊", "🐼", "👑", "🤖", "🌈", "☀️", "🐯", "☂️", "🚐", "🐳", "🎄", "🎁", "🦓"
)

private val letterWords = listOf(
    "Apple — تفاحة", "Ball — كرة", "Cat — قطة", "Dog — كلب", "Elephant — فيل", "Fish — سمكة",
    "Giraffe — زرافة", "Ice cream — مثلجات", "Ice cream — آيس كريم", "Jellyfish — قنديل البحر",
    "Lion — أسد", "Moon — قمر", "Mouse — فأر", "Orange — برتقالة", "Panda — باندا", "Queen — ملكة",
    "Robot — روبوت", "Rainbow — قوس قزح", "Sun — شمس", "Tiger — نمر", "Umbrella — مظلة",
    "Van — حافلة صغيرة", "Whale — حوت", "Xmas tree — شجرة عيد الميلاد", "Yo-yo — يويو", "Zebra — حمار وحشي"
)

@Composable
internal fun EnglishLettersScreen(
    audio: LocalAudioManager,
    onTap: () -> Unit,
    onBack: () -> Unit,
    onLetterSeen: (Int) -> Unit,
    soundsEnabled: () -> Boolean = { true }
) {
    var letterIndex by remember { mutableIntStateOf(0) }
    var case by remember { mutableStateOf(EnglishCase.UPPER) }
    val letter = englishLetters[letterIndex]
    val shown = if (case == EnglishCase.UPPER) letter.toString() else letter.lowercase()

    LaunchedEffect(letterIndex, case) {
        if (soundsEnabled()) playEnglishLetterSound(audio, letterIndex + 1)
        onLetterSeen(letterIndex)
    }

    CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Ltr) {
        Box(Modifier.fillMaxSize().background(MaterialTheme.colorScheme.background)) {
            Column(
                Modifier.fillMaxSize().padding(14.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Header: English title with Arabic explanation above the controls.
                Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                    Small3DButton("رجوع\nBack", false, Color(0xFF42A5F5), Modifier.width(100.dp)) { onBack(); onTap() }
                    Spacer(Modifier.weight(1f))
                    Column(horizontalAlignment = Alignment.End) {
                        Text("الحروف الإنجليزية", fontSize = 17.sp, fontWeight = FontWeight.Bold, color = Color(0xFF39708F))
                        Text("English Letters", fontSize = 27.sp, fontWeight = FontWeight.ExtraBold, color = Color(0xFF245B8A))
                    }
                }

                Spacer(Modifier.height(8.dp))
                Text(
                    "اختر شكل الحرف / Choose letter case",
                    modifier = Modifier.fillMaxWidth().background(Color(0xFFFFF0B8), RoundedCornerShape(16.dp)).padding(8.dp),
                    textAlign = TextAlign.Center,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = Color(0xFF704400)
                )
                Spacer(Modifier.height(7.dp))

                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    CaseButton("حروف صغيرة\nlowercase", case == EnglishCase.LOWER, Modifier.weight(1f)) { case = EnglishCase.LOWER; onTap() }
                    CaseButton("حروف كبيرة\nUPPERCASE", case == EnglishCase.UPPER, Modifier.weight(1f)) { case = EnglishCase.UPPER; onTap() }
                }

                Spacer(Modifier.height(9.dp))
                Text(
                    "ممتاز! اضغط على الحرف واستمع إلى صوته\nGreat job! Tap the letter and listen to its sound 🌟",
                    modifier = Modifier.background(Color(0xFFDDF6FF), RoundedCornerShape(18.dp)).padding(horizontal = 14.dp, vertical = 8.dp),
                    color = Color(0xFF14577D),
                    fontWeight = FontWeight.Bold,
                    fontSize = 15.sp,
                    textAlign = TextAlign.Center
                )

                Spacer(Modifier.height(8.dp))

                // Main display only: no letter grid at the top.
                Box(
                    Modifier.fillMaxWidth().weight(1f)
                        .shadow(12.dp, RoundedCornerShape(30.dp))
                        .background(Brush.verticalGradient(listOf(Color.White, Color(0xFFE7F7FF))), RoundedCornerShape(30.dp))
                        .border(5.dp, letterColors[letterIndex], RoundedCornerShape(30.dp))
                        .clickable { if (soundsEnabled()) playEnglishLetterSound(audio, letterIndex + 1); onTap() },
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(letterEmojis[letterIndex], fontSize = 58.sp)
                        Spacer(Modifier.height(3.dp))
                        Text(shown, fontSize = 128.sp, fontWeight = FontWeight.ExtraBold, color = letterColors[letterIndex])
                        Text("صوت الحرف  •  Letter sound", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = Color(0xFF245B8A))
                        Spacer(Modifier.height(3.dp))
                        Text(letterWords[letterIndex], fontSize = 17.sp, fontWeight = FontWeight.ExtraBold, color = Color(0xFF6A4A18), textAlign = TextAlign.Center)
                        Spacer(Modifier.height(8.dp))
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            Small3DButton("🔊\nصوت الحرف\nLetter sound", false, Color(0xFF66BB6A), Modifier.weight(1f)) { if (soundsEnabled()) playEnglishLetterSound(audio, letterIndex + 1); onTap() }
                            Small3DButton("🔤\nاسم الحرف\nLetter name", false, Color(0xFFFFA726), Modifier.weight(1f)) { if (soundsEnabled()) playEnglishLetterName(audio, letterIndex + 1); onTap() }
                        }
                    }
                }

                Spacer(Modifier.height(10.dp))
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    Small3DButton(
                        "السابق\nPrevious", letterIndex == 0, Color(0xFFAB47BC), Modifier.weight(1f)
                    ) { if (letterIndex > 0) letterIndex--; onTap() }
                    Small3DButton(
                        "التالي\nNext", letterIndex == 25, Color(0xFFEF5350), Modifier.weight(1f)
                    ) { if (letterIndex < 25) letterIndex++; onTap() }
                }
            }
        }
    }
}

@Composable
private fun CaseButton(text: String, selected: Boolean, modifier: Modifier, onClick: () -> Unit) {
    Box(
        modifier.height(58.dp).shadow(if (selected) 9.dp else 3.dp, RoundedCornerShape(17.dp))
            .background(if (selected) Color(0xFF42A5F5) else MaterialTheme.colorScheme.surface, RoundedCornerShape(17.dp))
            .border(2.dp, Color(0xFF90CAF9), RoundedCornerShape(17.dp)).clickable(onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        Text(text, fontSize = 17.sp, fontWeight = FontWeight.ExtraBold, color = if (selected) Color.White else Color(0xFF245B8A), textAlign = TextAlign.Center)
    }
}

@Composable
private fun Small3DButton(text: String, disabled: Boolean, color: Color, modifier: Modifier = Modifier, onClick: () -> Unit) {
    var pressed by remember { mutableStateOf(false) }
    val scale by animateFloatAsState(if (pressed) .94f else 1f, spring(dampingRatio = .55f, stiffness = 650f), label = text)
    Box(
        modifier.height(54.dp).scale(scale).shadow(if (disabled) 2.dp else 7.dp, RoundedCornerShape(16.dp))
            .background(if (disabled) Color(0xFFE8EEF2) else color, RoundedCornerShape(16.dp))
            .border(2.dp, Color.White.copy(alpha = .75f), RoundedCornerShape(16.dp))
            .clickable(enabled = !disabled) { pressed = true; onClick(); pressed = false }
            .padding(horizontal = 10.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(text, fontSize = 15.sp, lineHeight = 18.sp, fontWeight = FontWeight.ExtraBold, color = if (disabled) Color.Gray else Color.White, textAlign = TextAlign.Center)
    }
}

private fun playEnglishLetterSound(audio: LocalAudioManager, index: Int) {
    audio.playRequired("en_letter_%02d_sound".format(index))
}

private fun playEnglishLetterName(audio: LocalAudioManager, index: Int) {
    audio.playRequired("en_letter_%02d_name".format(index))
}
