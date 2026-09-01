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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.platform.LocalLayoutDirection

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
    var showIsolatedLetters by remember { mutableStateOf(false) }
    val current = arabicLetters[index]

    LaunchedEffect(index, mode, form, vowel) {
        if (soundsEnabled()) {
            if (mode == LetterMode.FORMS) playArabicLetterSound(audio, index + 1)
            else playArabicVowelSound(audio, index + 1, vowel)
        }
    }

    CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Rtl) {
        Box(Modifier.fillMaxSize().background(MaterialTheme.colorScheme.background)) {
            Column(Modifier.fillMaxSize().padding(14.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                    Button(onClick = { onBack(); onTap() }, shape = RoundedCornerShape(18.dp)) { Text("رجوع") }
                    Spacer(Modifier.weight(1f))
                    Text("الحروف العربية", fontSize = 28.sp, fontWeight = FontWeight.ExtraBold, color = Color(0xFF114B8C))
                }
                Spacer(Modifier.height(10.dp))
                Row(Modifier.fillMaxWidth().background(MaterialTheme.colorScheme.surface.copy(alpha = .85f), RoundedCornerShape(20.dp)).padding(6.dp), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    ModeButton("الحروف", mode == LetterMode.FORMS, Modifier.weight(1f)) { mode = LetterMode.FORMS; form = ArabicLetterForm.INITIAL; showIsolatedLetters = false; onTap() }
                    ModeButton("الحركات", mode == LetterMode.VOWELS, Modifier.weight(1f)) { mode = LetterMode.VOWELS; vowel = Vowel.FATHA; showIsolatedLetters = false; onTap() }
                }
                Spacer(Modifier.height(8.dp))
                Text(
                    if (mode == LetterMode.FORMS) "رائع! اختر شكل الحرف وتعلّم صوته 🌟" else "أحسنت! جرّب الفتحة والضمة والكسرة 🎈",
                    modifier = Modifier.background(Color(0xFFFFF3B0), RoundedCornerShape(18.dp)).padding(horizontal = 18.dp, vertical = 8.dp),
                    color = Color(0xFF7A3E00), fontWeight = FontWeight.Bold, fontSize = 17.sp, textAlign = TextAlign.Center
                )
                Spacer(Modifier.height(8.dp))
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(7.dp)) {
                    if (mode == LetterMode.FORMS) {
                        FormButton("الحروف", showIsolatedLetters, Modifier.weight(1f)) { index = 0; showIsolatedLetters = true; onTap() }
                        FormButton("أولي", !showIsolatedLetters && form == ArabicLetterForm.INITIAL, Modifier.weight(1f)) { showIsolatedLetters = false; form = ArabicLetterForm.INITIAL; onTap() }
                        FormButton("وسطي", !showIsolatedLetters && form == ArabicLetterForm.MEDIAL, Modifier.weight(1f)) { showIsolatedLetters = false; form = ArabicLetterForm.MEDIAL; onTap() }
                        FormButton("أخري", !showIsolatedLetters && form == ArabicLetterForm.FINAL, Modifier.weight(1f)) { showIsolatedLetters = false; form = ArabicLetterForm.FINAL; onTap() }
                    } else {
                        FormButton("فتحة", vowel == Vowel.FATHA, Modifier.weight(1f)) { vowel = Vowel.FATHA; onTap() }
                        FormButton("ضمة", vowel == Vowel.DAMMA, Modifier.weight(1f)) { vowel = Vowel.DAMMA; onTap() }
                        FormButton("كسرة", vowel == Vowel.KASRA, Modifier.weight(1f)) { vowel = Vowel.KASRA; onTap() }
                    }
                }
                Spacer(Modifier.height(7.dp))
                Box(Modifier.fillMaxWidth().weight(1f).shadow(12.dp, RoundedCornerShape(30.dp)).background(Brush.verticalGradient(listOf(Color.White, Color(0xFFDDF5FF))), RoundedCornerShape(30.dp)).border(4.dp, current.color, RoundedCornerShape(30.dp)).clickable {
                    if (soundsEnabled()) { if (mode == LetterMode.FORMS) playArabicLetterSound(audio, index + 1) else playArabicVowelSound(audio, index + 1, vowel) }
                    onTap()
                }, contentAlignment = Alignment.Center) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        val shown = if (mode == LetterMode.FORMS) {
                            if (showIsolatedLetters) current.letter else when(form) {
                                ArabicLetterForm.INITIAL -> current.initial
                                ArabicLetterForm.MEDIAL -> current.medial
                                ArabicLetterForm.FINAL -> current.final
                            }
                        } else addVowel(current.letter, vowel)
                        Text(shown, fontSize = 108.sp, fontWeight = FontWeight.ExtraBold, color = current.color)
                        Text(if (mode == LetterMode.FORMS) if (showIsolatedLetters) "الحرف" else "صوت الحرف" else vowelName(vowel), fontSize = 20.sp, fontWeight = FontWeight.Bold, color = Color(0xFF155E8A))
                        Spacer(Modifier.height(6.dp))
                        OutlinedButton(onClick = { if (soundsEnabled()) audio.playRequired("ar_letter_%02d_name".format(index + 1)); onTap() }, shape = RoundedCornerShape(14.dp)) { Text("اسم الحرف", fontWeight = FontWeight.Bold) }
                    }
                }
                Spacer(Modifier.height(10.dp))
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(14.dp)) {
                    Button(onClick = { if (index > 0) { index--; onTap() } }, enabled = index > 0, modifier = Modifier.weight(1f).height(58.dp), shape = RoundedCornerShape(18.dp)) { Text("السابق", fontSize = 19.sp, fontWeight = FontWeight.ExtraBold) }
                    Button(onClick = { if (index < arabicLetters.lastIndex) { index++; onTap() } }, enabled = index < arabicLetters.lastIndex, modifier = Modifier.weight(1f).height(58.dp), shape = RoundedCornerShape(18.dp)) { Text("التالي", fontSize = 19.sp, fontWeight = FontWeight.ExtraBold) }
                }
                Spacer(Modifier.height(10.dp))
                Row(Modifier.fillMaxWidth().height(110.dp), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    IllustrationCard(current, Modifier.weight(1f)) { if (soundsEnabled()) audio.speakOffline(current.item, "ar"); onTap() }
                    Text(current.item, modifier = Modifier.weight(1f).clickable { if (soundsEnabled()) audio.speakOffline(current.item, "ar"); onTap() }.background(Color(0xFFFFF7DA), RoundedCornerShape(22.dp)).padding(12.dp), fontSize = 27.sp, fontWeight = FontWeight.ExtraBold, textAlign = TextAlign.Center, color = Color(0xFF7A3E00))
                }
            }
        }
    }
}

@Composable private fun ModeButton(text: String, selected: Boolean, modifier: Modifier, onClick: () -> Unit) {
    Box(modifier.height(52.dp).shadow(if(selected) 8.dp else 3.dp, RoundedCornerShape(16.dp)).background(if(selected) Color(0xFF26A69A) else Color(0xFFEAF5FF), RoundedCornerShape(16.dp)).clickable(onClick = onClick), contentAlignment = Alignment.Center) { Text(text, fontSize = 19.sp, fontWeight = FontWeight.ExtraBold, color = if(selected) Color.White else Color(0xFF165A7D)) }
}

@Composable private fun FormButton(text: String, selected: Boolean, modifier: Modifier, onClick: () -> Unit) {
    val scale by animateFloatAsState(if(selected) 1.04f else 1f, spring(), label = text)
    Box(modifier.height(50.dp).scale(scale).shadow(7.dp, RoundedCornerShape(17.dp)).background(if(selected) Color(0xFFFFA726) else MaterialTheme.colorScheme.surface, RoundedCornerShape(17.dp)).border(2.dp, Color(0xFFFFD54F), RoundedCornerShape(17.dp)).clickable(onClick = onClick), contentAlignment = Alignment.Center) { Text(text, fontSize = 18.sp, fontWeight = FontWeight.ExtraBold, color = Color(0xFF6D3500)) }
}

@Composable private fun IllustrationCard(letter: ArabicLetter, modifier: Modifier, onClick: () -> Unit) {
    Box(modifier.shadow(9.dp, RoundedCornerShape(24.dp)).background(Brush.verticalGradient(listOf(Color.White, letter.color.copy(alpha = .10f))), RoundedCornerShape(24.dp)).border(3.dp, letter.color.copy(alpha = .75f), RoundedCornerShape(24.dp)).clickable(onClick = onClick).padding(6.dp), contentAlignment = Alignment.Center) {
        Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.Center) {
            Text(illustrationEmoji(letter.item), fontSize = 58.sp, textAlign = TextAlign.Center)
            Spacer(Modifier.height(2.dp))
            Text(letter.item, fontSize = 16.sp, fontWeight = FontWeight.ExtraBold, color = Color(0xFF6D3500), textAlign = TextAlign.Center)
        }
    }
}

private fun illustrationEmoji(item: String): String = when (item) {
    "أسد" -> "🦁"; "بطة" -> "🦆"; "تفاحة" -> "🍎"; "ثعلب" -> "🦊"; "جمل" -> "🐪"; "حصان" -> "🐴"; "خوخ" -> "🍑"; "دب" -> "🐻"; "ذئب" -> "🐺"; "رمان" -> "🍎"; "زرافة" -> "🦒"; "سمكة" -> "🐟"; "شمام" -> "🍈"; "صقر" -> "🦅"; "ضفدع" -> "🐸"; "طاووس" -> "🦚"; "ظبي" -> "🦌"; "عنب" -> "🍇"; "غزال" -> "🦌"; "فراولة" -> "🍓"; "قرد" -> "🐒"; "كمثرى" -> "🍐"; "ليمون" -> "🍋"; "موز" -> "🍌"; "نحلة" -> "🐝"; "هدهد" -> "🦉"; "وردة" -> "🌹"; "يد" -> "✋"; else -> "⭐"
}

private fun addVowel(letter: String, vowel: Vowel): String = when(vowel) { Vowel.FATHA -> "$letterَ"; Vowel.DAMMA -> "$letterُ"; Vowel.KASRA -> "$letterِ" }
private fun vowelName(v: Vowel) = when(v) { Vowel.FATHA -> "الفتحة"; Vowel.DAMMA -> "الضمة"; Vowel.KASRA -> "الكسرة" }
private fun playArabicLetterSound(audio: LocalAudioManager, index: Int) { audio.playRequired("ar_letter_%02d_sound".format(index)) }
private fun playArabicVowelSound(audio: LocalAudioManager, index: Int, vowel: Vowel) { val suffix = when (vowel) { Vowel.FATHA -> 1; Vowel.DAMMA -> 2; Vowel.KASRA -> 3 }; audio.playRequired("ar_letter_%02d_vowel_%d".format(index, suffix)) }