package com.learnlettersnumbers.app

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

data class LearningGame(
    val title: String,
    val subtitle: String,
    val icon: String,
    val color: Color
)

private val games = listOf(
    LearningGame("طابق الحرف", "اختر الحرف المطابق", "🔤", Color(0xFFFFD166)),
    LearningGame("صيد الأرقام", "اعثر على الرقم المطلوب", "🔢", Color(0xFF8ED1FC)),
    LearningGame("ذاكرة الحروف", "طابق الأزواج", "🧠", Color(0xFFCDB4DB)),
    LearningGame("رتب الأرقام", "رتب من الأصغر للأكبر", "📊", Color(0xFFA8E6CF)),
    LearningGame("الكلمة المفقودة", "أكمل الكلمة", "🧩", Color(0xFFFFAAA5)),
    LearningGame("اسمع واختر", "استمع واختر الإجابة", "🔊", Color(0xFFFFD6A5)),
    LearningGame("شكل الحرف", "تعرف على شكل الحرف", "✏️", Color(0xFFBDE0FE)),
    LearningGame("عد الأشياء", "عد الصور واختر العدد", "🌸", Color(0xFFCAFFBF)),
    LearningGame("سباق الكلمات", "كوّن الكلمة", "🏁", Color(0xFFFFC8DD)),
    LearningGame("التحدي السريع", "أسئلة متنوعة", "🏆", Color(0xFFFFE5B4))
)

@Composable
fun GamesScreen(
    onBack: () -> Unit,
    repo: ProgressRepository,
    onSpeak: ((String, String) -> Unit)? = null
) {
    var selected by remember { mutableStateOf<LearningGame?>(null) }

    if (selected == null) {
        Scaffold(
            topBar = {
                TopAppBar(
                    title = { Text("🎮 الألعاب", fontWeight = FontWeight.Bold) },
                    navigationIcon = {
                        IconButton(onClick = onBack) {
                            onBack()
                        }
                    }
                )
            }
        ) { padding ->
            Column(
                Modifier.fillMaxSize().padding(padding).padding(16.dp)
            ) {
                Text("هيا نلعب ونتعلم! 🌟", fontSize = 28.sp, fontWeight = FontWeight.Bold)
                Text("ألعاب تعليمية وترفيهية تعمل بدون إنترنت.", fontSize = 16.sp)
                Spacer(Modifier.height(16.dp))
                LazyVerticalGrid(
                    columns = GridCells.Fixed(2),
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(games) { game ->
                        Card(
                            Modifier.fillMaxWidth().height(170.dp).clickable {
                                selected = game
                                onSpeak?.invoke("هيا نلعب لعبة ${game.title}", "ar")
                            },
                            shape = RoundedCornerShape(24.dp),
                            colors = CardDefaults.cardColors(containerColor = game.color),
                            elevation = CardDefaults.cardElevation(8.dp)
                        ) {
                            Column(
                                Modifier.fillMaxSize().padding(14.dp),
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.Center
                            ) {
                                Text(game.icon, fontSize = 48.sp)
                                Text(game.title, fontSize = 21.sp, fontWeight = FontWeight.Bold)
                                Text(game.subtitle, fontSize = 13.sp)
                            }
                        }
                    }
                }
            }
        }
    } else {
        GamePlayScreen(
            game = selected!!,
            onBack = { selected = null },
            repo = repo,
            onSpeak = onSpeak
        )
    }
}

private data class GameQuestion(val prompt: String, val options: List<String>, val answer: String)

private fun makeGameQuestion(game: LearningGame, index: Int): GameQuestion {
    return when (game.title) {
        "طابق الحرف" -> {
            val letters = listOf("أ", "ب", "ت", "ث", "ج", "ح", "خ", "د")
            val a = letters[index % letters.size]
            GameQuestion("اختر الحرف: $a", listOf(a, letters[(index+1)%8], letters[(index+3)%8], letters[(index+5)%8]), a)
        }
        "صيد الأرقام" -> {
            val a = ((index * 3) % 9 + 1).toString()
            GameQuestion("اعثر على الرقم $a", listOf(a, ((index+2)%9+1).toString(), ((index+4)%9+1).toString(), ((index+6)%9+1).toString()), a)
        }
        "ذاكرة الحروف" -> {
            val letters = listOf("أ", "ب", "ج", "د")
            val a = letters[index % 4]
            GameQuestion("طابق الحرف مع مثيله: $a", letters.shuffled(java.util.Random(index.toLong())), a)
        }
        "رتب الأرقام" -> GameQuestion("ما الرقم الأصغر؟", listOf("2","5","1","4"), "1")
        "الكلمة المفقودة" -> {
            val items = listOf("ب_ب" to "ا", "ك_ب" to "ت", "ق_م" to "ل", "م_رس" to "د")
            val (word, a) = items[index % items.size]
            GameQuestion("أكمل الكلمة: $word", listOf(a,"ب","م","ن"), a)
        }
        "اسمع واختر" -> {
            val items = listOf("ألف" to "أ", "باء" to "ب", "تاء" to "ت", "جيم" to "ج")
            val (word,a)=items[index%items.size]
            GameQuestion("استمع ثم اختر: $word", listOf(a,"د","خ","س"), a)
        }
        "شكل الحرف" -> {
            val a = listOf("أ","ب","ج","د")[index%4]
            GameQuestion("أي شكل يطابق الحرف؟ $a", listOf(a,"م","س","ر"), a)
        }
        "عد الأشياء" -> {
            val a = ((index%5)+1).toString()
            GameQuestion("كم نجمة؟ ⭐ × $a", listOf(a,"2","4","6"), a)
        }
        "سباق الكلمات" -> {
            val items=listOf("ب + ا =" to "با", "م + ا =" to "ما", "د + ا =" to "دا", "ل + ا =" to "لا")
            val (q,a)=items[index%items.size]
            GameQuestion(q, listOf(a,"بو","مي","دو"),a)
        }
        else -> {
            val a=(index%4+1).toString()
            GameQuestion("اختر الإجابة الصحيحة: $a + 1 = ?", listOf((a.toInt()+1).toString(),"3","5","6"),(a.toInt()+1).toString())
        }
    }
}

@Composable
private fun GamePlayScreen(
    game: LearningGame,
    onBack: () -> Unit,
    repo: ProgressRepository,
    onSpeak: ((String, String) -> Unit)?
) {
    var questionIndex by remember { mutableStateOf(0) }
    var score by remember { mutableStateOf(0) }
    var answered by remember { mutableStateOf(false) }
    val question = makeGameQuestion(game, questionIndex)

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(game.title) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        onBack()
                    }
                }
            )
        }
    ) { padding ->
        Column(
            Modifier.fillMaxSize().padding(padding).padding(20.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(game.icon, fontSize = 72.sp)
            Text(question.prompt, fontSize = 26.sp, fontWeight = FontWeight.Bold)
            Spacer(Modifier.height(18.dp))

            Button(onClick = { onSpeak?.invoke(question.prompt, "ar") }) {
                Text("🔊 اسمع السؤال")
            }

            Spacer(Modifier.height(14.dp))
            question.options.forEach { answer ->
                Button(
                    Modifier.fillMaxWidth().padding(vertical = 4.dp),
                    enabled = !answered,
                    onClick = {
                        answered = true
                        val correct = answer == question.answer
                        repo.recordAnswer(correct)
                        if (correct) {
                            score++
                            repo.addStars(1)
                            onSpeak?.invoke("أحسنت! إجابة رائعة ⭐", "ar")
                        } else {
                            onSpeak?.invoke("حاول مرة أخرى، أنت تستطيع 💪", "ar")
                        }
                    }
                ) { Text(answer, fontSize = 20.sp) }
            }

            Spacer(Modifier.height(12.dp))
            Text("⭐ النقاط: $score", fontSize = 20.sp, fontWeight = FontWeight.Bold)
            Spacer(Modifier.height(12.dp))
            OutlinedButton(onClick = {
                questionIndex++
                answered = false
                onSpeak?.invoke("السؤال التالي", "ar")
            }) {
                Text("التالي ➜")
            }
        }
    }
}
