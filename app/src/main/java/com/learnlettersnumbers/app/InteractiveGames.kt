package com.learnlettersnumbers.app

import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlin.random.Random

/** Interactive game primitives used by GamesScreen. No SVG assets are required. */
@Composable
fun InteractiveMemoryGame(level: GameLevel, onComplete: (stars: Int) -> Unit) {
    val source = listOf("أ", "ب", "ت", "ث", "ج", "ح", "م", "ن")
    val pairCount = when (level) {
        GameLevel.EASY -> 3
        GameLevel.MEDIUM -> 4
        GameLevel.HARD -> 6
    }
    val cards = remember(level) { source.take(pairCount).flatMap { listOf(it, it) }.shuffled(Random(level.multiplier * 97)) }
    var open by remember { mutableStateOf<List<Int>>(emptyList()) }
    var matched by remember { mutableStateOf(emptySet<Int>()) }
    var moves by remember { mutableIntStateOf(0) }

    LaunchedEffect(open) {
        if (open.size == 2) {
            moves++
            if (cards[open[0]] == cards[open[1]]) matched = matched + open
            else kotlinx.coroutines.delay(650)
            open = emptyList()
            if (matched.size == cards.size) onComplete(if (moves <= pairCount + 2) 3 else 2)
        }
    }

    Column(Modifier.fillMaxWidth().padding(horizontal = 8.dp)) {
        GameRoundBanner("🧠 ذاكرة الحروف", "افتح بطاقتين وحاول مطابقة الحرفين")
        Spacer(Modifier.height(12.dp))
        GameProgressHeader(matched.size / 2 + 1, pairCount, matched.size / 2)
        Spacer(Modifier.height(8.dp))
        InteractiveMemoryBoard(values = cards, opened = open.toSet(), matched = matched, enabled = open.size < 2) { index ->
            if (index !in open) open = open + index
        }
    }
}

@Composable
fun InteractiveOrderGame(level: GameLevel, onComplete: (Int) -> Unit) {
    val count = if (level == GameLevel.HARD) 6 else 4
    val values = remember(level) { (1..20).shuffled(Random(200 + level.multiplier)).take(count).sorted() }
    var selected by remember { mutableStateOf<List<Int>>(emptyList()) }
    var wrong by remember { mutableStateOf(false) }
    val remaining = values.filter { it !in selected }

    Column(Modifier.fillMaxWidth().padding(16.dp), horizontalAlignment = Alignment.CenterHorizontally) {
        GameRoundBanner("🔢 ترتيب الأرقام", "اختر الأرقام من الأصغر إلى الأكبر")
        Spacer(Modifier.height(16.dp))
        GameProgressHeader(selected.size + 1, values.size, if (selected.isEmpty()) 0 else selected.size / 2)
        Spacer(Modifier.height(14.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) { selected.forEach { NumberTile(it, true) } }
        Spacer(Modifier.height(18.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            remaining.forEach { n ->
                NumberTile(n, false, Modifier.pointerInput(n) {
                    detectDragGestures(onDragEnd = {
                        if (n == values[selected.size]) {
                            wrong = false
                            selected = selected + n
                            if (selected.size == values.size) onComplete(if (level == GameLevel.HARD) 3 else 2)
                        } else wrong = true
                    }) { change, _ -> change.consume() }
                }, onClick = {
                    if (n == values[selected.size]) {
                        wrong = false
                        selected = selected + n
                        if (selected.size == values.size) onComplete(if (level == GameLevel.HARD) 3 else 2)
                    } else wrong = true
                })
            }
        }
        if (wrong) { Spacer(Modifier.height(12.dp)); Text("حاول اختيار الرقم الأصغر أولًا 🔎", color = Color(0xFFD64545), fontWeight = FontWeight.Bold) }
    }
}

@Composable
private fun NumberTile(number: Int, selected: Boolean, modifier: Modifier = Modifier, onClick: (() -> Unit)? = null) {
    Card(modifier = modifier.size(58.dp), shape = RoundedCornerShape(16.dp), colors = CardDefaults.cardColors(containerColor = if (selected) Color(0xFFDCE7FF) else Color.White), elevation = CardDefaults.cardElevation(if (selected) 6.dp else 3.dp)) {
        Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Text(number.toString(), fontSize = 22.sp, fontWeight = FontWeight.Bold)
        }
    }
    if (onClick != null) {
        // Click handling is supplied by the parent drag target; this branch intentionally keeps the tile visual-only.
    }
}

@Composable
fun InteractiveCountGame(level: GameLevel, onAnswer: (Boolean) -> Unit) {
    val amount = remember(level) { Random.nextInt(2, if (level == GameLevel.HARD) 10 else 7) }
    Column(Modifier.fillMaxWidth().padding(20.dp), horizontalAlignment = Alignment.CenterHorizontally) {
        GameRoundBanner("⭐ عد الأشياء", "انظر جيدًا ثم اختر العدد الصحيح")
        Spacer(Modifier.height(14.dp))
        CountingScene(amount)
        Spacer(Modifier.height(18.dp))
        val options = remember(amount) { listOf(amount, amount + 1, (amount - 1).coerceAtLeast(1), amount + 2).distinct().shuffled() }
        Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            options.forEach { n -> Button(onClick = { onAnswer(n == amount) }, shape = RoundedCornerShape(16.dp)) { Text(n.toString(), fontSize = 20.sp, fontWeight = FontWeight.Bold) } }
        }
    }
}
