package com.learnlettersnumbers.app

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

private enum class EnglishCase { LOWER, UPPER }

private val englishLetters = ('A'..'Z').toList()
private val letterColors = listOf(
    0xFFEF5350,0xFF42A5F5,0xFF66BB6A,0xFFFFA726,0xFFAB47BC,0xFF26A69A,
    0xFFEC407A,0xFF7E57C2,0xFF29B6F6,0xFFFF7043,0xFF5C6BC0,0xFF9CCC65,
    0xFFFFCA28,0xFF26C6DA,0xFF8D6E63,0xFFEC407A,0xFFFFB300,0xFF42A5F5,
    0xFF66BB6A,0xFFFF8A65,0xFF7E57C2,0xFF29B6F6,0xFFAB47BC,0xFF26A69A,
    0xFFFFA726,0xFF5C6BC0
).map { Color(it) }

@Composable
internal fun EnglishLettersScreen(audio: LocalAudioManager, onTap: () -> Unit, onBack: () -> Unit, onLetterSeen: (Int) -> Unit, soundsEnabled: () -> Boolean = { true }) {
    var letterIndex by remember { mutableIntStateOf(0) }
    var case by remember { mutableStateOf(EnglishCase.UPPER) }
    var showMessage by remember { mutableStateOf(true) }
    val letter = englishLetters[letterIndex]
    val shown = if (case == EnglishCase.UPPER) letter.toString() else letter.lowercase()

    LaunchedEffect(letterIndex, case) {
        showMessage = true
        if (soundsEnabled()) playEnglishLetterSound(audio, letterIndex + 1)
        onLetterSeen(letterIndex)
    }

    CompositionLocalProvider(androidx.compose.ui.platform.LocalLayoutDirection provides androidx.compose.ui.unit.LayoutDirection.Ltr) {
        Box(Modifier.fillMaxSize().background(MaterialTheme.colorScheme.background)) {
            Column(Modifier.fillMaxSize().padding(14.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                    Small3DButton("Back", false, Color(0xFF42A5F5)) { onBack(); onTap() }
                    Spacer(Modifier.weight(1f))
                    Text("English Letters", fontSize = 28.sp, fontWeight = FontWeight.ExtraBold, color = Color(0xFF245B8A))
                }
                Spacer(Modifier.height(10.dp))
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    CaseButton("lowercase", case == EnglishCase.LOWER, Modifier.weight(1f)) { case = EnglishCase.LOWER; onTap() }
                    CaseButton("UPPERCASE", case == EnglishCase.UPPER, Modifier.weight(1f)) { case = EnglishCase.UPPER; onTap() }
                }
                Spacer(Modifier.height(8.dp))
                if (showMessage) {
                    Text("Great job! Tap a letter and listen carefully! 🌟", modifier = Modifier
                        .background(Color(0xFFDDF6FF), RoundedCornerShape(18.dp))
                        .padding(horizontal = 16.dp, vertical = 9.dp), color = Color(0xFF14577D), fontWeight = FontWeight.Bold, fontSize = 16.sp, textAlign = TextAlign.Center)
                }
                Spacer(Modifier.height(8.dp))
                LazyVerticalGrid(columns = GridCells.Fixed(6), modifier = Modifier.fillMaxWidth().height(150.dp), horizontalArrangement = Arrangement.spacedBy(7.dp), verticalArrangement = Arrangement.spacedBy(7.dp)) {
                    itemsIndexed(englishLetters) { i, l ->
                        EnglishLetterTile(l, i == letterIndex, letterColors[i]) { letterIndex = i; onTap() }
                    }
                }
                Spacer(Modifier.height(10.dp))
                Box(Modifier.fillMaxWidth().weight(1f).shadow(12.dp, RoundedCornerShape(30.dp))
                    .background(Brush.verticalGradient(listOf(Color.White, Color(0xFFE7F7FF))), RoundedCornerShape(30.dp))
                    .border(5.dp, letterColors[letterIndex], RoundedCornerShape(30.dp))
                    .clickable { if (soundsEnabled()) playEnglishLetterSound(audio, letterIndex + 1); onTap() }, contentAlignment = Alignment.Center) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(shown, fontSize = 126.sp, fontWeight = FontWeight.ExtraBold, color = letterColors[letterIndex])
                        Text("Letter sound", fontSize = 19.sp, fontWeight = FontWeight.Bold, color = Color(0xFF245B8A))
                        Spacer(Modifier.height(8.dp))
                        Small3DButton("🔊 Letter sound", false, Color(0xFF66BB6A)) { if (soundsEnabled()) playEnglishLetterSound(audio, letterIndex + 1); onTap() }
                        Spacer(Modifier.height(7.dp))
                        Small3DButton("🔤 Letter name", false, Color(0xFFFFA726)) { if (soundsEnabled()) playEnglishLetterName(audio, letterIndex + 1); onTap() }
                    }
                }
                Spacer(Modifier.height(10.dp))
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    Small3DButton("Previous", letterIndex == 0, Color(0xFFAB47BC), Modifier.weight(1f)) { if(letterIndex > 0) letterIndex--; onTap() }
                    Small3DButton("Next", letterIndex == 25, Color(0xFFEF5350), Modifier.weight(1f)) { if(letterIndex < 25) letterIndex++; onTap() }
                }
            }
        }
    }
}

@Composable private fun EnglishLetterTile(letter: Char, selected: Boolean, color: Color, onClick: () -> Unit) {
    val scale by animateFloatAsState(if(selected) 1.08f else 1f, spring(), label = "tile_$letter")
    Box(Modifier.size(48.dp).scale(scale).shadow(if(selected) 8.dp else 2.dp, RoundedCornerShape(14.dp))
        .background(if(selected) color else MaterialTheme.colorScheme.surface, RoundedCornerShape(14.dp))
        .border(2.dp, color, RoundedCornerShape(14.dp)).clickable(onClick = onClick), contentAlignment = Alignment.Center) {
        Text(letter.toString(), fontSize = 22.sp, fontWeight = FontWeight.ExtraBold, color = if(selected) Color.White else color)
    }
}

@Composable private fun CaseButton(text: String, selected: Boolean, modifier: Modifier, onClick: () -> Unit) {
    Box(modifier.height(54.dp).shadow(if(selected) 9.dp else 3.dp, RoundedCornerShape(17.dp))
        .background(if(selected) Color(0xFF42A5F5) else MaterialTheme.colorScheme.surface, RoundedCornerShape(17.dp))
        .border(2.dp, Color(0xFF90CAF9), RoundedCornerShape(17.dp)).clickable(onClick = onClick), contentAlignment = Alignment.Center) {
        Text(text, fontSize = 18.sp, fontWeight = FontWeight.ExtraBold, color = if(selected) Color.White else Color(0xFF245B8A))
    }
}

@Composable private fun Small3DButton(text: String, disabled: Boolean, color: Color, modifier: Modifier = Modifier, onClick: () -> Unit) {
    Box(modifier.height(48.dp).shadow(if(disabled) 2.dp else 7.dp, RoundedCornerShape(16.dp))
        .background(if(disabled) Color(0xFFE8EEF2) else color, RoundedCornerShape(16.dp))
        .border(2.dp, Color.White.copy(alpha=.75f), RoundedCornerShape(16.dp)).clickable(enabled=!disabled, onClick=onClick), contentAlignment = Alignment.Center) {
        Text(text, fontSize = 16.sp, fontWeight = FontWeight.ExtraBold, color = if(disabled) Color.Gray else MaterialTheme.colorScheme.surface, modifier = Modifier.padding(horizontal = 12.dp))
    }
}

private fun playEnglishLetterSound(audio: LocalAudioManager, index: Int) {
    audio.playRequired("en_letter_%02d_sound".format(index))
}

private fun playEnglishLetterName(audio: LocalAudioManager, index: Int) {
    audio.playRequired("en_letter_%02d_name".format(index))
}
