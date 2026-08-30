@file:OptIn(androidx.compose.material3.ExperimentalMaterial3Api::class)
package com.learnlettersnumbers.app

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView

private enum class TutorialArabicForm { INITIAL, MEDIAL, FINAL }
private enum class TutorialEnglishCase { UPPER, LOWER }

private val tutorialArabic = listOf("ا","ب","ت","ث","ج","ح","خ","د","ذ","ر","ز","س","ش","ص","ض","ط","ظ","ع","غ","ف","ق","ك","ل","م","ن","ه","و","ي")
private val tutorialArabicNames = listOf("الألف","الباء","التاء","الثاء","الجيم","الحاء","الخاء","الدال","الذال","الراء","الزاي","السين","الشين","الصاد","الضاد","الطاء","الظاء","العين","الغين","الفاء","القاف","الكاف","اللام","الميم","النون","الهاء","الواو","الياء")

private fun tutorialArabicSymbol(index: Int, form: TutorialArabicForm): String {
    return when (val c = tutorialArabic[index]) {
        "ا" -> "ا"
        "ب" -> arrayOf("ﺑ","ﺒ","ﺐ")[form.ordinal]
        "ت" -> arrayOf("ﺗ","ﺘ","ﺖ")[form.ordinal]
        "ث" -> arrayOf("ﺛ","ﺜ","ﺚ")[form.ordinal]
        "ج" -> arrayOf("ﺟ","ﺠ","ﺞ")[form.ordinal]
        "ح" -> arrayOf("ﺣ","ﺤ","ﺢ")[form.ordinal]
        "خ" -> arrayOf("ﺧ","ﺨ","ﺦ")[form.ordinal]
        "د" -> if (form == TutorialArabicForm.FINAL) "ﺪ" else "د"
        "ذ" -> if (form == TutorialArabicForm.FINAL) "ﺬ" else "ذ"
        "ر" -> if (form == TutorialArabicForm.FINAL) "ﺮ" else "ر"
        "ز" -> if (form == TutorialArabicForm.FINAL) "ﺰ" else "ز"
        "س" -> arrayOf("ﺳ","ﺴ","ﺲ")[form.ordinal]
        "ش" -> arrayOf("ﺷ","ﺸ","ﺶ")[form.ordinal]
        "ص" -> arrayOf("ﺻ","ﺼ","ﺺ")[form.ordinal]
        "ض" -> arrayOf("ﺿ","ﻀ","ﺾ")[form.ordinal]
        "ط" -> arrayOf("ﻃ","ﻄ","ﻂ")[form.ordinal]
        "ظ" -> arrayOf("ﻇ","ﻈ","ﻆ")[form.ordinal]
        "ع" -> arrayOf("ﻋ","ﻌ","ﻊ")[form.ordinal]
        "غ" -> arrayOf("ﻏ","ﻐ","ﻎ")[form.ordinal]
        "ف" -> arrayOf("ﻓ","ﻔ","ﻒ")[form.ordinal]
        "ق" -> arrayOf("ﻗ","ﻘ","ﻖ")[form.ordinal]
        "ك" -> arrayOf("ﻛ","ﻜ","ﻚ")[form.ordinal]
        "ل" -> arrayOf("ﻟ","ﻠ","ﻞ")[form.ordinal]
        "م" -> arrayOf("ﻣ","ﻤ","ﻢ")[form.ordinal]
        "ن" -> arrayOf("ﻧ","ﻨ","ﻦ")[form.ordinal]
        "ه" -> arrayOf("ﻫ","ﻬ","ﻪ")[form.ordinal]
        "و" -> if (form == TutorialArabicForm.FINAL) "ﻮ" else "و"
        "ي" -> arrayOf("ﻳ","ﻴ","ﻲ")[form.ordinal]
        else -> c
    }
}

@Composable
fun WritingTutorialScreen(language: String, onBack: () -> Unit, speak: (String, String) -> Unit) {
    val arabic = language == "ar"
    var mode by remember { mutableStateOf("letters") }
    var index by remember { mutableIntStateOf(0) }
    var replay by remember { mutableIntStateOf(0) }
    var arabicForm by remember { mutableStateOf(TutorialArabicForm.INITIAL) }
    var englishCase by remember { mutableStateOf(TutorialEnglishCase.UPPER) }

    val total = if (mode == "letters") if (arabic) tutorialArabic.size else 26 else 100
    val current = index.coerceIn(0, total - 1)
    val symbol = when {
        mode == "numbers" -> if (arabic) (current + 1).toString().map { "٠١٢٣٤٥٦٧٨٩"[it - '0'] }.joinToString("") else (current + 1).toString()
        arabic -> tutorialArabicSymbol(current, arabicForm)
        englishCase == TutorialEnglishCase.UPPER -> ('A'.code + current).toChar().toString()
        else -> ('A'.code + current).toChar().lowercase()
    }

    LaunchedEffect(mode, current, arabicForm, englishCase, replay) {
        speak(if (arabic) "تعلم كتابة $symbol" else "Learn to write $symbol", language)
    }

    CompositionLocalProvider(LocalLayoutDirection provides if (arabic) LayoutDirection.Rtl else LayoutDirection.Ltr) {
        Scaffold(topBar = {
            TopAppBar(
                title = { Text(if (arabic) "تعلم الكتابة" else "Learn to Write", fontWeight = FontWeight.ExtraBold) },
                navigationIcon = { TextButton(onClick = onBack) { Text(if (arabic) "رجوع" else "Back", fontWeight = FontWeight.Bold) } }
            )
        }) { padding ->
            Column(Modifier.fillMaxSize().padding(padding).padding(horizontal = 8.dp, vertical = 4.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(7.dp)) {
                    ChoiceButton(if (arabic) "الحروف" else "Letters", mode == "letters", Color(0xFF4C8BF5), Modifier.weight(1f)) { mode = "letters"; index = 0; replay++ }
                    ChoiceButton(if (arabic) "الأرقام" else "Numbers", mode == "numbers", Color(0xFFFF8A4C), Modifier.weight(1f)) { mode = "numbers"; index = 0; replay++ }
                }
                Spacer(Modifier.height(4.dp))

                if (mode == "letters" && arabic) {
                    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(7.dp)) {
                        ChoiceButton("أولي", arabicForm == TutorialArabicForm.INITIAL, Color(0xFF4C8BF5), Modifier.weight(1f)) { arabicForm = TutorialArabicForm.INITIAL; replay++ }
                        ChoiceButton("وسطي", arabicForm == TutorialArabicForm.MEDIAL, Color(0xFFFFA726), Modifier.weight(1f)) { arabicForm = TutorialArabicForm.MEDIAL; replay++ }
                        ChoiceButton("أخري", arabicForm == TutorialArabicForm.FINAL, Color(0xFF43A047), Modifier.weight(1f)) { arabicForm = TutorialArabicForm.FINAL; replay++ }
                    }
                    Spacer(Modifier.height(4.dp))
                } else if (mode == "letters") {
                    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(7.dp)) {
                        ChoiceButton("UPPERCASE", englishCase == TutorialEnglishCase.UPPER, Color(0xFF4C8BF5), Modifier.weight(1f)) { englishCase = TutorialEnglishCase.UPPER; replay++ }
                        ChoiceButton("lowercase", englishCase == TutorialEnglishCase.LOWER, Color(0xFFFF8A4C), Modifier.weight(1f)) { englishCase = TutorialEnglishCase.LOWER; replay++ }
                    }
                    Spacer(Modifier.height(4.dp))
                }

                Row(Modifier.fillMaxWidth().height(48.dp), horizontalArrangement = Arrangement.Center, verticalAlignment = Alignment.CenterVertically) {
                    Text("${current + 1} / $total", fontWeight = FontWeight.ExtraBold, fontSize = 13.sp)
                    Spacer(Modifier.width(8.dp))
                    Text(symbol, fontSize = 34.sp, fontWeight = FontWeight.Black, color = Color(0xFF315CFF))
                    Spacer(Modifier.width(8.dp))
                    Text(if (arabic) (if (mode == "letters") tutorialArabicNames[current] else "الرقم $symbol") else (if (mode == "letters") symbol else "Number $symbol"), fontSize = 14.sp, fontWeight = FontWeight.Bold)
                }

                Card(Modifier.fillMaxWidth().weight(1f), shape = RoundedCornerShape(24.dp), colors = CardDefaults.cardColors(containerColor = Color(0xFFFFFBF0)), elevation = CardDefaults.cardElevation(8.dp)) {
                    AndroidView(
                        modifier = Modifier.fillMaxSize().padding(4.dp),
                        factory = { WritingTraceView(it) },
                        update = { it.setLesson(symbol, replay) }
                    )
                }

                Spacer(Modifier.height(4.dp))
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(7.dp)) {
                    NavButton(if (arabic) "السابق" else "Previous", Color(0xFF5C6BC0), current > 0, Modifier.weight(1f)) { if (current > 0) { index = current - 1; replay++ } }
                    NavButton(if (arabic) "إعادة" else "Replay", Color(0xFF039BE5), true, Modifier.weight(1f)) { replay++ }
                    NavButton(if (arabic) "التالي" else "Next", Color(0xFF2EAD69), current < total - 1, Modifier.weight(1f)) { if (current < total - 1) { index = current + 1; replay++ } }
                }
            }
        }
    }
}

@Composable
private fun ChoiceButton(text: String, selected: Boolean, color: Color, modifier: Modifier, onClick: () -> Unit) {
    Card(modifier.height(54.dp).clickable(onClick = onClick), shape = RoundedCornerShape(16.dp), colors = CardDefaults.cardColors(containerColor = if (selected) color else Color.White), elevation = CardDefaults.cardElevation(if (selected) 7.dp else 2.dp)) {
        Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) { Text(text, fontSize = 16.sp, fontWeight = FontWeight.Black, color = if (selected) Color.White else color) }
    }
}

@Composable
private fun NavButton(text: String, color: Color, enabled: Boolean, modifier: Modifier, onClick: () -> Unit) {
    Button(onClick = onClick, enabled = enabled, modifier = modifier.height(58.dp), shape = RoundedCornerShape(17.dp), colors = ButtonDefaults.buttonColors(containerColor = color, disabledContainerColor = Color(0xFFD9E0E5))) {
        Text(text, fontSize = 17.sp, fontWeight = FontWeight.ExtraBold)
    }
}
