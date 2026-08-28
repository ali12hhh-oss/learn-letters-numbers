@file:OptIn(androidx.compose.material3.ExperimentalMaterial3Api::class)

package com.learnlettersnumbers.app

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.core.tween
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

private data class GameEntry(val id: String, val title: String, val subtitle: String, val accent: Color, val kind: GameKind)

private val gameEntries = listOf(
    GameEntry("match", "طابق الحرف", "اعثر على الحرف المطلوب", Color(0xFF4F8EF7), GameKind.LETTER),
    GameEntry("hunt", "صيد الأرقام", "التقط الرقم الصحيح", Color(0xFFFF8A4C), GameKind.NUMBER),
    GameEntry("memory", "ذاكرة الحروف", "اكشف وطابق الأزواج", Color(0xFF9B7EDE), GameKind.MEMORY),
    GameEntry("order", "رتّب الأرقام", "رتبها من الأصغر للأكبر", Color(0xFF35B779), GameKind.ORDER),
    GameEntry("missing", "الكلمة المفقودة", "أكمل الكلمة", Color(0xFFE96A8D), GameKind.WORD),
    GameEntry("listen", "اسمع واختر", "استمع ثم اختر", Color(0xFF0FA3B1), GameKind.LISTEN),
    GameEntry("shape", "شكل الحرف", "تعرف على الحرف", Color(0xFFF2B134), GameKind.SHAPE),
    GameEntry("count", "عدّ الأشياء", "عدّ ثم اختر العدد", Color(0xFF5E8CFF), GameKind.COUNT),
    GameEntry("build", "سباق الكلمات", "كوّن الكلمة", Color(0xFFEF6C3B), GameKind.BUILD),
    GameEntry("quick", "التحدي السريع", "مزيج من المهارات", Color(0xFF7A5AF8), GameKind.MIXED)
)

@Composable
fun GamesScreen(onBack: () -> Unit, repo: ProgressRepository, onSpeak: ((String, String) -> Unit)? = null) {
    var selected by remember { mutableStateOf<GameEntry?>(null) }
    AnimatedContent(
        targetState = selected,
        transitionSpec = { fadeIn(tween(180)) togetherWith fadeOut(tween(120)) },
        label = "games_navigation"
    ) { game ->
        if (game == null) GameLobby(onBack) { selected = it }
        else ProfessionalGameScreen(game, repo, onSpeak) { selected = null }
    }
}

@Composable
private fun GameLobby(onBack: () -> Unit, onGameClick: (GameEntry) -> Unit) {
    Scaffold(
        containerColor = Color(0xFFF4F7FF),
        topBar = {
            TopAppBar(
                title = { Text("الألعاب التعليمية", fontWeight = FontWeight.ExtraBold) },
                navigationIcon = { IconButton(onClick = onBack) { Icon(Icons.Default.ArrowBack, "رجوع") } },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.Transparent)
            )
        }
    ) { padding ->
        Column(Modifier.fillMaxSize().padding(padding).padding(horizontal = 16.dp)) {
            Card(
                Modifier.fillMaxWidth().height(138.dp),
                shape = RoundedCornerShape(30.dp),
                colors = CardDefaults.cardColors(containerColor = Color.Transparent),
                elevation = CardDefaults.cardElevation(7.dp)
            ) {
                Box(Modifier.fillMaxSize().background(Brush.linearGradient(listOf(Color(0xFF4F8EF7), Color(0xFF7A5AF8))))) {
                    Column(Modifier.fillMaxSize().padding(22.dp), verticalArrangement = Arrangement.Center) {
                        Text("🎮 مدينة الألعاب", color = Color.White, fontSize = 27.sp, fontWeight = FontWeight.ExtraBold)
                        Text("تعلم • العب • اجمع النجوم ⭐", color = Color.White.copy(.94f), fontSize = 15.sp)
                    }
                }
            }
            Spacer(Modifier.height(12.dp))
            Text("هيا نلعب ونتعلم! 🌟", fontSize = 26.sp, fontWeight = FontWeight.ExtraBold, modifier = Modifier.fillMaxWidth(), textAlign = TextAlign.Center)
            Text("اختر لعبة وابدأ مغامرة تعليمية", fontSize = 14.sp, color = Color(0xFF667085), modifier = Modifier.fillMaxWidth(), textAlign = TextAlign.Center)
            Spacer(Modifier.height(14.dp))
            LazyVerticalGrid(
                columns = GridCells.Fixed(2),
                modifier = Modifier.fillMaxSize(),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp),
                contentPadding = PaddingValues(bottom = 24.dp)
            ) { items(gameEntries, key = { it.id }) { GameCard(it, onGameClick) } }
        }
    }
}

@Composable
private fun GameCard(game: GameEntry, onClick: (GameEntry) -> Unit) {
    var pressed by remember { mutableStateOf(false) }
    val scale by androidx.compose.animation.core.animateFloatAsState(if (pressed) .95f else 1f, tween(100), label = "card_scale")
    Card(
        Modifier.fillMaxWidth().height(172.dp).scale(scale).clickable {
            pressed = true
            onClick(game)
            pressed = false
        },
        shape = RoundedCornerShape(25.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(5.dp)
    ) {
        Column(Modifier.fillMaxSize().padding(12.dp), horizontalAlignment = Alignment.CenterHorizontally) {
            Box(Modifier.size(88.dp).background(game.accent.copy(.14f), CircleShape), contentAlignment = Alignment.Center) {
                Text(gameIcon(game.kind), fontSize = 40.sp)
            }
            Spacer(Modifier.height(7.dp))
            Text(game.title, fontSize = 17.sp, fontWeight = FontWeight.ExtraBold, textAlign = TextAlign.Center)
            Text(game.subtitle, fontSize = 11.sp, color = Color(0xFF667085), textAlign = TextAlign.Center)
        }
    }
}

private fun gameIcon(kind: GameKind): String = when (kind) {
    GameKind.LETTER -> "🔤"
    GameKind.NUMBER -> "🔢"
    GameKind.MEMORY -> "🧠"
    GameKind.ORDER -> "📊"
    GameKind.WORD -> "🧩"
    GameKind.LISTEN -> "🔊"
    GameKind.SHAPE -> "🔠"
    GameKind.COUNT -> "⭐"
    GameKind.BUILD -> "🏁"
    GameKind.MIXED -> "⚡"
}

@Composable
private fun ProfessionalGameScreen(game: GameEntry, repo: ProgressRepository, onSpeak: ((String, String) -> Unit)?, onBack: () -> Unit) {
    var level by remember { mutableStateOf(GameLevel.EASY) }
    var started by remember { mutableStateOf(false) }
    var completed by remember { mutableStateOf(false) }
    var stars by remember { mutableIntStateOf(0) }

    if (!started) {
        GameStart(game, level, { level = it }, { started = true }, onBack)
        return
    }
    if (completed) {
        GameFinished(game, stars, { started = false; completed = false; stars = 0 }, onBack)
        return
    }

    when (game.kind) {
        GameKind.MEMORY -> InteractiveMemoryGame(level) { earned ->
            stars = earned
            repo.addStars(earned)
            repo.recordLesson("الألعاب", game.title, true)
            onSpeak?.invoke("أحسنت! أكملت لعبة ${game.title}", "ar")
            completed = true
        }
        GameKind.ORDER -> InteractiveOrderGame(level) { earned ->
            stars = earned
            repo.addStars(earned)
            repo.recordLesson("الألعاب", game.title, true)
            onSpeak?.invoke("رائع! رتبت الأرقام بشكل صحيح", "ar")
            completed = true
        }
        GameKind.COUNT -> InteractiveCountSession(level, game, repo, onSpeak) { earned ->
            stars = earned
            completed = true
        }
        else -> GenericGameSession(game, level, repo, onSpeak) { earned ->
            stars = earned
            completed = true
        }
    }
}

@Composable
private fun GameStart(game: GameEntry, level: GameLevel, onLevel: (GameLevel) -> Unit, onStart: () -> Unit, onBack: () -> Unit) {
    Scaffold(containerColor = Color(0xFFF5F7FF), topBar = { TopAppBar(title = { Text(game.title, fontWeight = FontWeight.ExtraBold) }, navigationIcon = { IconButton(onClick = onBack) { Icon(Icons.Default.ArrowBack, "رجوع") } }, colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.Transparent)) }) { padding ->
        Column(Modifier.fillMaxSize().padding(padding).padding(22.dp), horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.Center) {
            Text(gameIcon(game.kind), fontSize = 76.sp)
            Spacer(Modifier.height(12.dp))
            Text(game.title, fontSize = 30.sp, fontWeight = FontWeight.ExtraBold)
            Text(game.subtitle, fontSize = 16.sp, color = Color(0xFF667085), textAlign = TextAlign.Center)
            Spacer(Modifier.height(24.dp))
            Text("اختر مستوى التحدي", fontWeight = FontWeight.Bold, fontSize = 18.sp)
            Spacer(Modifier.height(10.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) { GameLevel.values().forEach { FilterChip(level == it, { onLevel(it) }, label = { Text(it.label) }) } }
            Spacer(Modifier.height(24.dp))
            Card(Modifier.fillMaxWidth(), shape = RoundedCornerShape(22.dp), colors = CardDefaults.cardColors(containerColor = Color.White)) {
                Column(Modifier.padding(18.dp)) {
                    Text("🎯 جلسة تعليمية", fontWeight = FontWeight.Bold, fontSize = 17.sp)
                    Text("⭐ نجوم • 🧠 مهارة • 🏆 تقدم محفوظ", color = Color(0xFF667085), fontSize = 14.sp)
                }
            }
            Spacer(Modifier.height(22.dp))
            Button(onClick = onStart, modifier = Modifier.fillMaxWidth().height(58.dp), shape = RoundedCornerShape(20.dp), colors = ButtonDefaults.buttonColors(containerColor = game.accent)) { Text("ابدأ اللعب 🚀", fontSize = 19.sp, fontWeight = FontWeight.ExtraBold) }
        }
    }
}

@Composable
private fun InteractiveCountSession(level: GameLevel, game: GameEntry, repo: ProgressRepository, speak: ((String, String) -> Unit)?, onComplete: (Int) -> Unit) {
    var round by remember { mutableIntStateOf(0) }
    var score by remember { mutableIntStateOf(0) }
    val rounds = 6
    Column(Modifier.fillMaxSize().padding(16.dp), horizontalAlignment = Alignment.CenterHorizontally) {
        Text("الجولة ${round + 1} من $rounds", fontWeight = FontWeight.Bold, fontSize = 18.sp)
        LinearProgressIndicator((round / rounds.toFloat()).coerceIn(0f, 1f), Modifier.fillMaxWidth().padding(vertical = 12.dp))
        InteractiveCountGame(level) { correct ->
            if (correct) {
                score++
                speak?.invoke("أحسنت!", "ar")
                if (round == rounds - 1) {
                    val stars = when { score >= 5 -> 3; score >= 3 -> 2; else -> 1 }
                    repo.addStars(stars)
                    repo.recordLesson("الألعاب", game.title, true)
                    onComplete(stars)
                } else round++
            } else speak?.invoke("حاول مرة أخرى", "ar")
        }
    }
}

@Composable
private fun GenericGameSession(game: GameEntry, level: GameLevel, repo: ProgressRepository, speak: ((String, String) -> Unit)?, onComplete: (Int) -> Unit) {
    val questions = 10
    var round by remember { mutableIntStateOf(0) }
    var score by remember { mutableIntStateOf(0) }
    var selected by remember { mutableStateOf<String?>(null) }
    var answered by remember { mutableStateOf(false) }
    val answer = remember(game.id, level, round) { genericQuestion(game, round, level) }

    Column(Modifier.fillMaxSize().padding(18.dp), horizontalAlignment = Alignment.CenterHorizontally) {
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            Text("الجولة ${round + 1} / $questions", fontWeight = FontWeight.Bold)
            Text("⭐ $score", fontWeight = FontWeight.Bold)
        }
        LinearProgressIndicator((round / questions.toFloat()).coerceIn(0f, 1f), Modifier.fillMaxWidth().padding(vertical = 12.dp))
        Spacer(Modifier.height(12.dp))
        Card(Modifier.fillMaxWidth().height(210.dp), shape = RoundedCornerShape(28.dp), colors = CardDefaults.cardColors(containerColor = Color.White), elevation = CardDefaults.cardElevation(5.dp)) {
            Column(Modifier.fillMaxSize().padding(18.dp), horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.Center) {
                Text(gameIcon(game.kind), fontSize = 58.sp)
                Spacer(Modifier.height(10.dp))
                Text(answer.first, fontSize = 24.sp, fontWeight = FontWeight.ExtraBold, textAlign = TextAlign.Center)
                if (game.kind == GameKind.LISTEN) FilledTonalButton(onClick = { speak?.invoke(answer.third, "ar") }) { Text("🔊 استمع مرة أخرى") }
            }
        }
        Spacer(Modifier.height(16.dp))
        LazyVerticalGrid(columns = GridCells.Fixed(if (answer.second.size > 4) 3 else 2), modifier = Modifier.weight(1f), horizontalArrangement = Arrangement.spacedBy(10.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
            items(answer.second) { option ->
                Card(Modifier.fillMaxWidth().height(68.dp).clickable(enabled = !answered) { selected = option; answered = true }, shape = RoundedCornerShape(18.dp), colors = CardDefaults.cardColors(containerColor = if (!answered || option == answer.fourth || option != selected) Color.White else Color(0xFFFFE8E8))) {
                    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) { Text(option, fontSize = 23.sp, fontWeight = FontWeight.ExtraBold) }
                }
            }
        }
        if (answered) {
            val correct = selected == answer.fourth
            Text(if (correct) "أحسنت! 🌟" else "الإجابة الصحيحة: ${answer.fourth}", fontWeight = FontWeight.Bold, fontSize = 17.sp)
            Button(onClick = {
                if (correct) { score++ ; speak?.invoke("أحسنت!", "ar") } else speak?.invoke("حاول مرة أخرى", "ar")
                if (round == questions - 1) {
                    val stars = when { score >= 8 -> 3; score >= 5 -> 2; else -> 1 }
                    repo.addStars(stars)
                    repo.recordLesson("الألعاب", game.title, true)
                    onComplete(stars)
                } else { round++; selected = null; answered = false }
            }, modifier = Modifier.fillMaxWidth().height(52.dp), shape = RoundedCornerShape(18.dp)) { Text(if (round == questions - 1) "إنهاء اللعبة" else "التالي ←") }
        }
    }
}

private fun genericQuestion(game: GameEntry, index: Int, level: GameLevel): Quad {
    val letters = listOf("أ","ب","ت","ث","ج","ح","خ","د","ر","س","ش","م")
    val answer = letters[(index * level.multiplier) % letters.size]
    val options = (listOf(answer) + letters.filter { it != answer }.shuffled().take(if (level == GameLevel.HARD) 5 else 3)).shuffled()
    return when (game.kind) {
        GameKind.NUMBER, GameKind.ORDER -> {
            val n = index + 1 + (level.multiplier - 1) * 10
            val opts = listOf(n, n + 1, n + 2, (n - 1).coerceAtLeast(1)).shuffled().map(Int::toString)
            Quad(if (game.kind == GameKind.ORDER) "اختر الرقم الأصغر" else "اعثر على الرقم $n", opts, "الرقم $n", n.toString())
        }
        GameKind.WORD, GameKind.BUILD -> Quad("أكمل الكلمة بالحرف الصحيح", options.mapIndexed { i, _ -> listOf("ا","ب","ت","م")[i % 4] }.distinct(), "أكمل الكلمة", "ا")
        GameKind.LISTEN -> Quad("استمع ثم اختر الحرف", options, "${answer}", answer)
        else -> Quad("أين الحرف «$answer»؟", options, "اختر الحرف $answer", answer)
    }
}

private data class Quad(val first: String, val second: List<String>, val third: String, val fourth: String)

@Composable
private fun GameFinished(game: GameEntry, stars: Int, onAgain: () -> Unit, onBack: () -> Unit) {
    Scaffold(containerColor = Color(0xFFF5F7FF), topBar = { TopAppBar(title = { Text("نتيجة اللعبة", fontWeight = FontWeight.ExtraBold) }, navigationIcon = { IconButton(onClick = onBack) { Icon(Icons.Default.ArrowBack, "رجوع") } }, colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.Transparent)) }) { padding ->
        Column(Modifier.fillMaxSize().padding(padding).padding(24.dp), horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.Center) {
            Text(if (stars >= 3) "🏆" else if (stars == 2) "🌟" else "💪", fontSize = 78.sp)
            Text(if (stars >= 3) "أداء رائع!" else if (stars == 2) "أحسنت!" else "محاولة جميلة!", fontSize = 30.sp, fontWeight = FontWeight.ExtraBold)
            Text(game.title, color = Color(0xFF667085), fontSize = 17.sp)
            Spacer(Modifier.height(16.dp))
            Text("⭐ $stars نجوم", fontSize = 25.sp, fontWeight = FontWeight.ExtraBold)
            Spacer(Modifier.height(24.dp))
            Button(onClick = onAgain, modifier = Modifier.fillMaxWidth().height(56.dp), shape = RoundedCornerShape(19.dp), colors = ButtonDefaults.buttonColors(containerColor = game.accent)) { Text("العب مرة أخرى", fontSize = 18.sp, fontWeight = FontWeight.Bold) }
            Spacer(Modifier.height(10.dp))
            OutlinedButton(onClick = onBack, modifier = Modifier.fillMaxWidth().height(52.dp), shape = RoundedCornerShape(19.dp)) { Text("العودة إلى الألعاب") }
        }
    }
}
