@file:OptIn(androidx.compose.material3.ExperimentalMaterial3Api::class)

package com.learnlettersnumbers.app

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.Canvas
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
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlin.math.cos
import kotlin.math.sin
import kotlin.random.Random

data class LearningGame(val id: String, val title: String, val subtitle: String, val accent: Color, val kind: GameKind)
enum class GameKind { LETTER, NUMBER, MEMORY, ORDER, WORD, LISTEN, SHAPE, COUNT, BUILD, MIXED }
enum class GameLevel(val label: String, val multiplier: Int) { EASY("سهل", 1), MEDIUM("متوسط", 2), HARD("متقدم", 3) }

private val games = listOf(
    LearningGame("match", "طابق الحرف", "اعثر على الحرف المطلوب", Color(0xFF4F8EF7), GameKind.LETTER),
    LearningGame("hunt", "صيد الأرقام", "التقط الرقم الصحيح", Color(0xFFFF8A4C), GameKind.NUMBER),
    LearningGame("memory", "ذاكرة الحروف", "اكشف وطابق الأزواج", Color(0xFF9B7EDE), GameKind.MEMORY),
    LearningGame("order", "رتّب الأرقام", "رتبها من الأصغر للأكبر", Color(0xFF35B779), GameKind.ORDER),
    LearningGame("missing", "الكلمة المفقودة", "أكمل الكلمة", Color(0xFFE96A8D), GameKind.WORD),
    LearningGame("listen", "اسمع واختر", "استمع ثم اختر", Color(0xFF0FA3B1), GameKind.LISTEN),
    LearningGame("shape", "شكل الحرف", "تعرف على الحرف", Color(0xFFF2B134), GameKind.SHAPE),
    LearningGame("count", "عدّ الأشياء", "عدّ ثم اختر العدد", Color(0xFF5E8CFF), GameKind.COUNT),
    LearningGame("build", "سباق الكلمات", "كوّن الكلمة", Color(0xFFEF6C3B), GameKind.BUILD),
    LearningGame("quick", "التحدي السريع", "مزيج من المهارات", Color(0xFF7A5AF8), GameKind.MIXED)
)

private data class GameQuestion(
    val prompt: String,
    val options: List<String>,
    val answer: String,
    val speech: String,
    val visualKind: GameKind
)

@Composable
fun GamesScreen(onBack: () -> Unit, repo: ProgressRepository, onSpeak: ((String, String) -> Unit)? = null) {
    var selected by remember { mutableStateOf<LearningGame?>(null) }
    AnimatedContent(
        targetState = selected,
        transitionSpec = { fadeIn(tween(180)) togetherWith fadeOut(tween(120)) },
        label = "games_navigation"
    ) { game ->
        if (game == null) GameLobby(onBack) { selected = it }
        else GamePlayScreen(game, { selected = null }, repo, onSpeak)
    }
}

@Composable
private fun GameLobby(onBack: () -> Unit, onGameClick: (LearningGame) -> Unit) {
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
        Column(
            Modifier.fillMaxSize().padding(padding).padding(horizontal = 16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            GameHero()
            Spacer(Modifier.height(12.dp))
            Text("هيا نلعب ونتعلم! 🌟", fontSize = 27.sp, fontWeight = FontWeight.ExtraBold)
            Text("اختر لعبة وابدأ مغامرة تعليمية جديدة", fontSize = 15.sp, color = Color(0xFF667085), textAlign = TextAlign.Center)
            Spacer(Modifier.height(18.dp))
            LazyVerticalGrid(
                columns = GridCells.Fixed(2),
                modifier = Modifier.fillMaxSize(),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp),
                contentPadding = PaddingValues(bottom = 24.dp)
            ) { items(games, key = { it.id }) { GameCard(it, onGameClick) } }
        }
    }
}

@Composable
private fun GameHero() {
    Card(
        Modifier.fillMaxWidth().height(138.dp),
        shape = RoundedCornerShape(30.dp),
        colors = CardDefaults.cardColors(containerColor = Color.Transparent),
        elevation = CardDefaults.cardElevation(6.dp)
    ) {
        Box(Modifier.fillMaxSize().background(Brush.linearGradient(listOf(Color(0xFF4F8EF7), Color(0xFF7A5AF8))))) {
            Canvas(Modifier.fillMaxSize()) {
                repeat(18) { i ->
                    drawCircle(Color.White.copy(.12f), 4f + (i % 4) * 2f, Offset(size.width * ((i * 61 % 100) / 100f), size.height * ((i * 37 % 100) / 100f)))
                }
            }
            Row(Modifier.fillMaxSize().padding(horizontal = 22.dp), verticalAlignment = Alignment.CenterVertically) {
                Canvas(Modifier.size(86.dp)) { drawGameIllustration(GameKind.MIXED, Color.White) }
                Spacer(Modifier.width(16.dp))
                Column {
                    Text("مدينة الألعاب", color = Color.White, fontSize = 24.sp, fontWeight = FontWeight.ExtraBold)
                    Text("تعلم • العب • اجمع النجوم ⭐", color = Color.White.copy(.92f), fontSize = 14.sp)
                }
            }
        }
    }
}

@Composable
private fun GameCard(game: LearningGame, onClick: (LearningGame) -> Unit) {
    var pressed by remember { mutableStateOf(false) }
    val scale by animateFloatAsState(if (pressed) .95f else 1f, tween(100), label = "game_card_scale")
    Card(
        Modifier.fillMaxWidth().height(190.dp).scale(scale).clickable {
            pressed = true
            onClick(game)
            pressed = false
        },
        shape = RoundedCornerShape(26.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(5.dp)
    ) {
        Column(Modifier.fillMaxSize().padding(12.dp), horizontalAlignment = Alignment.CenterHorizontally) {
            Box(Modifier.fillMaxWidth().height(110.dp).background(game.accent.copy(.13f), RoundedCornerShape(20.dp))) {
                Canvas(Modifier.fillMaxSize().padding(12.dp)) { drawGameIllustration(game.kind, game.accent) }
            }
            Spacer(Modifier.height(8.dp))
            Text(game.title, fontSize = 18.sp, fontWeight = FontWeight.ExtraBold, textAlign = TextAlign.Center)
            Text(game.subtitle, fontSize = 12.sp, color = Color(0xFF667085), textAlign = TextAlign.Center)
        }
    }
}

private fun DrawScope.drawGameIllustration(kind: GameKind, color: Color) {
    val w = size.width
    val h = size.height
    when (kind) {
        GameKind.LETTER, GameKind.SHAPE -> {
            drawRoundRect(color.copy(.16f), Offset(w * .20f, h * .08f), androidx.compose.ui.geometry.Size(w * .60f, h * .78f), CornerRadius(24f, 24f))
            drawCircle(color, h * .18f, Offset(w * .50f, h * .47f))
            drawCircle(Color.White, h * .10f, Offset(w * .44f, h * .44f))
            drawCircle(Color.White, h * .10f, Offset(w * .56f, h * .44f))
            drawCircle(color, h * .045f, Offset(w * .44f, h * .44f))
            drawCircle(color, h * .045f, Offset(w * .56f, h * .44f))
            drawArc(color, 20f, 140f, false, Offset(w * .39f, h * .47f), androidx.compose.ui.geometry.Size(w * .22f, h * .18f), style = Stroke(5f))
        }
        GameKind.NUMBER, GameKind.ORDER, GameKind.COUNT -> {
            for (i in 0..2) {
                val x = w * .28f + i * w * .22f
                drawRoundRect(color, Offset(x - 22f, h * .20f), androidx.compose.ui.geometry.Size(44f, 52f), CornerRadius(12f, 12f))
                drawCircle(Color.White, 6f, Offset(x, h * .46f))
            }
            drawLine(color, Offset(w * .18f, h * .78f), Offset(w * .82f, h * .78f), 7f, StrokeCap.Round)
            drawCircle(color, 8f, Offset(w * .50f, h * .78f))
        }
        GameKind.MEMORY -> {
            for (r in 0..1) for (c in 0..2) {
                val x = w * .25f + c * w * .25f
                val y = h * .30f + r * h * .30f
                drawRoundRect(color, Offset(x - 17f, y - 22f), androidx.compose.ui.geometry.Size(34f, 44f), CornerRadius(9f, 9f))
                drawCircle(Color.White.copy(.9f), 5f, Offset(x, y))
            }
        }
        GameKind.WORD, GameKind.BUILD -> {
            drawRoundRect(color, Offset(w * .10f, h * .25f), androidx.compose.ui.geometry.Size(w * .80f, h * .42f), CornerRadius(18f, 18f))
            for (i in 0..2) drawCircle(Color.White, 11f, Offset(w * (.30f + i * .20f), h * .46f))
            drawLine(color, Offset(w * .28f, h * .78f), Offset(w * .72f, h * .78f), 6f, StrokeCap.Round)
        }
        GameKind.LISTEN -> {
            drawCircle(color, 9f, Offset(w * .28f, h * .52f))
            drawLine(color, Offset(w * .31f, h * .52f), Offset(w * .55f, h * .38f), 11f, StrokeCap.Round)
            drawLine(color, Offset(w * .55f, h * .38f), Offset(w * .55f, h * .66f), 11f, StrokeCap.Round)
            drawArc(color, -55f, 110f, false, Offset(w * .54f, h * .27f), androidx.compose.ui.geometry.Size(w * .30f, h * .50f), style = Stroke(8f))
        }
        GameKind.MIXED -> {
            val cx = w * .5f
            val cy = h * .48f
            drawCircle(color, 22f, Offset(cx, cy))
            for (i in 0..7) {
                val a = i * (Math.PI / 4).toFloat()
                drawLine(Color.White, Offset(cx + sin(a) * 32f, cy + cos(a) * 32f), Offset(cx + sin(a) * 50f, cy + cos(a) * 50f), 7f, StrokeCap.Round)
            }
        }
    }
}

private fun questionFor(game: LearningGame, index: Int, level: GameLevel): GameQuestion {
    val random = Random(game.id.hashCode() * 37 + index * 101 + level.multiplier * 17)
    val letters = if (level == GameLevel.HARD)
        listOf("أ","ب","ت","ث","ج","ح","خ","د","ذ","ر","ز","س","ش","ص","ض","ط","ظ","ع","غ","ف","ق","ك","ل","م","ن","ه","و","ي")
    else listOf("أ","ب","ت","ث","ج","ح","خ","د","ر","س","ش","م")
    return when (game.kind) {
        GameKind.LETTER, GameKind.SHAPE, GameKind.MEMORY -> {
            val answer = letters[index % letters.size]
            val count = if (level == GameLevel.HARD) 6 else 4
            GameQuestion("أين الحرف «$answer»؟", (listOf(answer) + letters.shuffled(random).filter { it != answer }.take(count - 1)).shuffled(random), answer, "اختر الحرف $answer", game.kind)
        }
        GameKind.NUMBER -> {
            val max = when (level) { GameLevel.EASY -> 9; GameLevel.MEDIUM -> 20; GameLevel.HARD -> 50 }
            val answer = index % max + 1
            val values = (1..max).shuffled(random).filter { it != answer }.take(if (level == GameLevel.HARD) 5 else 3)
            GameQuestion("اعثر على الرقم $answer", (listOf(answer) + values).map(Int::toString).shuffled(random), answer.toString(), "اعثر على الرقم $answer", game.kind)
        }
        GameKind.ORDER -> {
            val count = if (level == GameLevel.HARD) 6 else 4
            val nums = (1..(10 + level.multiplier * 5)).shuffled(random).take(count)
            val answer = nums.min().toString()
            GameQuestion("ما الرقم الأصغر؟", nums.map(Int::toString), answer, "اختر الرقم الأصغر", game.kind)
        }
        GameKind.WORD -> {
            val items = listOf("ب_ب" to "ا", "ك_ب" to "ت", "ق_م" to "ل", "م_رس" to "د", "س_م" to "م", "ج_ل" to "م")
            val (word, answer) = items[index % items.size]
            GameQuestion("أكمل الكلمة: $word", listOf(answer, "ب", "ن", "ر").shuffled(random), answer, "أكمل الكلمة", game.kind)
        }
        GameKind.LISTEN -> {
            val items = listOf("ألف" to "أ", "باء" to "ب", "تاء" to "ت", "جيم" to "ج", "حاء" to "ح", "ميم" to "م")
            val (name, answer) = items[index % items.size]
            GameQuestion("استمع ثم اختر", listOf(answer, "د", "خ", "س").shuffled(random), answer, name, game.kind)
        }
        GameKind.COUNT -> {
            val count = index % (if (level == GameLevel.HARD) 10 else 6) + 2
            val alternatives = (2..12).shuffled(random).filter { it != count }.take(3)
            GameQuestion("كم عنصرًا ترى؟", (listOf(count) + alternatives).map(Int::toString).shuffled(random), count.toString(), "عد العناصر ثم اختر العدد $count", game.kind)
        }
        GameKind.BUILD -> {
            val items = listOf("ب + ا =" to "با", "م + ا =" to "ما", "د + ا =" to "دا", "ل + ا =" to "لا", "س + ا =" to "سا", "ت + ا =" to "تا")
            val (prompt, answer) = items[index % items.size]
            GameQuestion(prompt, listOf(answer, "بو", "مي", "دو").shuffled(random), answer, "كوّن $answer", game.kind)
        }
        GameKind.MIXED -> when (index % 4) {
            0 -> questionFor(games[0], index, level)
            1 -> questionFor(games[1], index + 2, level)
            2 -> questionFor(games[3], index + 4, level)
            else -> questionFor(games[4], index + 5, level)
        }
    }
}

@Composable
private fun GamePlayScreen(game: LearningGame, onBack: () -> Unit, repo: ProgressRepository, onSpeak: ((String, String) -> Unit)?) {
    var level by remember { mutableStateOf(GameLevel.EASY) }
    var started by remember { mutableStateOf(false) }
    var round by remember { mutableStateOf(0) }
    var score by remember { mutableStateOf(0) }
    var lives by remember { mutableStateOf(3) }
    var answered by remember { mutableStateOf(false) }
    var selected by remember { mutableStateOf<String?>(null) }
    var showResult by remember { mutableStateOf(false) }
    var sessionStars by remember { mutableStateOf(0) }

    if (!started) {
        GameStartScreen(game, level, { level = it }, { started = true }, onBack)
        return
    }

    if (showResult) {
        GameResultScreen(game, score, sessionStars, { started = false; showResult = false; round = 0; score = 0; lives = 3; sessionStars = 0 }, onBack)
        return
    }

    val question = remember(game.id, level, round) { questionFor(game, round, level) }
    val progress = (round / 10f).coerceIn(0f, 1f)

    LaunchedEffect(game.id, level, round) {
        if (game.kind == GameKind.LISTEN) onSpeak?.invoke(question.speech, "ar")
    }

    Scaffold(
        containerColor = Color(0xFFF6F8FF),
        topBar = {
            TopAppBar(
                title = { Text(game.title, fontWeight = FontWeight.ExtraBold) },
                navigationIcon = { IconButton(onClick = onBack) { Icon(Icons.Default.ArrowBack, "رجوع") } },
                actions = { Text("⭐ $sessionStars", fontWeight = FontWeight.Bold, modifier = Modifier.padding(end = 14.dp)) },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.Transparent)
            )
        }
    ) { padding ->
        Column(Modifier.fillMaxSize().padding(padding).padding(horizontal = 18.dp), horizontalAlignment = Alignment.CenterHorizontally) {
            Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                Box(Modifier.size(48.dp).background(game.accent.copy(.14f), CircleShape), contentAlignment = Alignment.Center) {
                    Canvas(Modifier.size(40.dp).padding(3.dp)) { drawGameIllustration(game.kind, game.accent) }
                }
                Spacer(Modifier.width(10.dp))
                Column(Modifier.weight(1f)) {
                    Text("الجولة ${round + 1} من 10", fontWeight = FontWeight.Bold)
                    LinearProgressIndicator(progress = progress, modifier = Modifier.fillMaxWidth().height(8.dp), color = game.accent, trackColor = game.accent.copy(.12f))
                }
                Spacer(Modifier.width(12.dp))
                Text("❤️".repeat(lives), fontSize = 15.sp)
            }

            Spacer(Modifier.height(20.dp))
            Card(Modifier.fillMaxWidth().height(210.dp), shape = RoundedCornerShape(30.dp), colors = CardDefaults.cardColors(containerColor = Color.White), elevation = CardDefaults.cardElevation(5.dp)) {
                Column(Modifier.fillMaxSize().padding(20.dp), horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.Center) {
                    if (game.kind == GameKind.COUNT) CountVisual(round % (if (level == GameLevel.HARD) 10 else 6) + 2, game.accent)
                    else Canvas(Modifier.size(72.dp)) { drawGameIllustration(question.visualKind, game.accent) }
                    Spacer(Modifier.height(12.dp))
                    Text(question.prompt, fontSize = 23.sp, fontWeight = FontWeight.ExtraBold, textAlign = TextAlign.Center)
                    if (game.kind == GameKind.LISTEN) {
                        Spacer(Modifier.height(6.dp))
                        FilledTonalButton(onClick = { onSpeak?.invoke(question.speech, "ar") }) { Icon(Icons.Default.PlayArrow, null); Spacer(Modifier.width(6.dp)); Text("استمع مرة أخرى") }
                    }
                }
            }

            Spacer(Modifier.height(18.dp))
            LazyVerticalGrid(columns = GridCells.Fixed(if (question.options.size > 4) 3 else 2), modifier = Modifier.weight(1f), horizontalArrangement = Arrangement.spacedBy(12.dp), verticalArrangement = Arrangement.spacedBy(12.dp), contentPadding = PaddingValues(bottom = 20.dp)) {
                items(question.options) { option ->
                    AnswerCard(option, option == question.answer, option == selected, answered, game.accent) {
                        if (!answered) {
                            selected = option
                            answered = true
                            val correct = option == question.answer
                            repo.recordAnswer(correct)
                            if (correct) {
                                val earned = if (level == GameLevel.HARD) 2 else 1
                                score += 10 * level.multiplier
                                sessionStars += earned
                                repo.addStars(earned)
                                if (round + 1 == 10) repo.recordLesson("الألعاب", game.title, true)
                                onSpeak?.invoke(if (level == GameLevel.HARD) "ممتاز! إجابة رائعة" else "أحسنت!", "ar")
                            } else {
                                lives -= 1
                                onSpeak?.invoke("حاول مرة أخرى", "ar")
                            }
                        }
                    }
                }
            }

            AnimatedVisibility(visible = answered, enter = fadeIn() + scaleIn()) {
                Button(
                    onClick = {
                        if (lives <= 0 || round >= 9) showResult = true
                        else { round++; answered = false; selected = null }
                    },
                    modifier = Modifier.fillMaxWidth().height(54.dp),
                    shape = RoundedCornerShape(18.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = game.accent)
                ) { Text(if (lives <= 0) "عرض النتيجة" else if (round >= 9) "إنهاء اللعبة" else "التالي ←", fontSize = 17.sp, fontWeight = FontWeight.Bold) }
            }
            Spacer(Modifier.height(10.dp))
        }
    }
}

@Composable
private fun GameStartScreen(game: LearningGame, level: GameLevel, onLevel: (GameLevel) -> Unit, onStart: () -> Unit, onBack: () -> Unit) {
    Scaffold(containerColor = Color(0xFFF5F7FF), topBar = { TopAppBar(title = { Text(game.title, fontWeight = FontWeight.ExtraBold) }, navigationIcon = { IconButton(onClick = onBack) { Icon(Icons.Default.ArrowBack, "رجوع") } }, colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.Transparent)) }) { padding ->
        Column(Modifier.fillMaxSize().padding(padding).padding(22.dp), horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.Center) {
            Box(Modifier.size(150.dp).background(game.accent.copy(.12f), CircleShape), contentAlignment = Alignment.Center) { Canvas(Modifier.size(110.dp)) { drawGameIllustration(game.kind, game.accent) } }
            Spacer(Modifier.height(22.dp))
            Text(game.title, fontSize = 30.sp, fontWeight = FontWeight.ExtraBold)
            Text(game.subtitle, fontSize = 16.sp, color = Color(0xFF667085), textAlign = TextAlign.Center)
            Spacer(Modifier.height(25.dp))
            Text("اختر مستوى التحدي", fontWeight = FontWeight.Bold, fontSize = 18.sp)
            Spacer(Modifier.height(10.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                GameLevel.values().forEach { item ->
                    FilterChip(selected = level == item, onClick = { onLevel(item) }, label = { Text(item.label) })
                }
            }
            Spacer(Modifier.height(26.dp))
            Card(Modifier.fillMaxWidth(), shape = RoundedCornerShape(22.dp), colors = CardDefaults.cardColors(containerColor = Color.White)) {
                Column(Modifier.padding(18.dp)) {
                    Text("🎯 10 جولات", fontWeight = FontWeight.Bold)
                    Text("❤️ 3 محاولات • ⭐ نجوم حقيقية تضاف لتقدمك", color = Color(0xFF667085), fontSize = 14.sp)
                }
            }
            Spacer(Modifier.height(24.dp))
            Button(onClick = onStart, modifier = Modifier.fillMaxWidth().height(58.dp), shape = RoundedCornerShape(20.dp), colors = ButtonDefaults.buttonColors(containerColor = game.accent)) { Text("ابدأ اللعب 🚀", fontSize = 19.sp, fontWeight = FontWeight.ExtraBold) }
        }
    }
}

@Composable
private fun AnswerCard(option: String, correct: Boolean, selected: Boolean, answered: Boolean, accent: Color, onClick: () -> Unit) {
    val background = when {
        !answered -> Color.White
        selected && correct -> Color(0xFFE7F8EE)
        selected && !correct -> Color(0xFFFFE8E8)
        answered && correct -> Color(0xFFE7F8EE)
        else -> Color.White
    }
    Card(Modifier.fillMaxWidth().height(72.dp).clickable(enabled = !answered, onClick = onClick), shape = RoundedCornerShape(20.dp), colors = CardDefaults.cardColors(containerColor = background), elevation = CardDefaults.cardElevation(3.dp)) {
        Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Text(option, fontSize = 24.sp, fontWeight = FontWeight.ExtraBold, textAlign = TextAlign.Center)
            if (answered && correct) Text("✓", modifier = Modifier.align(Alignment.CenterEnd).padding(end = 14.dp), fontSize = 22.sp, fontWeight = FontWeight.Bold)
            if (answered && selected && !correct) Text("✕", modifier = Modifier.align(Alignment.CenterEnd).padding(end = 14.dp), fontSize = 22.sp, fontWeight = FontWeight.Bold)
        }
    }
}

@Composable
private fun CountVisual(count: Int, accent: Color) {
    Canvas(Modifier.fillMaxWidth().height(62.dp)) {
        val columns = minOf(6, count)
        repeat(count) { i ->
            val x = size.width * ((i % columns + 1f) / (columns + 1f))
            val y = if (i / columns == 0) size.height * .35f else size.height * .75f
            drawCircle(accent, 12f, Offset(x, y))
            drawCircle(Color.White.copy(.55f), 4f, Offset(x - 3f, y - 3f))
        }
    }
}

@Composable
private fun GameResultScreen(game: LearningGame, score: Int, stars: Int, onAgain: () -> Unit, onBack: () -> Unit) {
    Scaffold(containerColor = Color(0xFFF5F7FF), topBar = { TopAppBar(title = { Text("نتيجة اللعبة", fontWeight = FontWeight.ExtraBold) }, navigationIcon = { IconButton(onClick = onBack) { Icon(Icons.Default.ArrowBack, "رجوع") } }, colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.Transparent)) }) { padding ->
        Column(Modifier.fillMaxSize().padding(padding).padding(24.dp), horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.Center) {
            Text(if (stars >= 7) "🏆" else if (stars >= 4) "🌟" else "💪", fontSize = 76.sp)
            Spacer(Modifier.height(12.dp))
            Text(if (stars >= 7) "أداء رائع!" else if (stars >= 4) "أحسنت!" else "محاولة جميلة!", fontSize = 30.sp, fontWeight = FontWeight.ExtraBold)
            Text(game.title, color = Color(0xFF667085), fontSize = 16.sp)
            Spacer(Modifier.height(22.dp))
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                ResultBox("النقاط", score.toString(), game.accent, Modifier.weight(1f))
                ResultBox("النجوم", "⭐ $stars", Color(0xFFFFB020), Modifier.weight(1f))
            }
            Spacer(Modifier.height(26.dp))
            Button(onClick = onAgain, modifier = Modifier.fillMaxWidth().height(56.dp), shape = RoundedCornerShape(19.dp), colors = ButtonDefaults.buttonColors(containerColor = game.accent)) { Text("العب مرة أخرى", fontSize = 18.sp, fontWeight = FontWeight.Bold) }
            Spacer(Modifier.height(10.dp))
            OutlinedButton(onClick = onBack, modifier = Modifier.fillMaxWidth().height(52.dp), shape = RoundedCornerShape(19.dp)) { Text("العودة إلى الألعاب") }
        }
    }
}

@Composable
private fun ResultBox(title: String, value: String, accent: Color, modifier: Modifier) {
    Card(modifier, shape = RoundedCornerShape(22.dp), colors = CardDefaults.cardColors(containerColor = Color.White)) {
        Column(Modifier.padding(vertical = 18.dp).fillMaxWidth(), horizontalAlignment = Alignment.CenterHorizontally) {
            Text(title, color = Color(0xFF667085), fontSize = 13.sp)
            Text(value, color = accent, fontSize = 25.sp, fontWeight = FontWeight.ExtraBold)
        }
    }
}
