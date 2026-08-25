package com.learnlettersnumbers.app

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.consumePositionChange
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.runtime.CompositionLocalProvider

private enum class ReadingMode { LETTERS, NUMBERS, WORDS }
private enum class ReadingLetterForm { INITIAL, MEDIAL, FINAL }
private data class StrokeLine(val points: List<Offset>, val color: Color, val width: Float)

private val readingArabicLetters = listOf(
    "ا", "ب", "ت", "ث", "ج", "ح", "خ", "د", "ذ", "ر", "ز", "س", "ش", "ص", "ض", "ط", "ظ", "ع", "غ", "ف", "ق", "ك", "ل", "م", "ن", "ه", "و", "ي"
)

private val initialForms = listOf("ا","بـ","تـ","ثـ","جـ","حـ","خـ","د","ذ","ر","ز","سـ","شـ","صـ","ضـ","طـ","ظـ","عـ","غـ","فـ","قـ","كـ","لـ","مـ","نـ","هـ","و","يـ")
private val medialForms = listOf("ـا","ـبـ","ـتـ","ـثـ","ـجـ","ـحـ","ـخـ","د","ذ","ر","ز","ـسـ","ـشـ","ـصـ","ـضـ","ـطـ","ـظـ","ـعـ","ـغـ","ـفـ","ـقـ","ـكـ","ـلـ","ـمـ","ـنـ","ـهـ","و","ـيـ")
private val finalForms = listOf("ا","ـب","ـت","ـث","ـج","ـح","ـخ","د","ذ","ر","ز","ـس","ـش","ـص","ـض","ـط","ـظ","ـع","ـغ","ـف","ـق","ـك","ـل","ـم","ـن","ـه","و","ـي")

private val twoLetterQuestions = listOf(
    "دا", "اب", "با", "تا", "ثا", "جا", "حا", "خا", "سا", "شا", "صا", "ضا", "طا", "ظا", "عا", "غا", "فا", "قا", "كا", "لا", "ما", "نا", "ها", "وا", "يا", "دو", "بو", "تو", "مو", "نو", "لي", "مي", "في", "قي", "كي", "لو", "هو", "دي", "ري", "شي", "سو", "شو", "فو", "كو", "من", "هل", "بل", "قد", "لم", "لن", "عن", "في", "من", "رب", "حب", "جد", "خذ", "زر", "سر", "شد", "صد", "عد", "غد"
)

@Composable
internal fun ReadingScreen(
    audio: LocalAudioManager,
    onTap: () -> Unit,
    onBack: () -> Unit,
    soundsEnabled: () -> Boolean = { true }
) {
    var mode by remember { mutableStateOf(ReadingMode.LETTERS) }
    var form by remember { mutableStateOf<ReadingLetterForm>(ReadingLetterForm.INITIAL) }
    var index by remember { mutableIntStateOf(0) }
    var inkColor by remember { mutableStateOf(Color(0xFF3F51B5)) }
    var strokes by remember { mutableStateOf(emptyList<StrokeLine>()) }
    var currentPoints by remember { mutableStateOf(emptyList<Offset>()) }

    val currentTarget = when (mode) {
        ReadingMode.LETTERS -> readingArabicLetters[index]
        ReadingMode.NUMBERS -> arabicDigits(index + 1)
        ReadingMode.WORDS -> twoLetterQuestions[index % twoLetterQuestions.size]
    }

    LaunchedEffect(mode, index, form) {
        val spoken = when (mode) {
            ReadingMode.LETTERS -> "اكتب حرف ${readingArabicLetters[index]}"
            ReadingMode.NUMBERS -> "اكتب الرقم ${numberWords(index + 1)}"
            ReadingMode.WORDS -> "اكتب الحرفين ${twoLetterQuestions[index % twoLetterQuestions.size].toCharArray().joinToString(" مع ") }"
        }
        if (soundsEnabled()) {
            when (mode) {
                ReadingMode.LETTERS -> audio.playRequired("ar_letter_%02d_sound".format(index + 1))
                ReadingMode.NUMBERS -> audio.playRequired("ar_number_%03d".format(index + 1))
                ReadingMode.WORDS -> {
                    val word = twoLetterQuestions[index % twoLetterQuestions.size]
                    val ids: List<Int> = word.mapNotNull { ch ->
                        readingArabicLetters.indexOf(ch.toString()).takeIf { it >= 0 }?.plus(1)
                    }
                    if (ids.size == word.length) {
                        val clips: List<String> = ids.map { id: Int -> "ar_letter_%02d_sound".format(id) }
                        audio.playSequence(clips)
                    }
                }
            }
        }
    }

    fun clearBoard() {
        strokes = emptyList()
        currentPoints = emptyList()
    }
    fun next() {
        index = when (mode) {
            ReadingMode.LETTERS -> (index + 1) % readingArabicLetters.size
            ReadingMode.NUMBERS -> (index + 1) % 100
            ReadingMode.WORDS -> (index + 1) % twoLetterQuestions.size
        }
        clearBoard(); onTap()
    }
    fun previous() {
        val size = when (mode) { ReadingMode.LETTERS -> 28; ReadingMode.NUMBERS -> 100; ReadingMode.WORDS -> twoLetterQuestions.size }
        index = (index - 1 + size) % size
        clearBoard(); onTap()
    }

    CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Rtl) {
        Box(
            Modifier.fillMaxSize().background(Brush.verticalGradient(listOf(MaterialTheme.colorScheme.background, MaterialTheme.colorScheme.surfaceVariant)))
        ) {
            Column(Modifier.fillMaxSize().padding(12.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                    Reading3DButton("رجوع", Color(0xFF7E57C2), Modifier.width(88.dp)) { onBack(); onTap() }
                    Spacer(Modifier.weight(1f))
                    Text("القراءة والكتابة", fontSize = 28.sp, fontWeight = FontWeight.Black, color = Color(0xFF155B83))
                }

                Spacer(Modifier.height(8.dp))
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(7.dp)) {
                    ModeButton("الحروف", mode == ReadingMode.LETTERS, Modifier.weight(1f)) { mode = ReadingMode.LETTERS; index = 0; clearBoard(); onTap() }
                    ModeButton("الأرقام", mode == ReadingMode.NUMBERS, Modifier.weight(1f)) { mode = ReadingMode.NUMBERS; index = 0; clearBoard(); onTap() }
                    ModeButton("كلمات من حرفين", mode == ReadingMode.WORDS, Modifier.weight(1f)) { mode = ReadingMode.WORDS; index = 0; clearBoard(); onTap() }
                }

                Spacer(Modifier.height(7.dp))
                Text(
                    when (mode) {
                        ReadingMode.LETTERS -> "اكتب الحرف الظاهر على السبورة ✏️"
                        ReadingMode.NUMBERS -> "اكتب الرقم الظاهر بالأرقام العربية 🔢"
                        ReadingMode.WORDS -> "اكتب الحرفين فقط كما تسمعهما 🌟"
                    },
                    modifier = Modifier.fillMaxWidth().background(Color(0xFFFFE39A), RoundedCornerShape(18.dp)).border(2.dp, Color(0xFFFFC94A), RoundedCornerShape(18.dp)).padding(8.dp),
                    textAlign = TextAlign.Center, fontSize = 16.sp, fontWeight = FontWeight.ExtraBold, color = Color(0xFF654000)
                )

                Spacer(Modifier.height(7.dp))
                if (mode == ReadingMode.LETTERS) {
                    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(7.dp)) {
                        FormButton("أولي", form == ReadingLetterForm.INITIAL, Modifier.weight(1f)) { form = ReadingLetterForm.INITIAL; onTap() }
                        FormButton("وسطي", form == ReadingLetterForm.MEDIAL, Modifier.weight(1f)) { form = ReadingLetterForm.MEDIAL; onTap() }
                        FormButton("أخري", form == ReadingLetterForm.FINAL, Modifier.weight(1f)) { form = ReadingLetterForm.FINAL; onTap() }
                    }
                    Spacer(Modifier.height(6.dp))
                }

                Box(
                    Modifier.fillMaxWidth().weight(1f).shadow(12.dp, RoundedCornerShape(26.dp)).background(Color(0xFFF7FBFF), RoundedCornerShape(26.dp)).border(5.dp, Color(0xFF5AA7C7), RoundedCornerShape(26.dp))
                ) {
                    Column(Modifier.fillMaxSize().padding(10.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                        Box(Modifier.fillMaxWidth().weight(.36f).background(MaterialTheme.colorScheme.surface, RoundedCornerShape(20.dp)).border(2.dp, Color(0xFFD7E8EF), RoundedCornerShape(20.dp)), contentAlignment = Alignment.Center) {
                            Text(
                                displayTarget(mode, index, form),
                                fontSize = when (mode) { ReadingMode.WORDS -> 55.sp; else -> 72.sp },
                                fontWeight = FontWeight.Black, color = Color(0xFF294C78), textAlign = TextAlign.Center
                            )
                        }
                        Spacer(Modifier.height(7.dp))
                        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(6.dp), verticalAlignment = Alignment.CenterVertically) {
                            ColorButton("أزرق", Color(0xFF3F51B5), inkColor == Color(0xFF3F51B5), Modifier.weight(1f)) { inkColor = Color(0xFF3F51B5); onTap() }
                            ColorButton("أخضر", Color(0xFF2E9B62), inkColor == Color(0xFF2E9B62), Modifier.weight(1f)) { inkColor = Color(0xFF2E9B62); onTap() }
                            ColorButton("وردي", Color(0xFFE64A78), inkColor == Color(0xFFE64A78), Modifier.weight(1f)) { inkColor = Color(0xFFE64A78); onTap() }
                        }
                        Spacer(Modifier.height(6.dp))
                        Canvas(
                            Modifier.fillMaxWidth().weight(.64f).background(MaterialTheme.colorScheme.surface, RoundedCornerShape(18.dp)).border(2.dp, Color(0xFFE2EEF4), RoundedCornerShape(18.dp)).pointerInput(inkColor) {
                                detectDragGestures(
                                    onDragStart = { point -> currentPoints = listOf(point) },
                                    onDrag = { change, _ -> change.consumePositionChange(); currentPoints = currentPoints + change.position },
                                    onDragEnd = { if (currentPoints.size > 1) strokes = strokes + StrokeLine(currentPoints, inkColor, 9f); currentPoints = emptyList() },
                                    onDragCancel = { currentPoints = emptyList() }
                                )
                            }
                        ) {
                            strokes.forEach { stroke -> drawPath(path = pathOf(stroke.points), color = stroke.color, style = Stroke(width = stroke.width, cap = StrokeCap.Round, join = androidx.compose.ui.graphics.StrokeJoin.Round)) }
                            if (currentPoints.size > 1) drawPath(path = pathOf(currentPoints), color = inkColor, style = Stroke(width = 9f, cap = StrokeCap.Round, join = androidx.compose.ui.graphics.StrokeJoin.Round))
                        }
                    }
                }

                Spacer(Modifier.height(7.dp))
                Text(
                    encouragement(mode),
                    modifier = Modifier.background(MaterialTheme.colorScheme.surface.copy(alpha = .9f), RoundedCornerShape(16.dp)).border(2.dp, Color(0xFFBFE1ED), RoundedCornerShape(16.dp)).padding(horizontal = 14.dp, vertical = 6.dp),
                    fontSize = 15.sp, fontWeight = FontWeight.Bold, color = Color(0xFF246078), textAlign = TextAlign.Center
                )
                Spacer(Modifier.height(6.dp))
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(7.dp)) {
                    Reading3DButton("السابق", Color(0xFF5C6BC0), Modifier.weight(1f)) { previous() }
                    Reading3DButton("مسح", Color(0xFFE85D5D), Modifier.weight(1f)) { clearBoard(); onTap() }
                    Reading3DButton("التالي", Color(0xFF2EAD69), Modifier.weight(1f)) { next() }
                    Reading3DButton("🔊 اسمع", Color(0xFF039BE5), Modifier.weight(1f)) {
                        if (soundsEnabled()) {
                            when (mode) {
                                ReadingMode.LETTERS -> audio.playRequired("ar_letter_%02d_sound".format(index + 1))
                                ReadingMode.NUMBERS -> audio.playRequired("ar_number_%03d".format(index + 1))
                                ReadingMode.WORDS -> {
                                    val word = twoLetterQuestions[index % twoLetterQuestions.size]
                                    val ids: List<Int> = word.mapNotNull { ch ->
                                        arabicLetters.indexOfFirst { it.letter == ch.toString() }.takeIf { it >= 0 }?.plus(1)
                                    }
                                    if (ids.size == word.length) {
                                        val clips: List<String> = ids.map { id: Int -> "ar_letter_%02d_sound".format(id) }
                                        audio.playSequence(clips)
                                    }
                                }
                            }
                        }; onTap()
                    }
                }
            }
        }
    }
}

private fun displayTarget(mode: ReadingMode, index: Int, form: ReadingLetterForm): String = when (mode) {
    ReadingMode.LETTERS -> when (form) { ReadingLetterForm.INITIAL -> initialForms[index]; ReadingLetterForm.MEDIAL -> medialForms[index]; ReadingLetterForm.FINAL -> finalForms[index] }
    ReadingMode.NUMBERS -> arabicDigits(index + 1)
    ReadingMode.WORDS -> twoLetterQuestions[index % twoLetterQuestions.size]
}

private fun promptFor(mode: ReadingMode, index: Int): String = when (mode) {
    ReadingMode.LETTERS -> "اكتب حرف ${readingArabicLetters[index]}"
    ReadingMode.NUMBERS -> "اكتب الرقم ${numberWords(index + 1)}"
    ReadingMode.WORDS -> "اكتب الحرفين ${twoLetterQuestions[index % twoLetterQuestions.size].toCharArray().joinToString(" مع ") }"
}

private fun encouragement(mode: ReadingMode): String = when (mode) {
    ReadingMode.LETTERS -> "أحسنت! ركّز على شكل الحرف واكتب بهدوء ⭐"
    ReadingMode.NUMBERS -> "رائع! اكتب الرقم خطوة خطوة 👏"
    ReadingMode.WORDS -> "ممتاز! حرفان فقط، وأنت قادر عليهما 💪"
}

@Composable
private fun ModeButton(title: String, selected: Boolean, modifier: Modifier, onClick: () -> Unit) = Reading3DButton(title, if (selected) Color(0xFF00897B) else Color(0xFF42A5F5), modifier) { onClick() }

@Composable
private fun FormButton(title: String, selected: Boolean, modifier: Modifier, onClick: () -> Unit) = Reading3DButton(title, if (selected) Color(0xFFEF6C00) else Color(0xFFFFB74D), modifier) { onClick() }

@Composable
private fun ColorButton(title: String, color: Color, selected: Boolean, modifier: Modifier, onClick: () -> Unit) = Reading3DButton(title, if (selected) color else color.copy(alpha = .72f), modifier) { onClick() }

@Composable
private fun Reading3DButton(title: String, color: Color, modifier: Modifier, onClick: () -> Unit) {
    var pressed by remember { mutableStateOf(false) }
    val scale by animateFloatAsState(if (pressed) .93f else 1f, spring(dampingRatio = .55f, stiffness = 650f), label = "reading_button")
    Box(modifier.scale(scale).height(48.dp).shadow(7.dp, RoundedCornerShape(15.dp)).background(Brush.verticalGradient(listOf(color.copy(alpha = .78f), color)), RoundedCornerShape(15.dp)).border(2.dp, Color.White.copy(alpha = .65f), RoundedCornerShape(15.dp)).clickable { pressed = true; onClick(); pressed = false }, contentAlignment = Alignment.Center) {
        Text(title, color = Color.White, fontSize = 15.sp, fontWeight = FontWeight.Black, textAlign = TextAlign.Center)
    }
}

private fun pathOf(points: List<Offset>): Path = Path().apply {
    if (points.isEmpty()) return@apply
    moveTo(points.first().x, points.first().y)
    points.drop(1).forEach { lineTo(it.x, it.y) }
}

private fun arabicDigits(n: Int): String = n.toString().map { c -> if (c in '0'..'9') ('٠'.code + (c - '0')).toChar() else c }.joinToString("")

private fun numberWords(n: Int): String = when (n) {
    1 -> "واحد"; 2 -> "اثنان"; 3 -> "ثلاثة"; 4 -> "أربعة"; 5 -> "خمسة"; 6 -> "ستة"; 7 -> "سبعة"; 8 -> "ثمانية"; 9 -> "تسعة"; 10 -> "عشرة";
    else -> n.toString()
}
