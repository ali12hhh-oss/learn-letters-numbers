package com.learnlettersnumbers.app

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
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
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

internal enum class LetterMode { FORMS, VOWELS }
internal enum class ArabicLetterForm { INITIAL, MEDIAL, FINAL }
internal enum class Vowel { FATHA, DAMMA, KASRA }

internal data class ArabicLetter(
    val letter: String,
    val initial: String,
    val medial: String,
    val final: String,
    val item: String,
    val itemType: String,
    val color: Color
)

internal val arabicLetters = listOf(
    ArabicLetter("ا", "ا", "ـا", "ـا", "أسد", "حيوان", Color(0xFFFF7043)),
    ArabicLetter("ب", "بـ", "ـبـ", "ـب", "بطة", "حيوان", Color(0xFF42A5F5)),
    ArabicLetter("ت", "تـ", "ـتـ", "ـت", "تفاحة", "فاكهة", Color(0xFFEF5350)),
    ArabicLetter("ث", "ثـ", "ـثـ", "ـث", "ثعلب", "حيوان", Color(0xFFFFA726)),
    ArabicLetter("ج", "جـ", "ـجـ", "ـج", "جمل", "حيوان", Color(0xFFAB47BC)),
    ArabicLetter("ح", "حـ", "ـحـ", "ـح", "حصان", "حيوان", Color(0xFF26A69A)),
    ArabicLetter("خ", "خـ", "ـخـ", "ـخ", "خوخ", "فاكهة", Color(0xFFFFCA28)),
    ArabicLetter("د", "د", "ـد", "ـد", "دب", "حيوان", Color(0xFF8D6E63)),
    ArabicLetter("ذ", "ذ", "ـذ", "ـذ", "ذئب", "حيوان", Color(0xFF78909C)),
    ArabicLetter("ر", "ر", "ـر", "ـر", "رمان", "فاكهة", Color(0xFFE53935)),
    ArabicLetter("ز", "ز", "ـز", "ـز", "زرافة", "حيوان", Color(0xFFFFD54F)),
    ArabicLetter("س", "سـ", "ـسـ", "ـس", "سمكة", "حيوان", Color(0xFF29B6F6)),
    ArabicLetter("ش", "شـ", "ـشـ", "ـش", "شمام", "فاكهة", Color(0xFF66BB6A)),
    ArabicLetter("ص", "صـ", "ـصـ", "ـص", "صقر", "حيوان", Color(0xFF5C6BC0)),
    ArabicLetter("ض", "ضـ", "ـضـ", "ـض", "ضفدع", "حيوان", Color(0xFF7CB342)),
    ArabicLetter("ط", "طـ", "ـطـ", "ـط", "طاووس", "حيوان", Color(0xFF26C6DA)),
    ArabicLetter("ظ", "ظـ", "ـظـ", "ـظ", "ظبي", "حيوان", Color(0xFFFF8A65)),
    ArabicLetter("ع", "عـ", "ـعـ", "ـع", "عنب", "فاكهة", Color(0xFF8E24AA)),
    ArabicLetter("غ", "غـ", "ـغـ", "ـغ", "غزال", "حيوان", Color(0xFF66BB6A)),
    ArabicLetter("ف", "فـ", "ـفـ", "ـف", "فراولة", "فاكهة", Color(0xFFEC407A)),
    ArabicLetter("ق", "قـ", "ـقـ", "ـق", "قرد", "حيوان", Color(0xFFFFB300)),
    ArabicLetter("ك", "كـ", "ـكـ", "ـك", "كمثرى", "فاكهة", Color(0xFF9CCC65)),
    ArabicLetter("ل", "لـ", "ـلـ", "ـل", "ليمون", "فاكهة", Color(0xFFD4E157)),
    ArabicLetter("م", "مـ", "ـمـ", "ـم", "موز", "فاكهة", Color(0xFFFFC107)),
    ArabicLetter("ن", "نـ", "ـنـ", "ـن", "نحلة", "حيوان", Color(0xFFFFD740)),
    ArabicLetter("ه", "هـ", "ـهـ", "ـه", "هدهد", "حيوان", Color(0xFF7E57C2)),
    ArabicLetter("و", "و", "ـو", "ـو", "وردة", "نبات", Color(0xFFEC407A)),
    ArabicLetter("ي", "يـ", "ـيـ", "ـي", "يد", "شيء", Color(0xFF42A5F5))
)

@Composable
internal fun LettersScreen(
    audio: LocalAudioManager,
    onBack: () -> Unit,
    onTap: () -> Unit,
    soundsEnabled: () -> Boolean = { true }
) {
    var mode by remember { mutableStateOf(LetterMode.FORMS) }
    var index by remember { mutableIntStateOf(0) }
    var form by remember { mutableStateOf(ArabicLetterForm.INITIAL) }
    var vowel by remember { mutableStateOf(Vowel.FATHA) }
    val current = arabicLetters[index]

    fun playCurrent() {
        if (!soundsEnabled()) return
        if (mode == LetterMode.FORMS) playArabicLetterSound(audio, index + 1)
        else playArabicVowelSound(audio, index + 1, vowel)
    }

    LaunchedEffect(index, mode, form, vowel) { playCurrent() }

    CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Rtl) {
        Column(
            modifier = Modifier.fillMaxSize().background(MaterialTheme.colorScheme.background).padding(14.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                Button(onClick = { onBack(); onTap() }, shape = RoundedCornerShape(18.dp)) {
                    Text("رجوع", fontWeight = FontWeight.Bold)
                }
                Spacer(Modifier.weight(1f))
                Text("الحروف العربية", fontSize = 27.sp, fontWeight = FontWeight.ExtraBold, color = Color(0xFF114B8C))
            }

            Spacer(Modifier.height(10.dp))
            Row(
                Modifier.fillMaxWidth().background(MaterialTheme.colorScheme.surface.copy(alpha = .9f), RoundedCornerShape(20.dp)).padding(6.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                ModeButton("الحروف", mode == LetterMode.FORMS, Modifier.weight(1f)) {
                    mode = LetterMode.FORMS
                    form = ArabicLetterForm.INITIAL
                    onTap()
                }
                ModeButton("الحركات", mode == LetterMode.VOWELS, Modifier.weight(1f)) {
                    mode = LetterMode.VOWELS
                    vowel = Vowel.FATHA
                    onTap()
                }
            }

            Spacer(Modifier.height(8.dp))
            Text(
                if (mode == LetterMode.FORMS) "اختر شكل الحرف ثم استخدم السابق والتالي 🌟" else "جرّب الفتحة والضمة والكسرة 🎈",
                modifier = Modifier.background(Color(0xFFFFF3B0), RoundedCornerShape(18.dp)).padding(horizontal = 18.dp, vertical = 8.dp),
                color = Color(0xFF7A3E00), fontWeight = FontWeight.Bold, fontSize = 16.sp
            )

            Spacer(Modifier.height(10.dp))
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceEvenly) {
                if (mode == LetterMode.FORMS) {
                    FormButton("أولي", form == ArabicLetterForm.INITIAL) { form = ArabicLetterForm.INITIAL; onTap() }
                    FormButton("وسطي", form == ArabicLetterForm.MEDIAL) { form = ArabicLetterForm.MEDIAL; onTap() }
                    FormButton("أخري", form == ArabicLetterForm.FINAL) { form = ArabicLetterForm.FINAL; onTap() }
                } else {
                    FormButton("فتحة", vowel == Vowel.FATHA) { vowel = Vowel.FATHA; onTap() }
                    FormButton("ضمة", vowel == Vowel.DAMMA) { vowel = Vowel.DAMMA; onTap() }
                    FormButton("كسرة", vowel == Vowel.KASRA) { vowel = Vowel.KASRA; onTap() }
                }
            }

            Spacer(Modifier.height(10.dp))
            Box(
                Modifier.fillMaxWidth().weight(1f)
                    .shadow(14.dp, RoundedCornerShape(30.dp))
                    .background(Brush.verticalGradient(listOf(Color.White, Color(0xFFDDF5FF))), RoundedCornerShape(30.dp))
                    .border(5.dp, current.color, RoundedCornerShape(30.dp))
                    .clickable { playCurrent(); onTap() },
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    val shown = if (mode == LetterMode.FORMS) when (form) {
                        ArabicLetterForm.INITIAL -> current.initial
                        ArabicLetterForm.MEDIAL -> current.medial
                        ArabicLetterForm.FINAL -> current.final
                    } else addVowel(current.letter, vowel)
                    Text(shown, fontSize = 150.sp, fontWeight = FontWeight.ExtraBold, color = current.color)
                    Spacer(Modifier.height(8.dp))
                    Text(if (mode == LetterMode.FORMS) "صوت الحرف" else vowelName(vowel), fontSize = 21.sp, fontWeight = FontWeight.Bold, color = Color(0xFF155E8A))
                    Spacer(Modifier.height(10.dp))
                    OutlinedButton(
                        onClick = { if (soundsEnabled()) audio.playRequired("ar_letter_%02d_name".format(index + 1)); onTap() },
                        shape = RoundedCornerShape(14.dp)
                    ) { Text("اسم الحرف", fontWeight = FontWeight.Bold) }
                }
            }

            Spacer(Modifier.height(10.dp))
            Row(Modifier.fillMaxWidth().height(64.dp), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                NavigationButton("التالي ▶", index < arabicLetters.lastIndex) {
                    if (index < arabicLetters.lastIndex) { index++; onTap() }
                }
                NavigationButton("◀ السابق", index > 0) {
                    if (index > 0) { index--; onTap() }
                }
            }
        }
    }
}

@Composable private fun NavigationButton(text: String, enabled: Boolean, onClick: () -> Unit) {
    Button(
        onClick = onClick,
        enabled = enabled,
        modifier = Modifier.weight(1f).fillMaxHeight(),
        shape = RoundedCornerShape(18.dp),
        elevation = ButtonDefaults.buttonElevation(defaultElevation = 6.dp, pressedElevation = 2.dp)
    ) { Text(text, fontSize = 19.sp, fontWeight = FontWeight.ExtraBold) }
}

@Composable private fun ModeButton(text: String, selected: Boolean, modifier: Modifier, onClick: () -> Unit) {
    Box(
        modifier.height(52.dp).shadow(if (selected) 8.dp else 3.dp, RoundedCornerShape(16.dp))
            .background(if (selected) Color(0xFF26A69A) else Color(0xFFEAF5FF), RoundedCornerShape(16.dp))
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center
    ) { Text(text, fontSize = 19.sp, fontWeight = FontWeight.ExtraBold, color = if (selected) Color.White else Color(0xFF165A7D)) }
}

@Composable private fun FormButton(text: String, selected: Boolean, onClick: () -> Unit) {
    val scale by animateFloatAsState(if (selected) 1.04f else 1f, spring(), label = text)
    Box(
        Modifier.width(105.dp).height(50.dp).scale(scale).shadow(7.dp, RoundedCornerShape(17.dp))
            .background(if (selected) Color(0xFFFFA726) else MaterialTheme.colorScheme.surface, RoundedCornerShape(17.dp))
            .border(2.dp, Color(0xFFFFD54F), RoundedCornerShape(17.dp)).clickable(onClick = onClick),
        contentAlignment = Alignment.Center
    ) { Text(text, fontSize = 18.sp, fontWeight = FontWeight.ExtraBold, color = Color(0xFF6D3500)) }
}

private fun addVowel(letter: String, vowel: Vowel): String = when (vowel) {
    Vowel.FATHA -> "$letterَ"
    Vowel.DAMMA -> "$letterُ"
    Vowel.KASRA -> "$letterِ"
}

private fun vowelName(v: Vowel): String = when (v) {
    Vowel.FATHA -> "الفتحة"
    Vowel.DAMMA -> "الضمة"
    Vowel.KASRA -> "الكسرة"
}

private fun playArabicLetterSound(audio: LocalAudioManager, index: Int) {
    audio.playRequired("ar_letter_%02d_sound".format(index))
}

private fun playArabicVowelSound(audio: LocalAudioManager, index: Int, vowel: Vowel) {
    val suffix = when (vowel) { Vowel.FATHA -> 1; Vowel.DAMMA -> 2; Vowel.KASRA -> 3 }
    audio.playRequired("ar_letter_%02d_vowel_%d".format(index, suffix))
}
