@file:OptIn(androidx.compose.material3.ExperimentalMaterial3Api::class)
package com.learnlettersnumbers.app

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView

private enum class ArabicForm { INITIAL, MEDIAL, FINAL }
private enum class WritingEnglishCase { UPPER, LOWER }

private val arLetters = listOf("ا","ب","ت","ث","ج","ح","خ","د","ذ","ر","ز","س","ش","ص","ض","ط","ظ","ع","غ","ف","ق","ك","ل","م","ن","ه","و","ي")
private val arNames = listOf("الألف","الباء","التاء","الثاء","الجيم","الحاء","الخاء","الدال","الذال","الراء","الزاي","السين","الشين","الصاد","الضاد","الطاء","الظاء","العين","الغين","الفاء","القاف","الكاف","اللام","الميم","النون","الهاء","الواو","الياء")
private val enLetters = ('A'..'Z').toList()

private fun arabicFormSymbol(index: Int, form: ArabicForm): String {
    return when (val c = arLetters[index]) {
        "ا" -> "ا"
        "ب" -> arrayOf("ﺑ","ﺒ","ﺐ")[form.ordinal]
        "ت" -> arrayOf("ﺗ","ﺘ","ﺖ")[form.ordinal]
        "ث" -> arrayOf("ﺛ","ﺜ","ﺚ")[form.ordinal]
        "ج" -> arrayOf("ﺟ","ﺠ","ﺞ")[form.ordinal]
        "ح" -> arrayOf("ﺣ","ﺤ","ﺢ")[form.ordinal]
        "خ" -> arrayOf("ﺧ","ﺨ","ﺦ")[form.ordinal]
        "د" -> if (form == ArabicForm.FINAL) "ﺪ" else "د"
        "ذ" -> if (form == ArabicForm.FINAL) "ﺬ" else "ذ"
        "ر" -> if (form == ArabicForm.FINAL) "ﺮ" else "ر"
        "ز" -> if (form == ArabicForm.FINAL) "ﺰ" else "ز"
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
        "و" -> if (form == ArabicForm.FINAL) "ﻮ" else "و"
        "ي" -> arrayOf("ﻳ","ﻴ","ﻲ")[form.ordinal]
        else -> c
    }
}

private fun arabicFormName(form: ArabicForm) = when (form) {
    ArabicForm.INITIAL -> "أولي"
    ArabicForm.MEDIAL -> "وسطي"
    ArabicForm.FINAL -> "أخري"
}

@Composable
fun WritingStrokeLessonScreen(language: String, numbers: Boolean, onBack: () -> Unit, speak: (String, String) -> Unit) {
    val arabic = language == "ar"
    var index by remember { mutableIntStateOf(0) }
    var form by remember { mutableStateOf(ArabicForm.INITIAL) }
    var englishCase by remember { mutableStateOf(WritingEnglishCase.UPPER) }
    var replay by remember { mutableIntStateOf(0) }
    val total = if (numbers) 10 else if (arabic) arLetters.size else enLetters.size
    val current = index.coerceIn(0, total - 1)
    val symbol = when {
        numbers -> if (arabic) (current + 1).toString().map { ch -> "٠١٢٣٤٥٦٧٨٩"[ch - '0'] }.joinToString("") else (current + 1).toString()
        arabic -> arabicFormSymbol(current, form)
        englishCase == WritingEnglishCase.UPPER -> enLetters[current].toString()
        else -> enLetters[current].lowercase()
    }
    val title = when {
        numbers -> "${if (arabic) "الرقم" else "Number"} $symbol"
        arabic -> "${arNames[current]} — ${arabicFormName(form)}"
        englishCase == WritingEnglishCase.UPPER -> "${enLetters[current]} — حروف كبيرة"
        else -> "${enLetters[current].lowercase()} — حروف صغيرة"
    }

    LaunchedEffect(current, form, englishCase, replay) {
        val message = when {
            numbers -> if (arabic) "تعلم كتابة الرقم $symbol" else "Learn to write number $symbol"
            arabic -> "تعلم كتابة ${arNames[current]}، الشكل ${arabicFormName(form)}"
            englishCase == WritingEnglishCase.UPPER -> "Learn to write capital letter ${enLetters[current]}"
            else -> "Learn to write lowercase letter ${enLetters[current].lowercase()}"
        }
        speak(message, language)
    }

    CompositionLocalProvider(LocalLayoutDirection provides if (arabic) LayoutDirection.Rtl else LayoutDirection.Ltr) {
        Scaffold(topBar = {
            TopAppBar(
                title = { Text(if (arabic) "تعلم الكتابة" else "Learn to Write", fontWeight = FontWeight.ExtraBold) },
                navigationIcon = { TextButton(onClick = onBack) { Text(if (arabic) "رجوع" else "Back", fontWeight = FontWeight.Bold, fontSize = 17.sp) } }
            )
        }) { padding ->
            Column(Modifier.fillMaxSize().padding(padding).padding(horizontal = 8.dp, vertical = 4.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                if (arabic && !numbers) {
                    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(7.dp)) {
                        FormButton("أولي", form == ArabicForm.INITIAL, Color(0xFF4C8BF5), Modifier.weight(1f)) { form = ArabicForm.INITIAL; replay++ }
                        FormButton("وسطي", form == ArabicForm.MEDIAL, Color(0xFFFFA726), Modifier.weight(1f)) { form = ArabicForm.MEDIAL; replay++ }
                        FormButton("أخري", form == ArabicForm.FINAL, Color(0xFF43A047), Modifier.weight(1f)) { form = ArabicForm.FINAL; replay++ }
                    }
                    Spacer(Modifier.height(4.dp))
                    Text("الحروف", modifier = Modifier.fillMaxWidth(), fontSize = 14.sp, fontWeight = FontWeight.ExtraBold, textAlign = TextAlign.End)
                    Row(Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()), horizontalArrangement = Arrangement.spacedBy(5.dp)) {
                        arLetters.forEachIndexed { i, letter ->
                            Card(Modifier.size(42.dp).clickable { index = i; replay++ }, shape = RoundedCornerShape(11.dp), colors = CardDefaults.cardColors(containerColor = if (i == current) Color(0xFF4C8BF5) else Color.White), elevation = CardDefaults.cardElevation(if (i == current) 5.dp else 2.dp)) {
                                Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) { Text(letter, fontSize = 20.sp, fontWeight = FontWeight.Black, color = if (i == current) Color.White else Color(0xFF315CFF)) }
                            }
                        }
                    }
                    Spacer(Modifier.height(4.dp))
                    Text("الحروف", modifier = Modifier.fillMaxWidth(), fontSize = 14.sp, fontWeight = FontWeight.ExtraBold, textAlign = TextAlign.End)
                    Row(Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()), horizontalArrangement = Arrangement.spacedBy(5.dp)) {
                        arLetters.forEachIndexed { i, letter ->
                            Card(Modifier.size(42.dp).clickable { index = i; replay++ }, shape = RoundedCornerShape(11.dp), colors = CardDefaults.cardColors(containerColor = if (i == current) Color(0xFF4C8BF5) else Color.White), elevation = CardDefaults.cardElevation(if (i == current) 5.dp else 2.dp)) {
                                Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) { Text(letter, fontSize = 20.sp, fontWeight = FontWeight.Black, color = if (i == current) Color.White else Color(0xFF315CFF)) }
                            }
                        }
                    }
                    Spacer(Modifier.height(4.dp))
                } else if (!arabic && !numbers) {
                    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(7.dp)) {
                        FormButton("UPPERCASE", englishCase == WritingEnglishCase.UPPER, Color(0xFF4C8BF5), Modifier.weight(1f)) { englishCase = WritingEnglishCase.UPPER; replay++ }
                        FormButton("lowercase", englishCase == WritingEnglishCase.LOWER, Color(0xFFFF8A4C), Modifier.weight(1f)) { englishCase = WritingEnglishCase.LOWER; replay++ }
                    }
                    Spacer(Modifier.height(4.dp))
                }

                Row(Modifier.fillMaxWidth().height(48.dp), horizontalArrangement = Arrangement.Center, verticalAlignment = Alignment.CenterVertically) {
                    Text("${current + 1} / $total", fontWeight = FontWeight.ExtraBold, fontSize = 13.sp)
                    Spacer(Modifier.width(8.dp))
                    Text(symbol, fontSize = 34.sp, fontWeight = FontWeight.Black, color = Color(0xFF315CFF))
                    Spacer(Modifier.width(8.dp))
                    Text(title, fontSize = 14.sp, fontWeight = FontWeight.Bold)
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
                    LessonButton(if (arabic) "السابق\nPrevious" else "Previous", Color(0xFF5C6BC0), current > 0, Modifier.weight(1f)) { if (current > 0) { index = current - 1; replay++ } }
                    LessonButton("🔄 ${if (arabic) "إعادة" else "Replay"}", Color(0xFF039BE5), true, Modifier.weight(1f)) { replay++ }
                    LessonButton(if (arabic) "التالي\nNext" else "Next", Color(0xFF2EAD69), current < total - 1, Modifier.weight(1f)) { if (current < total - 1) { index = current + 1; replay++ } }
                }
            }
        }
    }
}

@Composable
private fun FormButton(title: String, selected: Boolean, color: Color, modifier: Modifier, onClick: () -> Unit) {
    Card(modifier.height(56.dp).clickable(onClick = onClick), shape = RoundedCornerShape(16.dp), colors = CardDefaults.cardColors(containerColor = if (selected) color else Color.White), elevation = CardDefaults.cardElevation(if (selected) 7.dp else 2.dp)) {
        Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Text(title, fontSize = 17.sp, fontWeight = FontWeight.Black, color = if (selected) Color.White else color, textAlign = TextAlign.Center)
        }
    }
}

@Composable
private fun LessonButton(text: String, color: Color, enabled: Boolean, modifier: Modifier, onClick: () -> Unit) {
    Button(onClick = onClick, enabled = enabled, modifier = modifier.height(58.dp), shape = RoundedCornerShape(17.dp), colors = ButtonDefaults.buttonColors(containerColor = color, disabledContainerColor = Color(0xFFD9E0E5))) {
        Text(text, fontWeight = FontWeight.ExtraBold, fontSize = 17.sp, textAlign = TextAlign.Center)
    }
}
