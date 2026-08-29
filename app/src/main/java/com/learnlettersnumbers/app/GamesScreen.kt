@file:OptIn(androidx.compose.material3.ExperimentalMaterial3Api::class)

package com.learnlettersnumbers.app

import android.content.Context
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.delay
import kotlin.math.abs
import kotlin.math.roundToInt
import kotlin.random.Random

private data class LearningGame(val id: String, val title: String, val subtitle: String, val icon: String, val color: Color, val category: String)
private data class GameLevel(val number: Int, val title: String, val subtitle: String, val color: Color)

private val games = listOf(
    LearningGame("match", "صائد الحروف", "التقط الحرف المطلوب", "🔤", Color(0xFFFFD166), "حروف"),
    LearningGame("number", "صيد الأرقام", "اعثر على الرقم", "🎯", Color(0xFF8ED1FC), "أرقام"),
    LearningGame("memory", "ذاكرة الأبطال", "احفظ ثم طابق", "🧠", Color(0xFFCDB4DB), "ذاكرة"),
    LearningGame("sort", "سباق الترتيب", "اختر الأصغر", "🏁", Color(0xFFA8E6CF), "أرقام"),
    LearningGame("word", "الكلمة السحرية", "أكمل الكلمة", "🪄", Color(0xFFFFAAA5), "قراءة"),
    LearningGame("listen", "اسمع واربح", "استمع واختر", "🔊", Color(0xFFFFD6A5), "استماع"),
    LearningGame("count", "مزرعة الأعداد", "عد النجوم", "🌟", Color(0xFFCAFFBF), "أرقام"),
    LearningGame("build", "صانع الكلمات", "كوّن الكلمة", "🧩", Color(0xFFFFC8DD), "قراءة"),
    LearningGame("quick", "التحدي الذهبي", "تحديات متنوعة", "🏆", Color(0xFFFFE5B4), "متنوع"),
    LearningGame("shapes", "شكل الحرف", "تعرف على الحرف", "✏️", Color(0xFFBDE0FE), "حروف")
)

private val gameLevels = listOf(
    GameLevel(1, "المستوى السهل", "ابدأ وتعلّم بهدوء", Color(0xFFB8F2C8)),
    GameLevel(2, "المستوى المتوسط", "تحدٍ أكبر وسرعة أعلى", Color(0xFFFFE08A)),
    GameLevel(3, "المستوى الصعب", "لأبطال الألعاب فقط", Color(0xFFFFA6A6))
)

private const val GAMES_PREFS = "professional_games_progress_v2"
private const val TOTAL_ROUNDS = 10

private fun prefs(context: Context) = context.getSharedPreferences(GAMES_PREFS, Context.MODE_PRIVATE)
private fun bestScoreKey(game: LearningGame, level: Int) = "best_score_${game.id}_$level"
private fun bestAccuracyKey(game: LearningGame, level: Int) = "best_accuracy_${game.id}_$level"
private fun completedKey(game: LearningGame, level: Int) = "completed_${game.id}_$level"

private fun isLevelUnlocked(context: Context, game: LearningGame, level: Int): Boolean {
    if (level == 1) return true
    return prefs(context).getInt(bestAccuracyKey(game, level - 1), 0) >= 70
}

@Composable
fun GamesScreen(onBack: () -> Unit, repo: ProgressRepository, onSpeak: ((String, String) -> Unit)? = null) {
    var selected by remember { mutableStateOf<LearningGame?>(null) }
    var selectedLevel by remember { mutableStateOf<Int?>(null) }
    when {
        selected == null -> GameHubScreen(onBack) { selected = it }
        selectedLevel == null -> LevelSelectionScreen(selected!!, { selected = null }, { selectedLevel = it }, onSpeak)
        else -> ProfessionalGameScreen(selected!!, selectedLevel!!, { selectedLevel = null }, repo, onSpeak)
    }
}

@Composable
private fun GameHubScreen(onBack: () -> Unit, onGameSelected: (LearningGame) -> Unit) {
    val context = LocalContext.current
    Scaffold(topBar = {
        TopAppBar(title = { Text("🎮 عالم الألعاب", fontWeight = FontWeight.ExtraBold) }, navigationIcon = {
            IconButton(onClick = onBack) { Icon(Icons.Default.ArrowBack, "رجوع") }
        })
    }) { padding ->
        Column(Modifier.fillMaxSize().padding(padding).padding(horizontal = 14.dp)) {
            Card(Modifier.fillMaxWidth().padding(top = 8.dp), shape = RoundedCornerShape(28.dp), colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer)) {
                Column(Modifier.padding(20.dp)) {
                    Text("مستعد للتحدي؟ 🚀", fontSize = 28.sp, fontWeight = FontWeight.ExtraBold)
                    Spacer(Modifier.height(5.dp))
                    Text("اختر لعبتك، افتح المستويات، اجمع النجوم، وابنِ سلسلة انتصاراتك!", fontSize = 15.sp)
                    Spacer(Modifier.height(8.dp))
                    val completed = games.sumOf { game -> gameLevels.count { level -> prefs(context).getBoolean(completedKey(game, level.number), false) } }
                    Text("🏅 تقدمك: $completed / ${games.size * gameLevels.size} مراحل مكتملة", fontSize = 13.sp, fontWeight = FontWeight.Bold)
                }
            }
            Spacer(Modifier.height(14.dp))
            Text("الألعاب", fontSize = 22.sp, fontWeight = FontWeight.Bold)
            Spacer(Modifier.height(8.dp))
            LazyVerticalGrid(columns = GridCells.Fixed(2), contentPadding = PaddingValues(bottom = 20.dp), horizontalArrangement = Arrangement.spacedBy(12.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                items(games, key = { it.id }) { game -> GameCard(game) { onGameSelected(game) } }
            }
        }
    }
}

@Composable
private fun GameCard(game: LearningGame, onClick: () -> Unit) {
    var pressed by remember { mutableStateOf(false) }
    val scale by animateFloatAsState(if (pressed) .94f else 1f, tween(110), label = "gameCardScale")
    Card(Modifier.fillMaxWidth().height(174.dp).scale(scale).clickable { pressed = true; onClick(); pressed = false }, shape = RoundedCornerShape(26.dp), colors = CardDefaults.cardColors(containerColor = game.color), elevation = CardDefaults.cardElevation(7.dp)) {
        Column(Modifier.fillMaxSize().padding(13.dp), horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.Center) {
            Text(game.icon, fontSize = 45.sp)
            Spacer(Modifier.height(4.dp))
            Text(game.title, fontSize = 20.sp, fontWeight = FontWeight.ExtraBold)
            Text(game.subtitle, fontSize = 12.sp)
            Spacer(Modifier.height(6.dp))
            Surface(shape = RoundedCornerShape(50), color = Color.White.copy(alpha = .45f)) {
                Text(game.category, Modifier.padding(horizontal = 10.dp, vertical = 3.dp), fontSize = 11.sp)
            }
        }
    }
}

@Composable
private fun LevelSelectionScreen(game: LearningGame, onBack: () -> Unit, onLevelSelected: (Int) -> Unit, onSpeak: ((String, String) -> Unit)?) {
    val context = LocalContext.current
    Scaffold(topBar = {
        TopAppBar(title = { Text("${game.icon} ${game.title}", fontWeight = FontWeight.ExtraBold) }, navigationIcon = {
            IconButton(onClick = onBack) { Icon(Icons.Default.ArrowBack, "رجوع") }
        })
    }) { padding ->
        Column(Modifier.fillMaxSize().padding(padding).padding(18.dp), horizontalAlignment = Alignment.CenterHorizontally) {
            Text("اختر مستوى التحدي", fontSize = 27.sp, fontWeight = FontWeight.ExtraBold)
            Spacer(Modifier.height(6.dp))
            Text("يفتح المستوى التالي بعد تحقيق دقة 70% أو أكثر.", fontSize = 14.sp)
            Spacer(Modifier.height(18.dp))
            gameLevels.forEach { level ->
                val unlocked = isLevelUnlocked(context, game, level.number)
                val best = prefs(context).getInt(bestAccuracyKey(game, level.number), 0)
                val scale by animateFloatAsState(if (unlocked) 1f else .97f, tween(140), label = "levelScale${level.number}")
                Card(Modifier.fillMaxWidth().padding(vertical = 6.dp).scale(scale).clickable(enabled = unlocked) {
                    onSpeak?.invoke("${level.title}. هيا نبدأ!", "ar")
                    onLevelSelected(level.number)
                }, shape = RoundedCornerShape(24.dp), colors = CardDefaults.cardColors(containerColor = if (unlocked) level.color else MaterialTheme.colorScheme.surfaceVariant), elevation = CardDefaults.cardElevation(if (unlocked) 6.dp else 1.dp)) {
                    Row(Modifier.fillMaxWidth().padding(17.dp), verticalAlignment = Alignment.CenterVertically) {
                        Text(if (unlocked) "🎮" else "🔒", fontSize = 34.sp)
                        Spacer(Modifier.width(14.dp))
                        Column(Modifier.weight(1f)) {
                            Text(level.title, fontSize = 20.sp, fontWeight = FontWeight.ExtraBold)
                            Text(if (unlocked) level.subtitle else "أكمل المستوى السابق بدقة 70%", fontSize = 13.sp)
                            if (best > 0) Text("أفضل دقة: $best%", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                        }
                        Text(if (unlocked) "ابدأ ➜" else "مغلق", fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}

private data class RoundQuestion(val prompt: String, val options: List<String>, val answer: String, val spoken: String = prompt)

private fun questionFor(game: LearningGame, index: Int, level: Int, seed: Int): RoundQuestion {
    val letters = listOf("أ","ب","ت","ث","ج","ح","خ","د","ذ","ر","ز","س","ش","ص","ض","ط","ظ","ع","غ","ف","ق","ك","ل","م","ن","ه","و","ي")
    val random = Random(seed + index * 7919 + level * 104729)
    val difficulty = level - 1
    fun otherLetter(except: String): String = letters.filter { it != except }.shuffled(random).first()
    return when (game.id) {
        "match" -> {
            val a = letters[(index * (2 + level) + abs(seed)) % letters.size]
            RoundQuestion("التقط الحرف: $a", (listOf(a) + List(3) { otherLetter(a) }).distinct().shuffled(random), a)
        }
        "number" -> {
            val max = when (level) { 1 -> 20; 2 -> 50; else -> 100 }
            val a = random.nextInt(1, max + 1)
            val wrong = listOf((a + level + 1).coerceAtMost(max), (a + level + 4).coerceAtMost(max), (a - level - 2).coerceAtLeast(1))
            RoundQuestion("اعثر على الرقم $a", (listOf(a) + wrong).distinct().map(Int::toString).shuffled(random), a.toString())
        }
        "sort" -> {
            val max = 6 + level * 3
            val options = (List(5) { random.nextInt(1, max + 1) }).distinct().take(4)
            val safe = if (options.size >= 4) options else (options + List(4 - options.size) { random.nextInt(1, max + 1) }).distinct().take(4)
            val answer = safe.minOrNull() ?: 1
            RoundQuestion("ما الرقم الأصغر؟", safe.map(Int::toString).shuffled(random), answer.toString())
        }
        "word" -> {
            val data = listOf("ب_ب" to "ا", "ك_ب" to "ت", "ق_م" to "ل", "م_رس" to "د", "س_مك" to "م", "ك_تاب" to "ت", "ج_ل" to "م", "ن_ر" to "ه")
            val (pattern, answer) = data[(index + difficulty + abs(seed)) % data.size]
            RoundQuestion("أكمل الكلمة: $pattern", (listOf(answer) + listOf("ب","ن","س","د").filter { it != answer }.shuffled(random).take(3)).shuffled(random), answer)
        }
        "listen" -> {
            val data = listOf("أ" to "ألف", "ب" to "باء", "ت" to "تاء", "ج" to "جيم", "م" to "ميم", "س" to "سين", "ل" to "لام", "ن" to "نون")
            val (answer, spoken) = data[(index + abs(seed) + difficulty) % data.size]
            RoundQuestion("استمع ثم اختر الحرف", (listOf(answer) + List(3) { otherLetter(answer) }).distinct().shuffled(random), answer, spoken)
        }
        "count" -> {
            val max = 7 + level * 3
            val a = random.nextInt(2, max + 1)
            RoundQuestion("كم نجمة؟", listOf(a, a + 1, (a - 1).coerceAtLeast(1), a + 2).distinct().map(Int::toString).shuffled(random), a.toString())
        }
        "build" -> {
            val data = listOf("ب + ا" to "با", "م + ا" to "ما", "د + ا" to "دا", "ل + ا" to "لا", "س + ا" to "سا", "ك + ا" to "كا", "ر + ا" to "را", "ن + ا" to "نا")
            val (prompt, answer) = data[(index + abs(seed) + difficulty) % data.size]
            RoundQuestion("كوّن: $prompt", (listOf(answer) + listOf("بو","مي","دو","سو").filter { it != answer }.shuffled(random).take(3)).shuffled(random), answer)
        }
        "memory" -> {
            val a = letters[(index * (2 + level) + abs(seed)) % letters.size]
            RoundQuestion("احفظ الحرف ثم طابقه", (listOf(a) + List(3) { otherLetter(a) }).distinct().shuffled(random), a, a)
        }
        "shapes" -> {
            val a = letters[(index + level + abs(seed)) % letters.size]
            RoundQuestion("أي حرف تراه؟", (listOf(a) + List(3) { otherLetter(a) }).distinct().shuffled(random), a)
        }
        else -> {
            val max = when (level) { 1 -> 10; 2 -> 30; else -> 60 }
            val a = random.nextInt(1, max + 1)
            val b = random.nextInt(1, level + 3)
            RoundQuestion("$a + $b = ؟", listOf(a + b, a, a + b + 1, (a + b - 1).coerceAtLeast(0)).distinct().map(Int::toString).shuffled(random), (a + b).toString())
        }
    }
}

@Composable
private fun ProfessionalGameScreen(game: LearningGame, level: Int, onBack: () -> Unit, repo: ProgressRepository, onSpeak: ((String, String) -> Unit)?) {
    var round by remember { mutableStateOf(0) }
    var score by remember { mutableStateOf(0) }
    var correctCount by remember { mutableStateOf(0) }
    var streak by remember { mutableStateOf(0) }
    var bestStreak by remember { mutableStateOf(0) }
    var lives by remember { mutableStateOf(3) }
    var timeLeft by remember { mutableStateOf(if (level == 1) 18 else if (level == 2) 14 else 11) }
    var answered by remember { mutableStateOf(false) }
    var selected by remember { mutableStateOf<String?>(null) }
    var finished by remember { mutableStateOf(false) }
    var timedOut by remember { mutableStateOf(false) }
    var lastGained by remember { mutableStateOf(0) }
    var memoryReady by remember { mutableStateOf(game.id != "memory") }
    val seed = remember { Random.nextInt() }
    val context = LocalContext.current
    val total = TOTAL_ROUNDS
    val question = remember(game.id, level, round, seed) { questionFor(game, round, level, seed) }
    val progress = ((round + if (answered) 1 else 0).toFloat() / total.toFloat()).coerceIn(0f, 1f)
    val timeLimit = when (level) { 1 -> 18; 2 -> 14; else -> 11 }

    LaunchedEffect(round, answered, finished, game.id, level) {
        memoryReady = game.id != "memory"
        if (game.id == "memory" && !answered && !finished) {
            delay(if (level == 1) 1800 else if (level == 2) 1500 else 1200)
            memoryReady = true
        }
    }

    LaunchedEffect(round, answered, finished, level) {
        if (!answered && !finished) {
            timeLeft = timeLimit
            while (timeLeft > 0 && !answered && !finished) {
                delay(1000)
                timeLeft--
            }
            if (timeLeft == 0 && !answered && !finished) {
                timedOut = true
                answered = true
                selected = null
                lastGained = 0
                lives--
                streak = 0
                repo.recordAnswer(false)
                onSpeak?.invoke("انتهى الوقت، حاول مرة أخرى", "ar")
            }
        }
    }

    if (finished) {
        val accuracy = (correctCount * 100f / total).roundToInt()
        val completionBonus = if (correctCount > 0) 10 + level * 5 else 0
        val finalScore = score + completionBonus
        val p = prefs(context)
        val oldBest = p.getInt(bestScoreKey(game, level), 0)
        val oldAccuracy = p.getInt(bestAccuracyKey(game, level), 0)
        LaunchedEffect(game.id, level, finalScore, accuracy) {
            p.edit()
                .putInt(bestScoreKey(game, level), maxOf(oldBest, finalScore))
                .putInt(bestAccuracyKey(game, level), maxOf(oldAccuracy, accuracy))
                .putBoolean(completedKey(game, level), true)
                .apply()
        }
        GameResultScreen(game, level, finalScore, correctCount, accuracy, bestStreak, completionBonus, maxOf(oldBest, finalScore), onBack) {
            round = 0
            score = 0
            correctCount = 0
            streak = 0
            bestStreak = 0
            lives = 3
            timeLeft = timeLimit
            answered = false
            selected = null
            timedOut = false
            lastGained = 0
            finished = false
        }
        return
    }

    Scaffold(topBar = {
        TopAppBar(title = { Text("${game.icon} ${game.title} • مستوى $level", fontWeight = FontWeight.Bold) }, navigationIcon = {
            IconButton(onClick = onBack) { Icon(Icons.Default.ArrowBack, "رجوع") }
        })
    }) { padding ->
        Column(Modifier.fillMaxSize().padding(padding).padding(horizontal = 16.dp), horizontalAlignment = Alignment.CenterHorizontally) {
            Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                Text("${round + 1}/$total", fontWeight = FontWeight.Bold)
                Spacer(Modifier.width(8.dp))
                LinearProgressIndicator(progress = { progress }, Modifier.weight(1f).height(9.dp))
                Spacer(Modifier.width(8.dp))
                Text("⭐ $score", fontWeight = FontWeight.Bold)
            }
            Spacer(Modifier.height(7.dp))
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                Text(if (streak >= 2) "🔥 سلسلة ×$streak" else "ابدأ سلسلة!", fontWeight = FontWeight.Bold)
                Text("⏱ $timeLeft", fontSize = 18.sp, fontWeight = FontWeight.ExtraBold)
                Row { repeat(3) { i -> Icon(Icons.Default.Favorite, null, tint = if (i < lives) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.surfaceVariant, modifier = Modifier.size(22.dp)) } }
            }
            Spacer(Modifier.height(8.dp))
            if (game.id == "count") CountChallenge(round, level) else GameQuestionPanel(game, question, onSpeak, memoryReady)
            Spacer(Modifier.height(14.dp))
            question.options.forEach { option ->
                AnswerButton(option, !answered && memoryReady, selected == option, answered && option == question.answer, answered && selected == option && option != question.answer) {
                    selected = option
                    answered = true
                    val correct = option == question.answer
                    repo.recordAnswer(correct)
                    if (correct) {
                        correctCount++
                        val speedBonus = if (timeLeft >= timeLimit * 0.65f) 2 else if (timeLeft >= timeLimit * 0.35f) 1 else 0
                        val comboBonus = streak.coerceAtMost(4)
                        lastGained = 1 + comboBonus + speedBonus + (level - 1)
                        score += lastGained
                        streak++
                        bestStreak = maxOf(bestStreak, streak)
                        repo.addStars(lastGained)
                        onSpeak?.invoke("أحسنت! إجابة صحيحة", "ar")
                    } else {
                        lastGained = 0
                        lives--
                        streak = 0
                        onSpeak?.invoke("حاول مرة أخرى، أنت تستطيع", "ar")
                    }
                }
                Spacer(Modifier.height(7.dp))
            }
            AnimatedVisibility(answered, enter = fadeIn() + scaleIn(), exit = fadeOut() + scaleOut()) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(if (timedOut) "⏰ انتهى الوقت! الإجابة: ${question.answer}" else if (selected == question.answer) "رائع! +$lastGained ⭐" else "الإجابة الصحيحة: ${question.answer}", fontSize = 17.sp, fontWeight = FontWeight.ExtraBold)
                    Spacer(Modifier.height(7.dp))
                    Button(onClick = {
                        timedOut = false
                        if (round + 1 >= total || lives <= 0) finished = true else {
                            round++
                            answered = false
                            selected = null
                            lastGained = 0
                        }
                    }) { Text(if (round + 1 >= total || lives <= 0) "عرض النتيجة 🏆" else "السؤال التالي ➜") }
                }
            }
        }
    }
}

@Composable
private fun GameQuestionPanel(game: LearningGame, question: RoundQuestion, onSpeak: ((String, String) -> Unit)?, memoryReady: Boolean) {
    Card(Modifier.fillMaxWidth().height(180.dp), shape = RoundedCornerShape(30.dp), colors = CardDefaults.cardColors(containerColor = game.color.copy(alpha = .78f))) {
        Column(Modifier.fillMaxSize().padding(18.dp), horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.Center) {
            if (game.id == "memory" && !memoryReady) {
                Text("👀", fontSize = 48.sp)
                Text(question.answer, fontSize = 48.sp, fontWeight = FontWeight.ExtraBold)
                Text("احفظه جيدًا...", fontSize = 17.sp, fontWeight = FontWeight.Bold)
            } else {
                Text(game.icon, fontSize = 42.sp)
                Text(question.prompt, fontSize = 23.sp, fontWeight = FontWeight.ExtraBold)
                if (game.id == "listen") {
                    Spacer(Modifier.height(5.dp))
                    FilledTonalButton(onClick = { onSpeak?.invoke(question.spoken, "ar") }) { Text("🔊 استمع") }
                }
            }
        }
    }
}

@Composable
private fun CountChallenge(round: Int, level: Int) {
    val count = round % (7 + level * 2) + 2
    Card(Modifier.fillMaxWidth().height(180.dp), shape = RoundedCornerShape(30.dp), colors = CardDefaults.cardColors(containerColor = Color(0xFFCAFFBF).copy(alpha = .8f))) {
        Column(Modifier.fillMaxSize(), horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.Center) {
            Text("عد النجوم", fontSize = 22.sp, fontWeight = FontWeight.Bold)
            Spacer(Modifier.height(8.dp))
            Row { repeat(count) { Text("⭐", fontSize = if (level == 3) 24.sp else 28.sp) } }
            Spacer(Modifier.height(6.dp))
            Text("كم عددها؟", fontSize = 18.sp, fontWeight = FontWeight.Bold)
        }
    }
}

@Composable
private fun AnswerButton(text: String, enabled: Boolean, selected: Boolean, correct: Boolean, wrong: Boolean, onClick: () -> Unit) {
    val scale by animateFloatAsState(if (selected) .97f else 1f, tween(120), label = "answerScale")
    val bg = when { correct -> Color(0xFF8BE28B); wrong -> Color(0xFFFF9A9A); else -> MaterialTheme.colorScheme.surface }
    Surface(Modifier.fillMaxWidth().height(52.dp).scale(scale).clickable(enabled = enabled, onClick = onClick), shape = RoundedCornerShape(18.dp), color = bg, tonalElevation = 3.dp, shadowElevation = 2.dp) {
        Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) { Text(text, fontSize = 20.sp, fontWeight = FontWeight.Bold) }
    }
}

@Composable
private fun GameResultScreen(game: LearningGame, level: Int, score: Int, correctCount: Int, accuracy: Int, bestStreak: Int, bonus: Int, bestScore: Int, onBack: () -> Unit, onReplay: () -> Unit) {
    val rank = when { accuracy >= 90 -> "أسطورة 🏆"; accuracy >= 70 -> "بطل ⭐"; accuracy >= 50 -> "ممتاز 👏"; else -> "واصل التدريب 💪" }
    Column(Modifier.fillMaxSize().padding(24.dp), horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.Center) {
        Text("🏆", fontSize = 80.sp)
        Text("انتهت اللعبة!", fontSize = 30.sp, fontWeight = FontWeight.ExtraBold)
        Text("${game.title} • المستوى $level", fontSize = 21.sp)
        Spacer(Modifier.height(8.dp))
        Text(rank, fontSize = 22.sp, fontWeight = FontWeight.ExtraBold)
        Text("النتيجة: ⭐ $score", fontSize = 25.sp, fontWeight = FontWeight.Bold)
        Text("أفضل نتيجة: ⭐ $bestScore", fontSize = 17.sp)
        Text("الدقة: $accuracy%", fontSize = 18.sp)
        Text("أفضل سلسلة: 🔥 $bestStreak", fontSize = 18.sp)
        Text("مكافأة الإنهاء: +$bonus ⭐", fontSize = 16.sp, fontWeight = FontWeight.Bold)
        Spacer(Modifier.height(20.dp))
        Button(onClick = onReplay, modifier = Modifier.fillMaxWidth()) { Text("العب مرة أخرى") }
        OutlinedButton(onClick = onBack, modifier = Modifier.fillMaxWidth()) { Text("العودة للمستويات") }
    }
}
