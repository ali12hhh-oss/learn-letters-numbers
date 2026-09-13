package com.learnlettersnumbers.app

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.delay

class SplashActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        window.statusBarColor = android.graphics.Color.rgb(19, 112, 203)
        window.navigationBarColor = android.graphics.Color.rgb(22, 105, 137)
        setContent {
            LearnLettersNumbersSplash {
                startActivity(Intent(this, MainActivity::class.java))
                finish()
            }
        }
    }
}

private data class SplashBalloon(
    val word: String,
    val startX: Float,
    val startY: Float,
    val targetX: Float,
    val targetY: Float,
    val sizeFraction: Float,
    val delayMs: Int,
    val colors: List<Color>
)

@Composable
private fun LearnLettersNumbersSplash(onFinished: () -> Unit) {
    val balloons = remember {
        listOf(
            // Arabic reading order: right -> center -> left.
            SplashBalloon("تعلم", 0.85f, -1.20f, 0.25f, -0.02f, 0.31f, 150, listOf(Color(0xFFFFD84D), Color(0xFFFF9E1B), Color(0xFFE87800))),
            SplashBalloon("الحروف", -0.80f, 1.20f, 0.00f, -0.02f, 0.31f, 700, listOf(Color(0xFFFF73C5), Color(0xFFEF2998), Color(0xFFC51576))),
            SplashBalloon("والأرقام", 0.80f, -1.25f, -0.25f, -0.02f, 0.32f, 1250, listOf(Color(0xFF55C8FF), Color(0xFF1688F2), Color(0xFF0B5DCA)))
        )
    }

    LaunchedEffect(Unit) {
        delay(4500)
        onFinished()
    }

    BoxWithConstraints(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    0f to Color(0xFF087BD7),
                    0.48f to Color(0xFF39B9F2),
                    0.78f to Color(0xFFA7E6F3),
                    1f to Color(0xFFFFD8A7)
                )
            )
    ) {
        val density = LocalDensity.current
        val widthPx = with(density) { maxWidth.toPx() }
        val heightPx = with(density) { maxHeight.toPx() }
        val infinite = rememberInfiniteTransition(label = "splash_background")
        val glow by infinite.animateFloat(
            initialValue = 0.92f,
            targetValue = 1.08f,
            animationSpec = infiniteRepeatable(tween(2200, easing = FastOutSlowInEasing), RepeatMode.Reverse),
            label = "glow"
        )
        val cloudShift by infinite.animateFloat(
            initialValue = -14f,
            targetValue = 14f,
            animationSpec = infiniteRepeatable(tween(5000, easing = FastOutSlowInEasing), RepeatMode.Reverse),
            label = "cloud_shift"
        )

        Box(
            modifier = Modifier
                .size(300.dp)
                .align(Alignment.TopCenter)
                .offset(y = (-86).dp)
                .graphicsLayer { scaleX = glow; scaleY = glow }
                .background(Color.White.copy(alpha = 0.10f), CircleShape)
        )

        SplashCloud(
            modifier = Modifier
                .align(Alignment.TopStart)
                .offset(x = (-24 + cloudShift / 4).dp, y = 92.dp),
            scale = 0.72f
        )
        SplashCloud(
            modifier = Modifier
                .align(Alignment.TopEnd)
                .offset(x = (28 + cloudShift / 5).dp, y = 178.dp),
            scale = 0.92f
        )

        SplashSparkles(Modifier.fillMaxSize())

        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            balloons.forEach { balloon ->
                SplashBalloonView(balloon, widthPx, heightPx, density)
            }
        }

        Column(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(horizontal = 24.dp, vertical = 28.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color(0xFF164E69).copy(alpha = 0.30f), RoundedCornerShape(24.dp))
                    .border(1.dp, Color.White.copy(alpha = 0.20f), RoundedCornerShape(24.dp))
                    .padding(horizontal = 20.dp, vertical = 14.dp)
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = "عالم صغير... وتعلّم كبير",
                        color = Color.White,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.ExtraBold,
                        textAlign = TextAlign.Center
                    )
                    Spacer(Modifier.height(8.dp))
                    val loadingTransition = rememberInfiniteTransition(label = "loading")
                    val progress by loadingTransition.animateFloat(
                        initialValue = 0.12f,
                        targetValue = 0.88f,
                        animationSpec = infiniteRepeatable(tween(1200, easing = FastOutSlowInEasing), RepeatMode.Reverse),
                        label = "loading_progress"
                    )
                    Box(
                        modifier = Modifier
                            .fillMaxWidth(0.72f)
                            .height(5.dp)
                            .background(Color.White.copy(alpha = 0.22f), RoundedCornerShape(50))
                    ) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth(progress)
                                .fillMaxHeight()
                                .background(Color(0xFFFFD34E), RoundedCornerShape(50))
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun SplashBalloonView(
    balloon: SplashBalloon,
    widthPx: Float,
    heightPx: Float,
    density: Density
) {
    val entry = remember { Animatable(0f) }
    val infinite = rememberInfiniteTransition(label = "balloon_${balloon.word}")
    val floatY by infinite.animateFloat(
        initialValue = -5f,
        targetValue = 5f,
        animationSpec = infiniteRepeatable(tween(3600 + balloon.delayMs, easing = FastOutSlowInEasing), RepeatMode.Reverse),
        label = "float_y_${balloon.word}"
    )
    val rotation by infinite.animateFloat(
        initialValue = -1.0f,
        targetValue = 1.0f,
        animationSpec = infiniteRepeatable(tween(4200 + balloon.delayMs, easing = FastOutSlowInEasing), RepeatMode.Reverse),
        label = "rotation_${balloon.word}"
    )

    LaunchedEffect(Unit) {
        delay(balloon.delayMs.toLong())
        // A deliberately long, gentle entrance: the balloons should feel like
        // they are floating into place rather than being launched at the user.
        entry.animateTo(
            1f,
            animationSpec = tween(
                durationMillis = 1500,
                easing = FastOutSlowInEasing
            )
        )
    }

    val startTranslationX = balloon.startX * widthPx
    val startTranslationY = balloon.startY * heightPx
    val targetX = with(density) { (balloon.targetX * widthPx / density.density).dp }
    val targetY = with(density) { (balloon.targetY * heightPx / density.density).dp }
    val size = with(density) { (widthPx / density.density * balloon.sizeFraction).dp.coerceIn(106.dp, 132.dp) }

    Box(
        modifier = Modifier
            .offset(x = targetX, y = targetY)
            .size(size)
            .graphicsLayer {
                translationX = startTranslationX * (1f - entry.value)
                translationY = startTranslationY * (1f - entry.value) + floatY * density.density
                rotationZ = rotation * entry.value
                alpha = entry.value.coerceIn(0f, 1f)
                scaleX = 0.82f + 0.18f * entry.value
                scaleY = 0.82f + 0.18f * entry.value
                shadowElevation = 18.dp.toPx()
            },
        contentAlignment = Alignment.Center
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .shadow(12.dp, CircleShape)
                .background(
                    Brush.radialGradient(
                        colors = listOf(
                            Color.White.copy(alpha = 0.62f),
                            balloon.colors[0].copy(alpha = 0.98f),
                            balloon.colors[1],
                            balloon.colors[2]
                        ),
                        radius = 260f
                    ),
                    CircleShape
                )
                .border(1.5.dp, Color.White.copy(alpha = 0.48f), CircleShape)
        )

        Box(
            modifier = Modifier
                .size(size * 0.20f)
                .offset(x = -(size.value * 0.13f).dp, y = -(size.value * 0.18f).dp)
                .background(Color.White.copy(alpha = 0.46f), CircleShape)
        )
        Box(
            modifier = Modifier
                .size(size * 0.09f)
                .offset(x = (size.value * 0.03f).dp, y = -(size.value * 0.25f).dp)
                .background(Color.White.copy(alpha = 0.28f), CircleShape)
        )

        Text(
            text = balloon.word,
            color = Color.White,
            fontSize = if (balloon.word == "والأرقام") 15.sp else 17.sp,
            fontWeight = FontWeight.Black,
            textAlign = TextAlign.Center,
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 6.dp)
                .graphicsLayer { shadowElevation = 4.dp.toPx() }
        )

        Box(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .offset(y = (size.value * 0.42f).dp)
                .width(3.dp)
                .height(30.dp)
                .background(balloon.colors[2].copy(alpha = 0.88f), RoundedCornerShape(50))
        )
        Box(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .offset(y = (size.value * 0.38f).dp)
                .size(7.dp)
                .graphicsLayer { rotationZ = 45f }
                .background(balloon.colors[2], RoundedCornerShape(2.dp))
        )
    }
}

@Composable
private fun SplashCloud(modifier: Modifier, scale: Float) {
    Row(modifier = modifier, verticalAlignment = Alignment.Bottom) {
        Box(Modifier.size((42 * scale).dp).background(Color.White.copy(alpha = 0.64f), CircleShape))
        Box(
            Modifier
                .size((60 * scale).dp)
                .offset(x = (-10 * scale).dp)
                .background(Color.White.copy(alpha = 0.72f), CircleShape)
        )
        Box(
            Modifier
                .size((40 * scale).dp)
                .offset(x = (-18 * scale).dp)
                .background(Color.White.copy(alpha = 0.60f), CircleShape)
        )
    }
}

@Composable
private fun SplashSparkles(modifier: Modifier) {
    val infinite = rememberInfiniteTransition(label = "sparkles")
    val alpha by infinite.animateFloat(
        initialValue = 0.25f,
        targetValue = 0.80f,
        animationSpec = infiniteRepeatable(tween(1400), RepeatMode.Reverse),
        label = "sparkle_alpha"
    )
    Box(modifier = modifier) {
        Text("✦", modifier = Modifier.align(Alignment.TopStart).offset(70.dp, 250.dp), color = Color.White.copy(alpha = alpha), fontSize = 18.sp)
        Text("✦", modifier = Modifier.align(Alignment.TopEnd).offset((-58).dp, 310.dp), color = Color.White.copy(alpha = alpha * 0.8f), fontSize = 14.sp)
        Text("·", modifier = Modifier.align(Alignment.CenterStart).offset(42.dp, 100.dp), color = Color.White.copy(alpha = alpha), fontSize = 24.sp)
        Text("·", modifier = Modifier.align(Alignment.CenterEnd).offset((-50).dp, 30.dp), color = Color.White.copy(alpha = alpha * 0.7f), fontSize = 24.sp)
    }
}
