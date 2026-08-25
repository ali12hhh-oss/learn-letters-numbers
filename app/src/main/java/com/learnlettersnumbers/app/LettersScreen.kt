package com.learnlettersnumbers.app


import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
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
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
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
    var form by remember { mutableStateOf(ArabicArabicLetterForm.INITIAL) }
    var vowel by remember { mutableStateOf(Vowel.FATHA) }
    var showMessage by remember { mutableStateOf(true) }
    val current = arabicLetters[index]

    LaunchedEffect(index, mode, form, vowel) {
        showMessage = true
        if (soundsEnabled()) {
            if (mode == LetterMode.FORMS) playArabicLetterSound(audio, index + 1)
            else playArabicVowelSound(audio, index + 1, vowel)
        }
    }

    CompositionLocalProvider(LocalLayoutDirection provides androidx.compose.ui.unit.LayoutDirection.Rtl) {
        BoxWithConstraints(Modifier.fillMaxSize().background(MaterialTheme.colorScheme.background)) {
        Column(Modifier.fillMaxSize().padding(14.dp), horizontalAlignment = Alignment.CenterHorizontally) {
            Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                Button(onClick = { onBack(); onTap() }, shape = RoundedCornerShape(18.dp)) { Text("رجوع") }
                Spacer(Modifier.weight(1f))
                Text("الحروف العربية", fontSize = 28.sp, fontWeight = FontWeight.ExtraBold, color = Color(0xFF114B8C))
            }

            Spacer(Modifier.height(10.dp))
            Row(
                modifier = Modifier.fillMaxWidth().background(MaterialTheme.colorScheme.surface.copy(alpha = .85f), RoundedCornerShape(20.dp)).padding(6.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                ModeButton("الحروف", mode == LetterMode.FORMS, Modifier.weight(1f)) { mode = LetterMode.FORMS; form = ArabicArabicLetterForm.INITIAL; onTap() }
                ModeButton("الحركات", mode == LetterMode.VOWELS, Modifier.weight(1f)) { mode = LetterMode.VOWELS; vowel = Vowel.FATHA; onTap() }
            }

            AnimatedVisibility(showMessage) {
                Text(
                    if (mode == LetterMode.FORMS) "رائع! اختر شكل الحرف وتعلّم صوته 🌟" else "أحسنت! جرّب الفتحة والضمة والكسرة 🎈",
                    modifier = Modifier.padding(vertical = 8.dp).background(Color(0xFFFFF3B0), RoundedCornerShape(18.dp)).padding(horizontal = 18.dp, vertical = 8.dp),
                    color = Color(0xFF7A3E00), fontWeight = FontWeight.Bold, fontSize = 17.sp
                )
            }

            LazyVerticalGrid(
                columns = GridCells.Fixed(7),
                modifier = Modifier.fillMaxWidth().height(112.dp),
                horizontalArrangement = Arrangement.spacedBy(6.dp),
                verticalArrangement = Arrangement.spacedBy(5.dp)
            ) {
                items(arabicLetters) { l ->
                    val i = arabicLetters.indexOf(l)
                    Box(Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
                        LetterDot(l.letter, i == index, l.color) { index = i; onTap() }
                    }
                }
            }

            Spacer(Modifier.height(8.dp))
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceEvenly) {
                if (mode == LetterMode.FORMS) {
                    FormButton("أولي", form == ArabicArabicLetterForm.INITIAL) { form = ArabicArabicLetterForm.INITIAL; onTap() }
                    FormButton("وسطي", form == ArabicArabicLetterForm.MEDIAL) { form = ArabicArabicLetterForm.MEDIAL; onTap() }
                    FormButton("أخري", form == ArabicArabicLetterForm.FINAL) { form = ArabicArabicLetterForm.FINAL; onTap() }
                } else {
                    FormButton("فتحة", vowel == Vowel.FATHA) { vowel = Vowel.FATHA; onTap() }
                    FormButton("ضمة", vowel == Vowel.DAMMA) { vowel = Vowel.DAMMA; onTap() }
                    FormButton("كسرة", vowel == Vowel.KASRA) { vowel = Vowel.KASRA; onTap() }
                }
            }

            Spacer(Modifier.height(10.dp))
            Box(
                Modifier.fillMaxWidth().weight(1f).shadow(12.dp, RoundedCornerShape(30.dp)).background(
                    Brush.verticalGradient(listOf(Color.White, Color(0xFFDDF5FF))), RoundedCornerShape(30.dp)
                ).border(4.dp, current.color, RoundedCornerShape(30.dp)).clickable {
                    if (mode == LetterMode.FORMS) playArabicLetterSound(audio, index + 1) else playArabicVowelSound(audio, index + 1, vowel)
                    onTap()
                }, contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    val shown = if (mode == LetterMode.FORMS) when(form) {
                        ArabicArabicLetterForm.INITIAL -> current.initial
                        ArabicArabicLetterForm.MEDIAL -> current.medial
                        ArabicArabicLetterForm.FINAL -> current.final
                    } else addVowel(current.letter, vowel)
                    Text(shown, fontSize = 108.sp, fontWeight = FontWeight.ExtraBold, color = current.color)
                    Text(if (mode == LetterMode.FORMS) "صوت الحرف" else vowelName(vowel), fontSize = 20.sp, fontWeight = FontWeight.Bold, color = Color(0xFF155E8A))
                    Spacer(Modifier.height(6.dp))
                    OutlinedButton(
                        onClick = { if (soundsEnabled()) audio.playRequired("ar_letter_%02d_name".format(index + 1)); onTap() },
                        shape = RoundedCornerShape(14.dp),
                        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 4.dp)
                    ) { Text("اسم الحرف", fontWeight = FontWeight.Bold) }
                }
            }

            Spacer(Modifier.height(10.dp))
            Row(Modifier.fillMaxWidth().height(110.dp), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                IllustrationCard(current, Modifier.weight(1f)) {
                    onTap()
                }
                Text(
                    current.item,
                    modifier = Modifier.weight(1f).clickable {
                            onTap()
                    }.background(Color(0xFFFFF7DA), RoundedCornerShape(22.dp)).padding(12.dp),
                    fontSize = 27.sp, fontWeight = FontWeight.ExtraBold, textAlign = TextAlign.Center, color = Color(0xFF7A3E00)
                )
            }
        }
        }
    }
}

@Composable private fun ModeButton(text: String, selected: Boolean, modifier: Modifier, onClick: () -> Unit) {
    Box(modifier.height(52.dp).shadow(if(selected) 8.dp else 3.dp, RoundedCornerShape(16.dp)).background(if(selected) Color(0xFF26A69A) else Color(0xFFEAF5FF), RoundedCornerShape(16.dp)).clickable(onClick = onClick), contentAlignment = Alignment.Center) {
        Text(text, fontSize = 19.sp, fontWeight = FontWeight.ExtraBold, color = if(selected) Color.White else Color(0xFF165A7D))
    }
}

@Composable private fun FormButton(text: String, selected: Boolean, onClick: () -> Unit) {
    val scale by animateFloatAsState(if(selected) 1.04f else 1f, spring(), label = text)
    Box(Modifier.width(105.dp).height(50.dp).scale(scale).shadow(7.dp, RoundedCornerShape(17.dp)).background(if(selected) Color(0xFFFFA726) else MaterialTheme.colorScheme.surface, RoundedCornerShape(17.dp)).border(2.dp, Color(0xFFFFD54F), RoundedCornerShape(17.dp)).clickable(onClick = onClick), contentAlignment = Alignment.Center) {
        Text(text, fontSize = 18.sp, fontWeight = FontWeight.ExtraBold, color = Color(0xFF6D3500))
    }
}

@Composable private fun LetterDot(letter: String, selected: Boolean, color: Color, onClick: () -> Unit) {
    var pressed by remember { mutableStateOf(false) }
    val scale by animateFloatAsState(if (pressed) .9f else if (selected) 1.08f else 1f, spring(), label = "letter_$letter")
    Box(
        Modifier.size(42.dp).scale(scale).shadow(if(selected) 7.dp else 2.dp, RoundedCornerShape(14.dp))
            .background(if(selected) color else MaterialTheme.colorScheme.surface, RoundedCornerShape(14.dp))
            .border(2.dp, if(selected) Color.White else color.copy(alpha=.55f), RoundedCornerShape(14.dp))
            .clickable { pressed = true; onClick(); pressed = false },
        contentAlignment = Alignment.Center
    ) {
        Text(letter, fontSize = 20.sp, fontWeight = FontWeight.ExtraBold, color = if(selected) Color.White else color)
    }
}

@Composable private fun IllustrationCard(letter: ArabicLetter, modifier: Modifier, onClick: () -> Unit) {
    Box(modifier.shadow(7.dp, RoundedCornerShape(22.dp)).background(MaterialTheme.colorScheme.surface, RoundedCornerShape(22.dp)).border(2.dp, letter.color, RoundedCornerShape(22.dp)).clickable(onClick = onClick), contentAlignment = Alignment.Center) {
        Canvas(Modifier.fillMaxSize().padding(8.dp)) {
            val w = size.width; val h = size.height
            when (letter.itemType) {
                "فاكهة" -> {
                    drawCircle(letter.color, radius = minOf(w,h)*.28f, center = Offset(w*.5f,h*.54f))
                    drawCircle(Color(0xFF4CAF50), radius = minOf(w,h)*.09f, center = Offset(w*.62f,h*.24f))
                    drawLine(Color(0xFF5D4037), Offset(w*.5f,h*.27f), Offset(w*.55f,h*.18f), strokeWidth = 8f)
                }
                "حيوان" -> {
                    drawCircle(Color(0xFFFFCC80), radius = minOf(w,h)*.28f, center = Offset(w*.5f,h*.52f))
                    drawCircle(Color(0xFFFFCC80), radius = minOf(w,h)*.12f, center = Offset(w*.30f,h*.30f))
                    drawCircle(Color(0xFFFFCC80), radius = minOf(w,h)*.12f, center = Offset(w*.70f,h*.30f))
                    drawCircle(Color.Black, radius = 5f, center = Offset(w*.43f,h*.48f)); drawCircle(Color.Black, radius = 5f, center = Offset(w*.57f,h*.48f))
                }
                else -> {
                    drawRoundRect(Color(0xFFFFCC80), topLeft = Offset(w*.28f,h*.25f), size = Size(w*.44f,h*.5f), cornerRadius = androidx.compose.ui.geometry.CornerRadius(18f,18f))
                    drawCircle(Color(0xFF42A5F5), radius = 9f, center = Offset(w*.43f,h*.48f)); drawCircle(Color(0xFF42A5F5), radius = 9f, center = Offset(w*.57f,h*.48f))
                }
            }
        }
    }
}

private fun letterName(letter: String): String = when(letter) {
    "ا" -> "ألف"; "ب" -> "باء"; "ت" -> "تاء"; "ث" -> "ثاء"; "ج" -> "جيم"; "ح" -> "حاء"; "خ" -> "خاء"
    "د" -> "دال"; "ذ" -> "ذال"; "ر" -> "راء"; "ز" -> "زاي"; "س" -> "سين"; "ش" -> "شين"; "ص" -> "صاد"
    "ض" -> "ضاد"; "ط" -> "طاء"; "ظ" -> "ظاء"; "ع" -> "عين"; "غ" -> "غين"; "ف" -> "فاء"; "ق" -> "قاف"
    "ك" -> "كاف"; "ل" -> "لام"; "م" -> "ميم"; "ن" -> "نون"; "ه" -> "هاء"; "و" -> "واو"; "ي" -> "ياء"
    else -> letter
}

private fun addVowel(letter: String, vowel: Vowel): String = when(vowel) {
    Vowel.FATHA -> "$letterَ"
    Vowel.DAMMA -> "$letterُ"
    Vowel.KASRA -> "$letterِ"
}
private fun vowelName(v: Vowel) = when(v) { Vowel.FATHA -> "الفتحة"; Vowel.DAMMA -> "الضمة"; Vowel.KASRA -> "الكسرة" }
private fun playArabicLetterSound(audio: LocalAudioManager, index: Int) {
    audio.playRequired("ar_letter_%02d_sound".format(index))
}

private fun playArabicVowelSound(audio: LocalAudioManager, index: Int, vowel: Vowel) {
    val suffix = when (vowel) { Vowel.FATHA -> 1; Vowel.DAMMA -> 2; Vowel.KASRA -> 3 }
    audio.playRequired("ar_letter_%02d_vowel_%d".format(index, suffix))
}

