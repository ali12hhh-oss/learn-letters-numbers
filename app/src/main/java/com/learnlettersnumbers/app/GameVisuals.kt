package com.learnlettersnumbers.app

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun GameProgressHeader(round: Int, total: Int, stars: Int, modifier: Modifier = Modifier) {
    val progress = (round.toFloat() / total).coerceIn(0f, 1f)
    Column(modifier.fillMaxWidth()) {
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
            Text("الجولة ${round.coerceAtMost(total)} من $total", fontWeight = FontWeight.Bold, fontSize = 16.sp)
            Text("⭐ $stars", fontWeight = FontWeight.ExtraBold, fontSize = 17.sp)
        }
        Spacer(Modifier.height(8.dp))
        Box(Modifier.fillMaxWidth().height(10.dp).background(Color(0xFFE5E7F2), CircleShape)) {
            Box(Modifier.fillMaxWidth(progress).fillMaxHeight().background(Brush.horizontalGradient(listOf(Color(0xFF4F8EF7), Color(0xFF7A5AF8))), CircleShape))
        }
    }
}

@Composable
fun CountingScene(count: Int, modifier: Modifier = Modifier) {
    Card(modifier.fillMaxWidth(), shape = RoundedCornerShape(28.dp), colors = CardDefaults.cardColors(containerColor = Color(0xFFF7FAFF)), elevation = CardDefaults.cardElevation(4.dp)) {
        Canvas(Modifier.fillMaxWidth().height(190.dp).padding(14.dp)) {
            val columns = 4
            val radius = 27f
            repeat(count) { i ->
                val row = i / columns
                val col = i % columns
                val x = size.width * (col + 0.5f) / columns
                val y = 46f + row * 65f
                drawCircle(Color(0xFFFFC857), radius, Offset(x, y))
                drawCircle(Color(0xFFFFE7A3), radius - 7f, Offset(x - 6f, y - 6f))
                drawCircle(Color(0xFFF3A712), radius, Offset(x, y), style = Stroke(3f))
            }
        }
    }
}

@Composable
fun GameRewardStars(stars: Int, modifier: Modifier = Modifier) {
    Row(modifier, horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.CenterVertically) {
        repeat(3) { index ->
            val active = index < stars
            val scale by animateFloatAsState(if (active) 1f else .82f, tween(250), label = "star_$index")
            Text(if (active) "⭐" else "☆", fontSize = 42.sp, modifier = Modifier.scale(scale))
        }
    }
}
