package com.learnlettersnumbers.app

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
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
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlin.random.Random

/** Interactive game primitives used by GamesScreen. No SVG assets are required. */
@Composable
fun InteractiveMemoryGame(
    level: GameLevel,
    onComplete: (stars: Int) -> Unit
) {
    val source = listOf("أ", "ب", "ت", "ث", "ج", "ح", "م", "ن")
    val pairCount = when (level) {
        GameLevel.EASY -> 3
        GameLevel.MEDIUM -> 4
        GameLevel.HARD -> 6
    }
    val cards = remember(level) {
        source.take(pairCount).flatMap { listOf(it, it) }.shuffled(Random(level.multiplier * 97))
    }
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

    LazyVerticalGrid(
        columns = GridCells.Fixed(if (pairCount <= 3) 3 else 4),
        contentPadding = PaddingValues(16.dp),
        horizontalArrangement = Arrangement.spacedBy(10.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        items(cards.indices.toList()) { index ->
            val visible = index in open || index in matched
            Button(
                enabled = index !in matched && index !in open && open.size < 2,
                onClick = { open = open + index },
                modifier = Modifier.height(82.dp),
                shape = RoundedCornerShape(18.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = if (visible) Color(0xFF5E8CFF) else Color(0xFFE8EEFF)
                )
            ) { Text(if (visible) cards[index] else "?", fontSize = 30.sp, fontWeight = FontWeight.ExtraBold) }
        }
    }
}

@Composable
fun InteractiveOrderGame(
    level: GameLevel,
    onComplete: (Int) -> Unit
) {
    val count = if (level == GameLevel.HARD) 6 else 4
    val values = remember(level) { (1..20).shuffled(Random(200 + level.multiplier)).take(count).sorted() }
    var selected by remember { mutableStateOf<List<Int>>(emptyList()) }
    val remaining = values.filter { it !in selected }
    Column(Modifier.fillMaxWidth().padding(16.dp), horizontalAlignment = Alignment.CenterHorizontally) {
        Text("اسحب أو اختر الأرقام من الأصغر إلى الأكبر", fontWeight = FontWeight.Bold, fontSize = 18.sp)
        Spacer(Modifier.height(18.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            selected.forEach { n -> NumberTile(n, true) }
        }
        Spacer(Modifier.height(20.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            remaining.forEach { n ->
                NumberTile(n, false, Modifier.pointerInput(Unit) {
                    detectDragGestures(onDragEnd = {
                        if (n == values[selected.size]) {
                            selected = selected + n
                            if (selected.size == values.size) onComplete(if (level == GameLevel.HARD) 3 else 2)
                        }
                    }) { _, _ -> }
                })
            }
        }
    }
}

@Composable
private fun NumberTile(number: Int, selected: Boolean, modifier: Modifier = Modifier) {
    Card(modifier.size(58.dp).then(modifier), shape = RoundedCornerShape(16.dp)) {
        Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Text(number.toString(), fontSize = 22.sp, fontWeight = FontWeight.Bold)
        }
    }
}

@Composable
fun InteractiveCountGame(level: GameLevel, onAnswer: (Boolean) -> Unit) {
    val amount = remember(level) { Random.nextInt(2, if (level == GameLevel.HARD) 10 else 7) }
    Column(Modifier.fillMaxWidth().padding(20.dp), horizontalAlignment = Alignment.CenterHorizontally) {
        Text("كم عدد الأشياء؟", fontSize = 24.sp, fontWeight = FontWeight.ExtraBold)
        Spacer(Modifier.height(18.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            repeat(amount) { Text("⭐", fontSize = 34.sp) }
        }
        Spacer(Modifier.height(20.dp))
        val options = remember(amount) { listOf(amount, amount + 1, (amount - 1).coerceAtLeast(1), amount + 2).distinct().shuffled() }
        Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            options.forEach { n -> Button(onClick = { onAnswer(n == amount) }) { Text(n.toString()) } }
        }
    }
}
