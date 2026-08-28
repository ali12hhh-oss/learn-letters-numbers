@file:OptIn(androidx.compose.material3.ExperimentalMaterial3Api::class)

package com.learnlettersnumbers.app

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

private data class LearningGame(val id: String, val title: String, val subtitle: String, val icon: String, val color: Color, val category: String)

private val games = listOf(
    LearningGame("match", "صائد الحروف", "اضرب الحرف المطلوب", "🔤", Color(0xFFFFD166), "حروف"),
    LearningGame("number", "صيد الأرقام", "التقط الرقم الصحيح", "🎯", Color(0xFF8ED1FC), "أرقام"),
    LearningGame("memory", "ذاكرة الأبطال", "اكشف وطابق الأزواج", "🧠", Color(0xFFCDB4DB), "حروف"),
    LearningGame("sort", "سباق الترتيب", "رتب الأرقام بسرعة", "🏁", Color(0xFFA8E6CF), "أرقام"),
    LearningGame("word", "الكلمة السحرية", "أكمل الكلمة الناقصة", "🪄", Color(0xFFFFAAA5), "قراءة"),
    LearningGame("listen", "اسمع واربح", "استمع واختر بسرعة", "🔊", Color(0xFFFFD6A5), "حروف"),
    LearningGame("count", "مزرعة الأعداد", "عد العناصر بدقة", "🌟", Color(0xFFCAFFBF), "أرقام"),
    LearningGame("build", "صانع الكلمات", "كوّن الكلمة بالحروف", "🧩", Color(0xFFFFC8DD), "قراءة"),
    LearningGame("quick", "التحدي الذهبي", "أسئلة متنوعة", "🏆", Color(0xFFFFE5B4), "متنوع"),
    LearningGame("shapes", "شكل الحرف", "تعرف على الحرف الصحيح", "✏️", Color(0xFFBDE0FE), "حروف")
)

@Composable
fun GamesScreen(onBack: () -> Unit, repo: ProgressRepository, onSpeak: ((String, String) -> Unit)? = null) {
    var selected by remember { mutableStateOf<LearningGame?>(null) }
    if (selected == null) GameHubScreen(games, onBack) { selected = it }
    else ProfessionalGameScreen(selected!!, { selected = null }, repo, onSpeak)
}

@Composable
private fun GameHubScreen(games: List<LearningGame>, onBack: () -> Unit, onGameSelected: (LearningGame) -> Unit) {
    Scaffold(topBar = {
        TopAppBar(title = { Text("🎮 عالم الألعاب", fontWeight = FontWeight.ExtraBold) }, navigationIcon = {
            IconButton(onClick = onBack) { Icon(Icons.Default.ArrowBack, "رجوع") }
        }, actions = { Text("🏆", fontSize = 24.sp); Spacer(Modifier.width(12.dp)) })
    }) { padding ->
        Column(Modifier.fillMaxSize().padding(padding).padding(horizontal = 14.dp)) {
            Spacer(Modifier.height(8.dp))
            Card(Modifier.fillMaxWidth(), shape = RoundedCornerShape(28.dp), colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer)) {
                Column(Modifier.padding(20.dp)) {
                    Text("مستعد للتحدي؟ 🚀", fontSize = 28.sp, fontWeight = FontWeight.ExtraBold)
                    Spacer(Modifier.height(5.dp))
                    Text("اختر لعبة، اجمع النجوم، وافتح طريقك نحو لقب البطل!", fontSize = 15.sp)
                }
            }
            Spacer(Modifier.height(14.dp)); Text("الألعاب", fontSize = 22.sp, fontWeight = FontWeight.Bold); Spacer(Modifier.height(8.dp))
            LazyVerticalGrid(columns = GridCells.Fixed(2), contentPadding = PaddingValues(bottom = 20.dp), horizontalArrangement = Arrangement.spacedBy(12.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                items(games, key = { it.id }) { game -> GameCard(game) { onGameSelected(game) } }
            }
        }
    }
}

@Composable
private fun GameCard(game: LearningGame, onClick: () -> Unit) {
    var pressed by remember { mutableStateOf(false) }
    val scale by animateFloatAsState(if (pressed) .96f else 1f, tween(100), label = "gameCardScale")
    Card(Modifier.fillMaxWidth().height(174.dp).scale(scale).clickable { pressed = true; onClick(); pressed = false }, shape = RoundedCornerShape(26.dp), colors = CardDefaults.cardColors(containerColor = game.color), elevation = CardDefaults.cardElevation(7.dp)) {
        Column(Modifier.fillMaxSize().padding(13.dp), horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.Center) {
            Text(game.icon, fontSize = 45.sp); Spacer(Modifier.height(4.dp)); Text(game.title, fontSize = 20.sp, fontWeight = FontWeight.ExtraBold); Text(game.subtitle, fontSize = 12.sp); Spacer(Modifier.height(6.dp))
            Surface(shape = RoundedCornerShape(50), color = Color.White.copy(alpha = .45f)) { Text(game.category, Modifier.padding(horizontal = 10.dp, vertical = 3.dp), fontSize = 11.sp) }
        }
    }
}

private data class RoundQuestion(val prompt: String, val options: List<String>, val answer: String, val spoken: String = prompt)

private fun questionFor(game: LearningGame, index: Int): RoundQuestion {
    val letters = listOf("أ", "ب", "ت", "ث", "ج", "ح", "خ", "د", "ر", "س")
    return when (game.id) {
        "match" -> { val a = letters[index % 10]; RoundQuestion("اضغط على الحرف: $a", listOf(a, letters[(index + 2) % 10], letters[(index + 5) % 10], letters[(index + 7) % 10]).shuffled(), a) }
        "number" -> { val a = ((index * 3) % 9 + 1).toString(); RoundQuestion("التقط الرقم $a", listOf(a, "${(index + 2) % 9 + 1}", "${(index + 4) % 9 + 1}", "${(index + 6) % 9 + 1}").distinct().shuffled(), a) }
        "sort" -> { val base = (index % 5) + 1; val v = listOf(base + 3, base + 1, base + 4, base); RoundQuestion("ما الرقم الأصغر؟", v.map(Int::toString).shuffled(), base.toString()) }
        "word" -> { val d = listOf("ب_ب" to "ا", "ك_ب" to "ت", "ق_م" to "ل", "م_رس" to "د", "س_مك" to "م"); val (p, a) = d[index % d.size]; RoundQuestion("أكمل الكلمة: $p", listOf(a, "ب", "ن", "س").shuffled(), a) }
        "listen" -> { val d = listOf("أ" to "ألف", "ب" to "باء", "ت" to "تاء", "ج" to "جيم", "م" to "ميم"); val (a, n) = d[index % d.size]; RoundQuestion("استمع ثم اختر الحرف", listOf(a, "د", "س", "ك").shuffled(), a, n) }
        "count" -> { val a = ((index % 7) + 2).toString(); RoundQuestion("كم نجمة؟", listOf(a, "${a.toInt() + 1}", "${a.toInt() - 1}", "9").distinct().shuffled(), a) }
        "build" -> { val d = listOf("ب + ا" to "با", "م + ا" to "ما", "د + ا" to "دا", "ل + ا" to "لا", "س + ا" to "سا"); val (p, a) = d[index % d.size]; RoundQuestion("كوّن: $p", listOf(a, "بو", "مي", "دو").shuffled(), a) }
        "shapes" -> { val a = letters[(index + 1) % 10]; RoundQuestion("أي حرف تراه هنا؟", listOf(a, letters[(index + 3) % 10], letters[(index + 6) % 10], letters[(index + 8) % 10]).shuffled(), a) }
        else -> { val a = (index % 8) + 1; RoundQuestion("$a + 1 = ؟", listOf((a + 1).toString(), a.toString(), (a + 2).toString(), "9").distinct().shuffled(), (a + 1).toString()) }
    }
}

@Composable
private fun ProfessionalGameScreen(game: LearningGame, onBack: () -> Unit, repo: ProgressRepository, onSpeak: ((String, String) -> Unit)?) {
    var round by remember { mutableStateOf(0) }; var score by remember { mutableStateOf(0) }; var streak by remember { mutableStateOf(0) }; var lives by remember { mutableStateOf(3) }; var answered by remember { mutableStateOf(false) }; var selected by remember { mutableStateOf<String?>(null) }; var finished by remember { mutableStateOf(false) }
    val total = 10; val question = questionFor(game, round); val progress = (round.coerceAtMost(total).toFloat() / total).coerceIn(0f, 1f)
    if (finished) { GameResultScreen(game, score, streak, onBack) { round = 0; score = 0; streak = 0; lives = 3; answered = false; selected = null; finished = false }; return }
    Scaffold(topBar = { TopAppBar(title = { Text(game.title, fontWeight = FontWeight.Bold) }, navigationIcon = { IconButton(onClick = onBack) { Icon(Icons.Default.ArrowBack, "رجوع") } }) }) { padding ->
        Column(Modifier.fillMaxSize().padding(padding).padding(horizontal = 16.dp), horizontalAlignment = Alignment.CenterHorizontally) {
            Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) { Text("${round + 1}/$total", fontWeight = FontWeight.Bold); Spacer(Modifier.width(10.dp)); LinearProgressIndicator(progress = { progress }, Modifier.weight(1f).height(9.dp)); Spacer(Modifier.width(10.dp)); Text("⭐ $score", fontWeight = FontWeight.Bold) }
            Spacer(Modifier.height(10.dp)); Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) { repeat(3) { i -> Icon(Icons.Default.Favorite, null, tint = if (i < lives) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.surfaceVariant, modifier = Modifier.size(22.dp)) } }
            Spacer(Modifier.height(8.dp)); if (game.id == "count") CountChallenge(round) else GameQuestionPanel(game, question, onSpeak); Spacer(Modifier.height(14.dp))
            question.options.forEach { option ->
                AnswerButton(option, !answered, selected == option, answered && option == question.answer, answered && selected == option && option != question.answer) {
                    selected = option; answered = true; val correct = option == question.answer; repo.recordAnswer(correct)
                    if (correct) { val gained = 1 + streak.coerceAtMost(4); score += gained; streak++; repo.addStars(gained); onSpeak?.invoke("أحسنت! إجابة صحيحة ⭐", "ar") }
                    else { lives--; streak = 0; onSpeak?.invoke("حاول مرة أخرى، أنت تستطيع 💪", "ar") }
                }; Spacer(Modifier.height(7.dp))
            }
            AnimatedVisibility(answered, enter = fadeIn() + scaleIn(), exit = fadeOut() + scaleOut()) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) { Spacer(Modifier.height(5.dp)); Text(if (selected == question.answer) "رائع! إجابة صحيحة ⭐" else "الإجابة الصحيحة: ${question.answer}", fontSize = 18.sp, fontWeight = FontWeight.ExtraBold); Spacer(Modifier.height(7.dp)); Button(onClick = { if (round + 1 >= total || lives <= 0) finished = true else { round++; answered = false; selected = null } }) { Text(if (round + 1 >= total || lives <= 0) "عرض النتيجة 🏆" else "السؤال التالي ➜") } }
            }
        }
    }
}

@Composable
private fun GameQuestionPanel(game: LearningGame, question: RoundQuestion, onSpeak: ((String, String) -> Unit)?) {
    Card(Modifier.fillMaxWidth().height(180.dp), shape = RoundedCornerShape(30.dp), colors = CardDefaults.cardColors(containerColor = game.color.copy(alpha = .75f))) {
        Column(Modifier.fillMaxSize().padding(18.dp), horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.Center) {
            Text(game.icon, fontSize = 45.sp); Text(question.prompt, fontSize = 24.sp, fontWeight = FontWeight.ExtraBold)
            if (game.id == "listen") { Spacer(Modifier.height(5.dp)); FilledTonalButton(onClick = { onSpeak?.invoke(question.spoken, "ar") }) { Text("🔊", fontSize = 20.sp); Spacer(Modifier.width(5.dp)); Text("استمع للسؤال") } }
        }
    }
}

@Composable
private fun CountChallenge(round: Int) {
    val count = (round % 7) + 2
    Card(Modifier.fillMaxWidth().height(180.dp), shape = RoundedCornerShape(30.dp), colors = CardDefaults.cardColors(containerColor = Color(0xFFCAFFBF).copy(alpha = .8f))) {
        Column(Modifier.fillMaxSize(), horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.Center) { Text("عد النجوم", fontSize = 22.sp, fontWeight = FontWeight.Bold); Spacer(Modifier.height(12.dp)); Row(horizontalArrangement = Arrangement.Center) { repeat(count) { Text("⭐", fontSize = 28.sp) } }; Spacer(Modifier.height(8.dp)); Text("كم عددها؟", fontSize = 18.sp, fontWeight = FontWeight.Bold) }
    }
}

@Composable
private fun AnswerButton(text: String, enabled: Boolean, selected: Boolean, correct: Boolean, wrong: Boolean, onClick: () -> Unit) {
    val scale by animateFloatAsState(if (selected) .97f else 1f, tween(120), label = "answerScale")
    val bg = when { correct -> Color(0xFF8BE28B); wrong -> Color(0xFFFF9A9A); else -> MaterialTheme.colorScheme.surface }
    Surface(Modifier.fillMaxWidth().height(52.dp).scale(scale).clickable(enabled = enabled, onClick = onClick), shape = RoundedCornerShape(18.dp), color = bg, tonalElevation = 3.dp, shadowElevation = 2.dp) {
        Row(Modifier.fillMaxSize().padding(horizontal = 16.dp), verticalAlignment = Alignment.CenterVertically) { Text(text, Modifier.weight(1f), fontSize = 20.sp, fontWeight = FontWeight.Bold); if (correct) Icon(Icons.Default.CheckCircle, null) }
    }
}

@Composable
private fun GameResultScreen(game: LearningGame, score: Int, streak: Int, onBack: () -> Unit, onReplay: () -> Unit) {
    Column(Modifier.fillMaxSize().padding(22.dp), horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.Center) {
        Text("🏆", fontSize = 90.sp); Text("انتهى التحدي!", fontSize = 32.sp, fontWeight = FontWeight.ExtraBold); Spacer(Modifier.height(8.dp)); Text(game.title, fontSize = 20.sp); Spacer(Modifier.height(20.dp))
        Card(shape = RoundedCornerShape(28.dp), colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer)) { Column(Modifier.padding(25.dp), horizontalAlignment = Alignment.CenterHorizontally) { Text("النتيجة", fontSize = 18.sp); Text("⭐ $score", fontSize = 44.sp, fontWeight = FontWeight.ExtraBold); Text("🔥 أفضل سلسلة: $streak", fontSize = 17.sp) } }
        Spacer(Modifier.height(20.dp)); Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) { OutlinedButton(onClick = onBack) { Text("خروج") }; Button(onClick = onReplay) { Text("العب مرة أخرى 🔄") } }
    }
}
