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
import androidx.compose.foundation.Canvas
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
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.delay

class SplashActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        window.statusBarColor = android.graphics.Color.rgb(15, 105, 190)
        window.navigationBarColor = android.graphics.Color.rgb(17, 91, 119)
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
    val delayMs: Int,
    val fill: List<Color>,
    val textColor: Color
)

@Composable
private fun LearnLettersNumbersSplash(onFinished: () -> Unit) {
    val balloons = remember {
        // Deliberate RTL composition: right = تعلّم, center = الحروف, left = والأرقام.
        listOf(
            SplashBalloon("تعلّم", 0.92f, -1.18f, 0.255f, 100, listOf(Color(0xFFFFE45A), Color(0xFFFFA515), Color(0xFFE87500)), Color(0xFF8C3D00)),
            SplashBalloon("الحروف", -0.88f, 1.16f, 0.00f, 650, listOf(Color(0xFFFF70C2), Color(0xFFEF2A98), Color(0xFFC91673)), Color(0xFF8B145F)),
            SplashBalloon("والأرقام", 0.88f, -1.22f, -0.255f, 1200, listOf(Color(0xFF63D4FF), Color(0xFF168CF0), Color(0xFF075BC4)), Color(0xFF063F88))
        )
    }

    LaunchedEffect(Unit) {
        delay(5000)
        onFinished()
    }

    BoxWithConstraints(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    0f to Color(0xFF0577D2),
                    0.42f to Color(0xFF28B6F0),
                    0.72f to Color(0xFF8DDEEF),
                    1f to Color(0xFFFFD29A)
                )
            )
    ) {
        val density = LocalDensity.current
        val widthPx = with(density) { maxWidth.toPx() }
        val heightPx = with(density) { maxHeight.toPx() }
        val infinite = rememberInfiniteTransition(label = "splash_world")
        val sunScale by infinite.animateFloat(
            0.96f, 1.04f,
            infiniteRepeatable(tween(3000, easing = FastOutSlowInEasing), RepeatMode.Reverse),
            label = "sun_scale"
        )
        val cloudDrift by infinite.animateFloat(
            -7f, 7f,
            infiniteRepeatable(tween(6500, easing = FastOutSlowInEasing), RepeatMode.Reverse),
            label = "cloud_drift"
        )
        val sparkleAlpha by infinite.animateFloat(
            0.25f, 0.9f,
            infiniteRepeatable(tween(1700), RepeatMode.Reverse),
            label = "sparkle_alpha"
        )

        SplashLandscape(Modifier.fillMaxSize(), sunScale, cloudDrift)

        Text("✦", Modifier.align(Alignment.TopStart).offset(54.dp, 214.dp), color = Color.White.copy(alpha = sparkleAlpha), fontSize = 22.sp)
        Text("✦", Modifier.align(Alignment.TopEnd).offset((-48).dp, 278.dp), color = Color.White.copy(alpha = sparkleAlpha * .8f), fontSize = 16.sp)
        Text("·", Modifier.align(Alignment.CenterStart).offset(30.dp, 86.dp), color = Color.White.copy(alpha = sparkleAlpha), fontSize = 28.sp)
        Text("·", Modifier.align(Alignment.CenterEnd).offset((-34).dp, 120.dp), color = Color.White.copy(alpha = sparkleAlpha * .7f), fontSize = 25.sp)

        Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            balloons.forEach { balloon ->
                SplashBalloonView(balloon, widthPx, heightPx, density)
            }
        }

        SplashLoadingPanel(Modifier.align(Alignment.BottomCenter))
    }
}

@Composable
private fun SplashLandscape(modifier: Modifier, sunScale: Float, cloudDrift: Float) {
    Canvas(modifier) {
        val w = size.width
        val h = size.height

        drawCircle(
            brush = Brush.radialGradient(listOf(Color(0xFFFFF8C7).copy(alpha = .95f), Color(0xFFFFD66E).copy(alpha = .35f), Color.Transparent)),
            radius = w * .27f * sunScale,
            center = androidx.compose.ui.geometry.Offset(w * .78f, h * .11f)
        )
        drawCircle(Color(0xFFFFF4B0).copy(alpha = .95f), w * .075f, androidx.compose.ui.geometry.Offset(w * .78f, h * .11f))

        fun cloud(cx: Float, cy: Float, s: Float) {
            drawCircle(Color.White.copy(alpha = .78f), w * .045f * s, androidx.compose.ui.geometry.Offset(cx - w * .055f * s, cy))
            drawCircle(Color.White.copy(alpha = .88f), w * .065f * s, androidx.compose.ui.geometry.Offset(cx, cy - h * .012f * s))
            drawCircle(Color.White.copy(alpha = .72f), w * .048f * s, androidx.compose.ui.geometry.Offset(cx + w * .055f * s, cy))
            drawOval(Color.White.copy(alpha = .72f), androidx.compose.ui.geometry.Offset(cx - w * .09f * s, cy), androidx.compose.ui.geometry.Size(w * .18f * s, h * .035f * s))
        }
        cloud(w * .13f + cloudDrift, h * .20f, 1.0f)
        cloud(w * .88f - cloudDrift, h * .27f, .82f)

        val far = Path().apply {
            moveTo(0f, h * .72f)
            lineTo(w * .16f, h * .59f)
            lineTo(w * .31f, h * .70f)
            lineTo(w * .48f, h * .57f)
            lineTo(w * .66f, h * .69f)
            lineTo(w * .82f, h * .55f)
            lineTo(w, h * .68f)
            lineTo(w, h)
            lineTo(0f, h)
            close()
        }
        drawPath(far, Brush.verticalGradient(listOf(Color(0xFF76C7D9), Color(0xFF4C9B91)), startY = h * .55f, endY = h))

        drawOval(Color(0xFF7DDDF0).copy(alpha = .95f), androidx.compose.ui.geometry.Offset(w * .20f, h * .70f), androidx.compose.ui.geometry.Size(w * .60f, h * .17f))
        drawOval(Color.White.copy(alpha = .18f), androidx.compose.ui.geometry.Offset(w * .28f, h * .73f), androidx.compose.ui.geometry.Size(w * .43f, h * .06f))

        val near = Path().apply {
            moveTo(0f, h * .78f)
            lineTo(w * .18f, h * .70f)
            lineTo(w * .37f, h * .80f)
            lineTo(w * .57f, h * .68f)
            lineTo(w * .76f, h * .79f)
            lineTo(w, h * .70f)
            lineTo(w, h)
            lineTo(0f, h)
            close()
        }
        drawPath(near, Brush.verticalGradient(listOf(Color(0xFF66B96D), Color(0xFF2D804F)), startY = h * .68f, endY = h))

        val path = Path().apply {
            moveTo(w * .43f, h)
            cubicTo(w * .47f, h * .92f, w * .58f, h * .88f, w * .57f, h * .78f)
            cubicTo(w * .56f, h * .74f, w * .53f, h * .71f, w * .51f, h * .68f)
            lineTo(w * .63f, h * .68f)
            cubicTo(w * .66f, h * .75f, w * .70f, h * .84f, w * .69f, h)
            close()
        }
        drawPath(path, Brush.verticalGradient(listOf(Color(0xFFFFE4B1), Color(0xFFD5A36C))))

        for (i in 0..10) {
            val x = (w * (.04f + i * .093f))
            val y = h * (.88f + (i % 3) * .027f)
            drawCircle(Color(0xFFFFF2A8), w * .012f, androidx.compose.ui.geometry.Offset(x, y))
            drawCircle(Color(0xFFFF8FB8), w * .007f, androidx.compose.ui.geometry.Offset(x + w * .018f, y - h * .018f))
        }
    }
}

@Composable
private fun SplashBalloonView(balloon: SplashBalloon, widthPx: Float, heightPx: Float, density: androidx.compose.ui.unit.Density) {
    val entry = remember { Animatable(0f) }
    val infinite = rememberInfiniteTransition(label = "balloon_${balloon.word}")
    val floatY by infinite.animateFloat(
        -4f, 4f,
        infiniteRepeatable(tween(4200 + balloon.delayMs, easing = FastOutSlowInEasing), RepeatMode.Reverse),
        label = "float_${balloon.word}"
    )
    val rotation by infinite.animateFloat(
        -0.8f, 0.8f,
        infiniteRepeatable(tween(5000 + balloon.delayMs, easing = FastOutSlowInEasing), RepeatMode.Reverse),
        label = "rotation_${balloon.word}"
    )

    LaunchedEffect(Unit) {
        delay(balloon.delayMs.toLong())
        entry.animateTo(1f, tween(1650, easing = FastOutSlowInEasing))
    }

    val startX = balloon.startX * widthPx
    val startY = balloon.startY * heightPx
    val targetX = with(density) { (balloon.targetX * widthPx).toDp() }
    val size = (with(density) { (widthPx * .31f).toDp() }).coerceIn(112.dp, 138.dp)

    Column(
        modifier = Modifier
            .offset(x = targetX)
            .graphicsLayer {
                translationX = startX * (1f - entry.value)
                translationY = startY * (1f - entry.value) + floatY * density.density
                rotationZ = rotation * entry.value
                alpha = entry.value
                scaleX = .84f + .16f * entry.value
                scaleY = .84f + .16f * entry.value
            },
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Box(
            modifier = Modifier
                .size(width = size, height = size * 1.06f)
                .shadow(15.dp, CircleShape)
                .background(Brush.radialGradient(listOf(Color.White.copy(alpha = .75f), balloon.fill[0], balloon.fill[1], balloon.fill[2])), CircleShape)
                .border(2.dp, Color.White.copy(alpha = .45f), CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Box(Modifier.size(size * .20f).offset(x = -(size.value * .20f).dp, y = -(size.value * .23f).dp).background(Color.White.copy(alpha = .48f), CircleShape))
            Box(Modifier.size(size * .07f).offset(x = -(size.value * .04f).dp, y = -(size.value * .30f).dp).background(Color.White.copy(alpha = .30f), CircleShape))
            Box(
                Modifier
                    .fillMaxWidth(.86f)
                    .height(46.dp)
                    .background(Color.White.copy(alpha = .13f), RoundedCornerShape(18.dp))
                    .border(1.dp, Color.White.copy(alpha = .25f), RoundedCornerShape(18.dp)),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    balloon.word,
                    color = balloon.textColor,
                    fontFamily = FontFamily.Serif,
                    fontSize = if (balloon.word == "والأرقام") 17.sp else 19.sp,
                    fontWeight = FontWeight.Bold,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.fillMaxWidth().padding(horizontal = 2.dp)
                )
            }
        }
        Box(Modifier.width(8.dp).height(8.dp).background(balloon.fill[2], RoundedCornerShape(2.dp)))
        Box(Modifier.width(3.dp).height(31.dp).background(balloon.fill[2].copy(alpha = .9f), RoundedCornerShape(50)))
        Box(
            Modifier
                .width(18.dp)
                .height(30.dp)
                .offset(y = (-3).dp)
                .background(Brush.horizontalGradient(listOf(balloon.fill[1], balloon.fill[0], balloon.fill[1])), RoundedCornerShape(50))
        )
    }
}

@Composable
private fun SplashLoadingPanel(modifier: Modifier) {
    val infinite = rememberInfiniteTransition(label = "loading_panel")
    val progress by infinite.animateFloat(
        .25f, .88f,
        infiniteRepeatable(tween(1600, easing = FastOutSlowInEasing), RepeatMode.Reverse),
        label = "progress"
    )
    Box(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 24.dp, vertical = 24.dp)
            .background(Color(0xFF123F58).copy(alpha = .48f), RoundedCornerShape(30.dp))
            .border(1.dp, Color.White.copy(alpha = .30f), RoundedCornerShape(30.dp))
            .shadow(10.dp, RoundedCornerShape(30.dp))
            .padding(horizontal = 18.dp, vertical = 13.dp)
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.fillMaxWidth()) {
            CompositionLocalProvider(androidx.compose.ui.platform.LocalLayoutDirection provides androidx.compose.ui.unit.LayoutDirection.Rtl) {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.Center) {
                    Text("كبير", color = Color(0xFFFFB72E), fontFamily = FontFamily.Serif, fontSize = 20.sp, fontWeight = FontWeight.Black)
                    Text(" وتعلّم ", color = Color(0xFFFF5CA8), fontFamily = FontFamily.Serif, fontSize = 20.sp, fontWeight = FontWeight.Black)
                    Text("صغير...", color = Color(0xFF6BD5FF), fontFamily = FontFamily.Serif, fontSize = 20.sp, fontWeight = FontWeight.Black)
                    Text("عالم ", color = Color.White, fontFamily = FontFamily.Serif, fontSize = 20.sp, fontWeight = FontWeight.Black)
                }
            }
            Spacer(Modifier.height(9.dp))
            Box(Modifier.fillMaxWidth(.74f).height(7.dp).background(Color.White.copy(alpha = .20f), RoundedCornerShape(50))) {
                Box(Modifier.fillMaxWidth(progress).fillMaxHeight().background(Brush.horizontalGradient(listOf(Color(0xFFFFD84D), Color(0xFFFF5AA9), Color(0xFF57C8FF))), RoundedCornerShape(50)))
            }
        }
    }
}
