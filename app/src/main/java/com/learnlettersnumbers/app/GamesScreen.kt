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
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.delay

private data class LearningGame(
    val id: String,
    val title: String,
    val subtitle: String,
    val icon: String,
    val color: Color,
    val category: String
)

private val games = listOf(
    LearningGame(
        "match",
        "صائد الحروف",
        "التقط الحرف المطلوب",
        "🔤",
        Color(0xFFFFD166),
        "حروف"
    ),
    LearningGame(
        "number",
        "صيد الأرقام",
        "اعثر على الرقم",
        "🎯",
        Color(0xFF8ED1FC),
        "أرقام"
    ),
    LearningGame(
        "memory",
        "ذاكرة الأبطال",
        "احفظ ثم طابق",
        "🧠",
        Color(0xFFCDB4DB),
        "ذاكرة"
    ),
    LearningGame(
        "sort",
        "سباق الترتيب",
        "اختر الأصغر",
        "🏁",
        Color(0xFFA8E6CF),
        "أرقام"
    ),
    LearningGame(
        "word",
        "الكلمة السحرية",
        "أكمل الكلمة",
        "🪄",
        Color(0xFFFFAAA5),
        "قراءة"
    ),
    LearningGame(
        "listen",
        "اسمع واربح",
        "استمع واختر",
        "🔊",
        Color(0xFFFFD6A5),
        "استماع"
    ),
    LearningGame(
        "count",
        "مزرعة الأعداد",
        "عد النجوم",
        "🌟",
        Color(0xFFCAFFBF),
        "أرقام"
    ),
    LearningGame(
        "build",
        "صانع الكلمات",
        "كوّن الكلمة",
        "🧩",
        Color(0xFFFFC8DD),
        "قراءة"
    ),
    LearningGame(
        "quick",
        "التحدي الذهبي",
        "تحديات متنوعة",
        "🏆",
        Color(0xFFFFE5B4),
        "متنوع"
    ),
    LearningGame(
        "shapes",
        "شكل الحرف",
        "تعرف على الحرف",
        "✏️",
        Color(0xFFBDE0FE),
        "حروف"
    )
)

@Composable
fun GamesScreen(
    onBack: () -> Unit,
    repo: ProgressRepository,
    onSpeak: ((String, String) -> Unit)? = null
) {
    var selectedGame by remember {
        mutableStateOf<LearningGame?>(null)
    }

    if (selectedGame == null) {
        GameHubScreen(
            onBack = onBack,
            onGameSelected = { selectedGame = it }
        )
    } else {
        ProfessionalGameScreen(
            game = selectedGame!!,
            onBack = { selectedGame = null },
            repo = repo,
            onSpeak = onSpeak
        )
    }
}

@Composable
private fun GameHubScreen(
    onBack: () -> Unit,
    onGameSelected: (LearningGame) -> Unit
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        "🎮 عالم الألعاب",
                        fontWeight = FontWeight.ExtraBold
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(
                            imageVector = Icons.Default.ArrowBack,
                            contentDescription = "رجوع"
                        )
                    }
                }
            )
        }
    ) { padding ->

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = 14.dp)
        ) {

            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 8.dp),
                shape = RoundedCornerShape(28.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer
                )
            ) {
                Column(
                    modifier = Modifier.padding(20.dp)
                ) {
                    Text(
                        "مستعد للتحدي؟ 🚀",
                        fontSize = 28.sp,
                        fontWeight = FontWeight.ExtraBold
                    )

                    Spacer(modifier = Modifier.height(6.dp))

                    Text(
                        "اختر لعبتك، اجمع النجوم، وابنِ سلسلة انتصاراتك!",
                        fontSize = 15.sp
                    )
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            Text(
                "الألعاب",
                fontSize = 22.sp,
                fontWeight = FontWeight.Bold
            )

            Spacer(modifier = Modifier.height(8.dp))

            LazyVerticalGrid(
                columns = GridCells.Fixed(2),
                contentPadding = PaddingValues(bottom = 20.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(
                    items = games,
                    key = { it.id }
                ) { game ->
                    GameCard(
                        game = game,
                        onClick = {
                            onGameSelected(game)
                        }
                    )
                }
            }
        }
    }
}

@Composable
private fun GameCard(
    game: LearningGame,
    onClick: () -> Unit
) {
    var pressed by remember {
        mutableStateOf(false)
    }

    val scale by animateFloatAsState(
        targetValue = if (pressed) 0.94f else 1f,
        animationSpec = tween(110),
        label = "gameCardScale"
    )

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .height(174.dp)
            .scale(scale)
            .clickable {
                pressed = true
                onClick()
                pressed = false
            },
        shape = RoundedCornerShape(26.dp),
        colors = CardDefaults.cardColors(
            containerColor = game.color
        ),
        elevation = CardDefaults.cardElevation(
            defaultElevation = 7.dp
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(13.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {

            Text(
                text = game.icon,
                fontSize = 45.sp
            )

            Spacer(modifier = Modifier.height(4.dp))

            Text(
                text = game.title,
                fontSize = 20.sp,
                fontWeight = FontWeight.ExtraBold
            )

            Text(
                text = game.subtitle,
                fontSize = 12.sp
            )

            Spacer(modifier = Modifier.height(6.dp))

            Surface(
                shape = RoundedCornerShape(50.dp),
                color = Color.White.copy(alpha = 0.45f)
            ) {
                Text(
                    text = game.category,
                    modifier = Modifier.padding(
                        horizontal = 10.dp,
                        vertical = 3.dp
                    ),
                    fontSize = 11.sp
                )
            }
        }
    }
}

private data class RoundQuestion(
    val prompt: String,
    val options: List<String>,
    val answer: String,
    val spoken: String = prompt
)

private fun questionFor(
    game: LearningGame,
    index: Int
): RoundQuestion {

    val letters = listOf(
        "أ", "ب", "ت", "ث", "ج", "ح", "خ",
        "د", "ذ", "ر", "ز", "س", "ش", "ص",
        "ض", "ط", "ظ", "ع", "غ", "ف", "ق",
        "ك", "ل", "م", "ن", "ه", "و", "ي"
    )

    val level = index / 3

    return when (game.id) {

        "match" -> {
            val answer = letters[index % letters.size]

            RoundQuestion(
                prompt = "التقط الحرف: $answer",
                options = listOf(
                    answer,
                    letters[(index + 3 + level) % letters.size],
                    letters[(index + 7 + level) % letters.size],
                    letters[(index + 11 + level) % letters.size]
                ).distinct().shuffled(),
                answer = answer
            )
        }

        "number" -> {
            val max = (9 + level * 5).coerceAtMost(99)
            val answer = (index * 7) % max + 1

            RoundQuestion(
                prompt = "اعثر على الرقم $answer",
                options = listOf(
                    answer,
                    answer + 2,
                    answer + 4,
                    (answer + 7).coerceAtMost(99)
                ).distinct().map {
                    it.toString()
                }.shuffled(),
                answer = answer.toString()
            )
        }

        "sort" -> {
            val base = index % (5 + level) + 1

            RoundQuestion(
                prompt = "ما الرقم الأصغر؟",
                options = listOf(
                    base + 3 + level,
                    base + 1,
                    base + 5 + level,
                    base
                ).map {
                    it.toString()
                }.distinct().shuffled(),
                answer = base.toString()
            )
        }

        "word" -> {
            val words = listOf(
                "ب_ب" to "ا",
                "ك_ب" to "ت",
                "ق_م" to "ل",
                "م_رس" to "د",
                "س_مك" to "م",
                "ك_تاب" to "ت"
            )

            val pair = words[index % words.size]

            RoundQuestion(
                prompt = "أكمل الكلمة: ${pair.first}",
                options = listOf(
                    pair.second,
                    "ب",
                    "ن",
                    "س"
                ).distinct().shuffled(),
                answer = pair.second
            )
        }

        "listen" -> {
            val sounds = listOf(
                "أ" to "ألف",
                "ب" to "باء",
                "ت" to "تاء",
                "ج" to "جيم",
                "م" to "ميم",
                "س" to "سين"
            )

            val pair = sounds[index % sounds.size]

            RoundQuestion(
                prompt = "استمع ثم اختر الحرف",
                options = listOf(
                    pair.first,
                    "د",
                    "ف",
                    "ك"
                ).distinct().shuffled(),
                answer = pair.first,
                spoken = pair.second
            )
        }

        "count" -> {
            val answer = index % (7 + level * 2) + 2

            RoundQuestion(
                prompt = "كم نجمة؟",
                options = listOf(
                    answer,
                    answer + 1,
                    answer - 1,
                    answer + 2
                ).distinct().map {
                    it.toString()
                }.shuffled(),
                answer = answer.toString()
            )
        }

        "build" -> {
            val words = listOf(
                "ب + ا" to "با",
                "م + ا" to "ما",
                "د + ا" to "دا",
                "ل + ا" to "لا",
                "س + ا" to "سا",
                "ك + ا" to "كا"
            )

            val pair = words[index % words.size]

            RoundQuestion(
                prompt = "كوّن: ${pair.first}",
                options = listOf(
                    pair.second,
                    "بو",
                    "مي",
                    "دو"
                ).distinct().shuffled(),
                answer = pair.second
            )
        }

        "memory" -> {
            val answer = letters[
                (index * 2 + level) % letters.size
            ]

            RoundQuestion(
                prompt = "احفظ الحرف ثم طابقه",
                options = listOf(
                    answer,
                    letters[(index + 4) % letters.size],
                    letters[(index + 9) % letters.size],
                    letters[(index + 13) % letters.size]
                ).distinct().shuffled(),
                answer = answer,
                spoken = answer
            )
        }

        "shapes" -> {
            val answer = letters[
                (index + 1 + level) % letters.size
            ]

            RoundQuestion(
                prompt = "أي حرف تراه؟",
                options = listOf(
                    answer,
                    letters[(index + 3 + level) % letters.size],
                    letters[(index + 6 + level) % letters.size],
                    letters[(index + 8 + level) % letters.size]
                ).distinct().shuffled(),
                answer = answer
            )
        }

        else -> {
            val answer = index % (8 + level * 2) + 1

            RoundQuestion(
                prompt = "$answer + ${level + 1} = ؟",
                options = listOf(
                    answer + level + 1,
                    answer,
                    answer + level + 2,
                    9 + level
                ).distinct().map {
                    it.toString()
                }.shuffled(),
                answer = (answer + level + 1).toString()
            )
        }
    }
}

@Composable
private fun ProfessionalGameScreen(
    game: LearningGame,
    onBack: () -> Unit,
    repo: ProgressRepository,
    onSpeak: ((String, String) -> Unit)?
) {

    var round by remember {
        mutableStateOf(0)
    }

    var score by remember {
        mutableStateOf(0)
    }

    var correctCount by remember {
        mutableStateOf(0)
    }

    var streak by remember {
        mutableStateOf(0)
    }

    var bestStreak by remember {
        mutableStateOf(0)
    }

    var lives by remember {
        mutableStateOf(3)
    }

    var timeLeft by remember {
        mutableStateOf(15)
    }

    var answered by remember {
        mutableStateOf(false)
    }

    var selected by remember {
        mutableStateOf<String?>(null)
    }

    var finished by remember {
        mutableStateOf(false)
    }

    var timedOut by remember {
        mutableStateOf(false)
    }

    var lastGained by remember {
        mutableStateOf(0)
    }

    var memoryReady by remember {
        mutableStateOf(game.id != "memory")
    }

    val totalQuestions = 10

    val question = questionFor(
        game = game,
        index = round
    )

    val progress = (
        round + if (answered) 1 else 0
    ).toFloat() / totalQuestions

    LaunchedEffect(
        round,
        answered,
        finished,
        game.id
    ) {

        memoryReady = game.id != "memory"

        if (
            game.id == "memory" &&
            !answered &&
            !finished
        ) {
            delay(1600)
            memoryReady = true
        }
    }

    LaunchedEffect(
        round,
        answered,
        finished
    ) {

        if (!answered && !finished) {

            timeLeft = (
                15 - round / 3
            ).coerceAtLeast(8)

            while (
                timeLeft > 0 &&
                !answered &&
                !finished
            ) {
                delay(1000)
                timeLeft--
            }

            if (
                timeLeft == 0 &&
                !answered &&
                !finished
            ) {

                timedOut = true
                answered = true
                selected = null
                lastGained = 0
                lives--
                streak = 0

                repo.recordAnswer(false)

                onSpeak?.invoke(
                    "انتهى الوقت، حاول مرة أخرى",
                    "ar"
                )
            }
        }
    }

    if (finished) {

        GameResultScreen(
            game = game,
            score = score,
            correctCount = correctCount,
            bestStreak = bestStreak,
            onBack = onBack,
            onReplay = {
                round = 0
                score = 0
                correctCount = 0
                streak = 0
                bestStreak = 0
                lives = 3
                timeLeft = 15
                answered = false
                selected = null
                timedOut = false
                lastGained = 0
                finished = false
            }
        )

        return
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        game.title,
                        fontWeight = FontWeight.Bold
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(
                            imageVector = Icons.Default.ArrowBack,
                            contentDescription = "رجوع"
                        )
                    }
                }
            )
        }
    ) { padding ->

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = 16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {

            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {

                Text(
                    "${round + 1}/$totalQuestions",
                    fontWeight = FontWeight.Bold
                )

                Spacer(modifier = Modifier.width(8.dp))

                LinearProgressIndicator(
                    progress = {
                        progress.coerceIn(0f, 1f)
                    },
                    modifier = Modifier
                        .weight(1f)
                        .height(9.dp)
                )

                Spacer(modifier = Modifier.width(8.dp))

                Text(
                    "⭐ $score",
                    fontWeight = FontWeight.Bold
                )
            }

            Spacer(modifier = Modifier.height(7.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {

                Text(
                    if (streak >= 2) {
                        "🔥 سلسلة ×$streak"
                    } else {
                        "ابدأ سلسلة!"
                    },
                    fontWeight = FontWeight.Bold
                )

                Text(
                    "⏱ $timeLeft",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.ExtraBold
                )

                Row {

                    repeat(3) { index ->

                        Icon(
                            imageVector = Icons.Default.Favorite,
                            contentDescription = null,
                            tint = if (index < lives) {
                                MaterialTheme.colorScheme.error
                            } else {
                                MaterialTheme.colorScheme.surfaceVariant
                            },
                            modifier = Modifier.size(22.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            if (game.id == "count") {
                CountChallenge(round)
            } else {
                GameQuestionPanel(
                    game = game,
                    question = question,
                    onSpeak = onSpeak,
                    memoryReady = memoryReady
                )
            }

            Spacer(modifier = Modifier.height(14.dp))

            question.options.forEach { option ->

                AnswerButton(
                    text = option,
                    enabled = !answered && memoryReady,
                    selected = selected == option,
                    correct = answered && option == question.answer,
                    wrong = answered &&
                        selected == option &&
                        option != question.answer,
                    onClick = {

                        if (answered) {
                            return@AnswerButton
                        }

                        selected = option
                        answered = true

                        val isCorrect =
                            option == question.answer

                        repo.recordAnswer(isCorrect)

                        if (isCorrect) {

                            correctCount++

                            val speedBonus =
                                if (timeLeft >= 10) {
                                    2
                                } else if (timeLeft >= 5) {
                                    1
                                } else {
                                    0
                                }

                            val comboBonus =
                                streak.coerceAtMost(4)

                            lastGained =
                                1 + comboBonus + speedBonus

                            score += lastGained

                            streak++

                            bestStreak =
                                maxOf(
                                    bestStreak,
                                    streak
                                )

                            repo.addStars(lastGained)

                            onSpeak?.invoke(
                                "أحسنت! إجابة صحيحة",
                                "ar"
                            )

                        } else {

                            lastGained = 0
                            lives--
                            streak = 0

                            onSpeak?.invoke(
                                "حاول مرة أخرى، أنت تستطيع",
                                "ar"
                            )
                        }
                    }
                )

                Spacer(
                    modifier = Modifier.height(7.dp)
                )
            }

            AnimatedVisibility(
                visible = answered,
                enter = fadeIn() + scaleIn(),
                exit = fadeOut() + scaleOut()
            ) {

                Column(
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {

                    Text(
                        text = when {
                            timedOut ->
                                "⏰ انتهى الوقت! الإجابة: ${question.answer}"

                            selected == question.answer ->
                                "رائع! +$lastGained ⭐"

                            else ->
                                "الإجابة الصحيحة: ${question.answer}"
                        },
                        fontSize = 17.sp,
                        fontWeight = FontWeight.ExtraBold
                    )

                    Spacer(
                        modifier = Modifier.height(7.dp)
                    )

                    Button(
                        onClick = {

                            timedOut = false

                            if (
                                round + 1 >= totalQuestions ||
                                lives <= 0
                            ) {
                                finished = true
                            } else {
                                round++
                                answered = false
                                selected = null
                                lastGained = 0
                            }
                        }
                    ) {

                        Text(
                            if (
                                round + 1 >= totalQuestions ||
                                lives <= 0
                            ) {
                                "عرض النتيجة 🏆"
                            } else {
                                "السؤال التالي ➜"
                            }
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun GameQuestionPanel(
    game: LearningGame,
    question: RoundQuestion,
    onSpeak: ((String, String) -> Unit)?,
    memoryReady: Boolean
) {

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .height(180.dp),
        shape = RoundedCornerShape(30.dp),
        colors = CardDefaults.cardColors(
            containerColor = game.color.copy(alpha = 0.78f)
        )
    ) {

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(18.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {

            if (
                game.id == "memory" &&
                !memoryReady
            ) {

                Text(
                    "👀",
                    fontSize = 48.sp
                )

                Text(
                    question.answer,
                    fontSize = 48.sp,
                    fontWeight = FontWeight.ExtraBold
                )

                Text(
                    "احفظه جيدًا...",
                    fontSize = 17.sp,
                    fontWeight = FontWeight.Bold
                )

            } else {

                Text(
                    game.icon,
                    fontSize = 42.sp
                )

                Text(
                    question.prompt,
                    fontSize = 23.sp,
                    fontWeight = FontWeight.ExtraBold
                )

                if (game.id == "listen") {

                    Spacer(
                        modifier = Modifier.height(5.dp)
                    )

                    FilledTonalButton(
                        onClick = {
                            onSpeak?.invoke(
                                question.spoken,
                                "ar"
                            )
                        }
                    ) {
                        Text("🔊 استمع")
                    }
                }
            }
        }
    }
}

@Composable
private fun CountChallenge(
    round: Int
) {

    val count = round % 7 + 2

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .height(180.dp),
        shape = RoundedCornerShape(30.dp),
        colors = CardDefaults.cardColors(
            containerColor = Color(0xFFCAFFBF)
                .copy(alpha = 0.8f)
        )
    ) {

        Column(
            modifier = Modifier.fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {

            Text(
                "عد النجوم",
                fontSize = 22.sp,
                fontWeight = FontWeight.Bold
            )

            Spacer(
                modifier = Modifier.height(8.dp)
            )

            Row {

                repeat(count) {
                    Text(
                        "⭐",
                        fontSize = 28.sp
                    )
                }
            }

            Spacer(
                modifier = Modifier.height(6.dp)
            )

            Text(
                "كم عددها؟",
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold
            )
        }
    }
}

@Composable
private fun AnswerButton(
    text: String,
    enabled: Boolean,
    selected: Boolean,
    correct: Boolean,
    wrong: Boolean,
    onClick: () -> Unit
) {

    val scale by animateFloatAsState(
        targetValue = if (selected) 0.97f else 1f,
        animationSpec = tween(120),
        label = "answerScale"
    )

    val backgroundColor = when {
        correct -> Color(0xFF8BE28B)
        wrong -> Color(0xFFFF9A9A)
        else -> MaterialTheme.colorScheme.surface
    }

    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .height(52.dp)
            .scale(scale)
            .clickable(
                enabled = enabled,
                onClick = onClick
            ),
        shape = RoundedCornerShape(18.dp),
        color = backgroundColor,
        tonalElevation = 3.dp,
        shadowElevation = 2.dp
    ) {

        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {

            Text(
                text = text,
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold
            )
        }
    }
}

@Composable
private fun GameResultScreen(
    game: LearningGame,
    score: Int,
    correctCount: Int,
    bestStreak: Int,
    onBack: () -> Unit,
    onReplay: () -> Unit
) {

    val accuracy = correctCount * 10

    val rank = when {
        accuracy >= 90 -> "أسطورة 🏆"
        accuracy >= 70 -> "بطل ⭐"
        accuracy >= 50 -> "ممتاز 👏"
        else -> "واصل التدريب 💪"
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {

        Text(
            "🏆",
            fontSize = 80.sp
        )

        Text(
            "انتهت اللعبة!",
            fontSize = 30.sp,
            fontWeight = FontWeight.ExtraBold
        )

        Text(
            game.title,
            fontSize = 21.sp
        )

        Spacer(
            modifier = Modifier.height(8.dp)
        )

        Text(
            rank,
            fontSize = 22.sp,
            fontWeight = FontWeight.ExtraBold
        )

        Text(
            "النتيجة: ⭐ $score",
            fontSize = 25.sp,
            fontWeight = FontWeight.Bold
        )

        Text(
            "الدقة: $accuracy%",
            fontSize = 18.sp
        )

        Text(
            "أفضل سلسلة: 🔥 $bestStreak",
            fontSize = 18.sp
        )

        Spacer(
            modifier = Modifier.height(20.dp)
        )

        Button(
            onClick = onReplay,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("العب مرة أخرى")
        }

        OutlinedButton(
            onClick = onBack,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("العودة للألعاب")
        }
    }
}
