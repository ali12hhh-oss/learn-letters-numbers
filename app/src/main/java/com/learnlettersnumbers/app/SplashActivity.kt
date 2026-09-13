package com.learnlettersnumbers.app

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.graphicsLayer
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.delay
import kotlin.math.roundToInt

class SplashActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        window.statusBarColor = Color(0xFF0875D8).toArgbCompat()
        window.navigationBarColor = Color(0xFF4EAF58).toArgbCompat()
        setContent {
            LearnLettersNumbersSplash {
                startActivity(Intent(this, MainActivity::class.java))
                finish()
            }
        }
    }
}

private fun Color.toArgbCompat(): Int = android.graphics.Color.argb(
    (alpha * 255).roundToInt(),
    (red * 255).roundToInt(),
    (green * 255).roundToInt(),
    (blue * 255).roundToInt()
)

private data class SplashBalloon(
    val word: String,
    val color: Color,
    val startX: Float,
    val startY: Float,
    val size: Float,
    val delay: Int
)

@Composable
private fun LearnLettersNumbersSplash(onFinished: () -> Unit) {
    val balloons = remember {
        listOf(
            SplashBalloon("تعلّم", Color(0xFFFFB52E), -0.10f, -0.95f, 0.29f, 80),
            SplashBalloon("الحروف", Color(0xFFEC3DAF), 0.08f, 1.05f, 0.31f, 360),
            SplashBalloon("والأرقام", Color(0xFF1599F2), 0.12f, -0.80f, 0.30f, 640)
        )
    }

    LaunchedEffect(Unit) {
        delay(3000)
        onFinished()
    }

    BoxWithConstraints(
        modifier = Modifier.fillMaxSize().background(
            Brush.verticalGradient(
                listOf(Color(0xFF43B9F5), Color(0xFFB8ECFF), Color(0xFFFFD7A8))
            )
        )
    ) {
        val widthPx = constraints.maxWidth.toFloat()
        val heightPx = constraints.maxHeight.toFloat()

        SplashLandscape(Modifier.fillMaxSize())

        Box(
            modifier = Modifier.fillMaxSize().padding(horizontal = 24.dp),
            contentAlignment = Alignment.Center
        ) {
            balloons.forEach { balloon ->
                SplashBalloonView(balloon, widthPx, heightPx)
            }
        }

        Column(
            modifier = Modifier.align(Alignment.BottomCenter).padding(bottom = 34.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                "عالم صغير... وتعلّم كبير ✨",
                color = Color.White,
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center
            )
        }
    }
}

@Composable
private fun SplashBalloonView(balloon: SplashBalloon, widthPx: Float, heightPx: Float) {
    val progress by animateFloatAsState(
        targetValue = 1f,
        animationSpec = tween(
            durationMillis = 1050,
            delayMillis = balloon.delay,
            easing = FastOutSlowInEasing
        ),
        label = "balloon_${balloon.word}"
    )
    val scale by animateFloatAsState(
        targetValue = 1f,
        animationSpec = tween(900, balloon.delay, easing = FastOutSlowInEasing),
        label = "scale_${balloon.word}"
    )

    val targetX = when (balloon.word) {
        "تعلّم" -> -0.18f
        "الحروف" -> 0.20f
        else -> 0f
    }
    val targetY = when (balloon.word) {
        "تعلّم" -> -0.16f
        "الحروف" -> 0.20f
        else -> -0.04f
    }

    val x = ((balloon.startX * widthPx) * (1f - progress) + targetX * widthPx * progress).roundToInt()
    val y = ((balloon.startY * heightPx) * (1f - progress) + targetY * heightPx * progress).roundToInt()

    Box(
        modifier = Modifier
            .offset { IntOffset(x, y) }
            .size((balloon.size * 100).dp)
            .graphicsLayer {
                alpha = (0.25f + 0.75f * progress).coerceIn(0f, 1f)
                scaleX = 0.86f + 0.14f * scale
                scaleY = 0.86f + 0.14f * scale
            },
        contentAlignment = Alignment.Center
    ) {
        Canvas(Modifier.fillMaxSize()) {
            val radius = size.minDimension * 0.43f
            val center = Offset(size.width / 2f, size.height / 2.05f)
            drawCircle(
                brush = Brush.radialGradient(
                    0f to Color.White.copy(alpha = .42f),
                    .20f to balloon.color.copy(alpha = 1f),
                    .72f to balloon.color,
                    1f to balloon.color.copy(red = balloon.color.red * .68f, green = balloon.color.green * .68f, blue = balloon.color.blue * .68f)
                ),
                radius = radius,
                center = center
            )
            drawCircle(
                color = Color.White.copy(alpha = .42f),
                radius = radius * .12f,
                center = Offset(center.x - radius * .28f, center.y - radius * .42f)
            )
            drawCircle(
                color = Color.White.copy(alpha = .17f),
                radius = radius * .065f,
                center = Offset(center.x - radius * .08f, center.y - radius * .53f)
            )
            drawLine(
                color = balloon.color.copy(alpha = .75f),
                start = Offset(center.x, center.y + radius * .98f),
                end = Offset(center.x, size.height),
                strokeWidth = 3.dp.toPx()
            )
        }
        Text(
            text = balloon.word,
            color = Color.White,
            fontSize = 19.sp,
            fontWeight = FontWeight.Black,
            textAlign = TextAlign.Center,
            modifier = Modifier.fillMaxWidth().padding(horizontal = 8.dp)
        )
    }
}

@Composable
private fun SplashLandscape(modifier: Modifier) {
    Canvas(modifier) {
        val w = size.width
        val h = size.height

        // Soft cinematic clouds.
        fun cloud(cx: Float, cy: Float, scale: Float) {
            val c = Color.White.copy(alpha = .72f)
            drawCircle(c, 52f * scale, Offset(cx, cy))
            drawCircle(c, 68f * scale, Offset(cx + 55f * scale, cy + 8f * scale))
            drawCircle(c, 42f * scale, Offset(cx + 105f * scale, cy + 20f * scale))
            drawOval(c, Offset(cx - 28f * scale, cy + 18f * scale), Size(160f * scale, 62f * scale))
        }
        cloud(w * .08f, h * .16f, .55f)
        cloud(w * .78f, h * .20f, .72f)
        cloud(w * .72f, h * .54f, .38f)

        // Distant hills.
        drawOval(Color(0xFF8DD99A), Offset(-w * .20f, h * .70f), Size(w * .78f, h * .34f))
        drawOval(Color(0xFF70C986), Offset(w * .40f, h * .67f), Size(w * .92f, h * .37f))
        drawOval(Color(0xFF55B874), Offset(-w * .08f, h * .80f), Size(w * 1.18f, h * .34f))

        // Warm winding path.
        val pathTop = h * .69f
        drawOval(Color(0xFFFFD18F), Offset(w * .43f, pathTop), Size(w * .16f, h * .44f))
        drawOval(Color(0xFFFFC77D), Offset(w * .33f, h * .82f), Size(w * .36f, h * .28f))

        // Rounded trees, intentionally vector-drawn so no low-quality bitmap is used.
        fun tree(x: Float, y: Float, s: Float) {
            drawRect(Color(0xFF8B5A3C), Offset(x - 9f * s, y), Size(18f * s, 70f * s))
            drawCircle(Color(0xFF3EAA67), 42f * s, Offset(x - 30f * s, y))
            drawCircle(Color(0xFF56BE76), 48f * s, Offset(x + 20f * s, y - 12f * s))
            drawCircle(Color(0xFF79CF82), 35f * s, Offset(x, y - 42f * s))
        }
        tree(w * .10f, h * .78f, .72f)
        tree(w * .88f, h * .79f, .78f)
        tree(w * .22f, h * .88f, .48f)
        tree(w * .77f, h * .87f, .50f)

        // Small foreground flowers for depth.
        listOf(Offset(w*.08f,h*.91f),Offset(w*.17f,h*.94f),Offset(w*.83f,h*.93f),Offset(w*.92f,h*.90f)).forEach {
            drawCircle(Color(0xFFFF80B8), 7f, it)
            drawCircle(Color(0xFFFFD54F), 3f, it)
        }
    }
}
