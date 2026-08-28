package com.learnlettersnumbers.app

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectDragGesturesAfterLongPress
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun InteractiveMemoryBoard(values: List<String>, opened: Set<Int>, matched: Set<Int>, enabled: Boolean = true, onCardClick: (Int) -> Unit) {
    LazyVerticalGrid(
        columns = GridCells.Fixed(if (values.size <= 6) 3 else 4),
        modifier = Modifier.fillMaxWidth(), contentPadding = PaddingValues(8.dp),
        horizontalArrangement = Arrangement.spacedBy(10.dp), verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        itemsIndexed(values) { index, value ->
            val faceUp = index in opened || index in matched
            val scale by animateFloatAsState(if (faceUp) 1f else .94f, tween(180), label = "memory_$index")
            Card(
                modifier = Modifier.height(86.dp).scale(scale).clickable(enabled = enabled && index !in matched) { onCardClick(index) },
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = if (faceUp) Color(0xFF6C7CF7) else Color(0xFFF0F3FF)),
                elevation = CardDefaults.cardElevation(if (faceUp) 8.dp else 3.dp)
            ) {
                Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    AnimatedContent(targetState = faceUp, label = "memory_face") { visible ->
                        Text(if (visible) value else "؟", fontSize = 30.sp, fontWeight = FontWeight.ExtraBold)
                    }
                }
            }
        }
    }
}

@Composable
fun DraggableNumberCard(number: Int, onDropped: () -> Unit, modifier: Modifier = Modifier) {
    var dragging by remember { mutableStateOf(false) }
    Card(
        modifier = modifier.size(68.dp).pointerInput(number) {
            detectDragGesturesAfterLongPress(
                onDragStart = { dragging = true },
                onDragEnd = { dragging = false; onDropped() },
                onDragCancel = { dragging = false }
            ) { change, _ -> change.consume() }
        },
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = if (dragging) Color(0xFFFFD166) else Color.White),
        elevation = CardDefaults.cardElevation(if (dragging) 10.dp else 4.dp)
    ) {
        Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Text(number.toString(), fontSize = 24.sp, fontWeight = FontWeight.ExtraBold)
        }
    }
}

@Composable
fun GameRoundBanner(title: String, subtitle: String) {
    Box(Modifier.fillMaxWidth().background(Brush.horizontalGradient(listOf(Color(0xFF5B8DEF), Color(0xFF8B6CF6))), RoundedCornerShape(24.dp)).padding(18.dp)) {
        Column {
            Text(title, color = Color.White, fontSize = 23.sp, fontWeight = FontWeight.ExtraBold)
            Spacer(Modifier.height(4.dp))
            Text(subtitle, color = Color.White.copy(alpha = .9f), fontSize = 14.sp)
        }
    }
}
