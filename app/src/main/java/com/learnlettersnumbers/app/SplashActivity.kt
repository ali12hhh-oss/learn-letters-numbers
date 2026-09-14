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
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.platform.LocalLayoutDirection
import kotlinx.coroutines.delay

class SplashActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        window.statusBarColor = android.graphics.Color.rgb(17, 93, 163)
        window.navigationBarColor = android.graphics.Color.rgb(18, 70, 105)
        setContent {
            ArabicSplash {
                startActivity(Intent(this, MainActivity::class.java))
                finish()
            }
        }
    }
}

private data class SplashItem(
    val word: String,
    val colors: List<Color>,
    val delay: Int
)

@Composable
private fun ArabicSplash(onFinished: () -> Unit) {
    val items = remember {
        listOf(
            SplashItem("والأرقام", listOf(Color(0xFFFF6A8B), Color(0xFF9D3FD1)), 1100),
            SplashItem("الحروف", listOf(Color(0xFFFFB14E), Color(0xFFE34D75)), 600),
            SplashItem("تعلّم", listOf(Color(0xFF45D8E8), Color(0xFF0878C8)), 100)
        )
    }

    LaunchedEffect(Unit) {
        delay(5200)
        onFinished()
    }

    val infinite = rememberInfiniteTransition(label = "splash_motion")
    val cloudShift by infinite.animateFloat(
        -10f, 10f,
        infiniteRepeatable(tween(8000, easing = FastOutSlowInEasing), RepeatMode.Reverse),
        label = "cloud_shift"
    )
    val sunPulse by infinite.animateFloat(
        .88f, 1.08f,
        infiniteRepeatable(tween(2600, easing = FastOutSlowInEasing), RepeatMode.Reverse),
        label = "sun_pulse"
    )

    // LTR is deliberate: left = والأرقام, center = الحروف, right = تعلّم.
    CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Ltr) {
        Box(Modifier.fillMaxSize()) {
            SplashBackground(cloudShift, sunPulse)

            Column(
                Modifier.fillMaxSize().padding(horizontal = 12.dp, vertical = 22.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Spacer(Modifier.height(148.dp))

                Text(
                    "تعلّم بمرح",
                    fontFamily = FontFamily.Serif,
                    fontSize = 17.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White.copy(alpha = .9f)
                )

                Spacer(Modifier.height(13.dp))

                Row(
                    Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.Top
                ) {
                    items.forEachIndexed { index, item ->
                        SplashBalloon(item)
                        if (index != items.lastIndex) Spacer(Modifier.width(3.dp))
                    }
                }

                Spacer(Modifier.weight(1f))
                StoneMotto()
                Spacer(Modifier.height(14.dp))
                FullGreenProgress()
                Spacer(Modifier.height(5.dp))
            }
        }
    }
}

@Composable
private fun SplashBackground(cloudShift: Float, sunPulse: Float) {
    val density = LocalDensity.current
    Box(Modifier.fillMaxSize()) {
        Canvas(Modifier.fillMaxSize()) {
            val w = size.width
            val h = size.height

            drawRect(
                brush = Brush.verticalGradient(
                    listOf(
                        Color(0xFF07569E),
                        Color(0xFF0798D0),
                        Color(0xFF62D5E4),
                        Color(0xFFFFD08D)
                    )
                )
            )

            val sunRadius = 74f * sunPulse * density.density
            drawCircle(
                color = Color(0xFFFFF2A8).copy(alpha = .18f),
                radius = sunRadius * 2.0f,
                center = androidx.compose.ui.geometry.Offset(w * .83f, h * .15f)
            )
            drawCircle(
                color = Color(0xFFFFF4B4).copy(alpha = .30f),
                radius = sunRadius,
                center = androidx.compose.ui.geometry.Offset(w * .83f, h * .15f)
            )
            drawCircle(
                color = Color(0xFFFFF7C8).copy(alpha = .9f),
                radius = sunRadius * .62f,
                center = androidx.compose.ui.geometry.Offset(w * .83f, h * .15f)
            )

            drawCloud(w * .10f + cloudShift * density.density, h * .20f, 1.0f)
            drawCloud(w * .78f - cloudShift * density.density, h * .29f, .72f)

            drawSparkle(w * .16f, h * .10f, 5f)
            drawSparkle(w * .66f, h * .08f, 4f)
            drawSparkle(w * .93f, h * .34f, 4f)

            val backHill = Path().apply {
                moveTo(0f, h * .84f)
                cubicTo(w * .20f, h * .75f, w * .34f, h * .84f, w * .52f, h * .78f)
                cubicTo(w * .72f, h * .70f, w * .86f, h * .80f, w, h * .73f)
                lineTo(w, h)
                lineTo(0f, h)
                close()
            }
            drawPath(backHill, Color(0xFF68C58D).copy(alpha = .75f))

            val frontHill = Path().apply {
                moveTo(0f, h * .91f)
                cubicTo(w * .18f, h * .83f, w * .34f, h * .91f, w * .52f, h * .87f)
                cubicTo(w * .70f, h * .82f, w * .84f, h * .91f, w, h * .84f)
                lineTo(w, h)
                lineTo(0f, h)
                close()
            }
            drawPath(frontHill, Color(0xFF42A874).copy(alpha = .92f))
        }
    }
}

private fun androidx.compose.ui.graphics.drawscope.DrawScope.drawCloud(x: Float, y: Float, scale: Float) {
    val r1 = 28f * scale
    val r2 = 39f * scale
    val r3 = 25f * scale
    drawCircle(Color.White.copy(alpha = .25f), r1, androidx.compose.ui.geometry.Offset(x, y))
    drawCircle(Color.White.copy(alpha = .34f), r2, androidx.compose.ui.geometry.Offset(x + r1, y - r1 * .42f))
    drawCircle(Color.White.copy(alpha = .28f), r3, androidx.compose.ui.geometry.Offset(x + r1 * 2.0f, y + 2f))
}

private fun androidx.compose.ui.graphics.drawscope.DrawScope.drawSparkle(x: Float, y: Float, radius: Float) {
    drawLine(Color.White.copy(alpha = .55f), androidx.compose.ui.geometry.Offset(x, y - radius), androidx.compose.ui.geometry.Offset(x, y + radius), strokeWidth = 2f)
    drawLine(Color.White.copy(alpha = .55f), androidx.compose.ui.geometry.Offset(x - radius, y), androidx.compose.ui.geometry.Offset(x + radius, y), strokeWidth = 2f)
}

@Composable
private fun SplashBalloon(item: SplashItem) {
    val entry = remember { Animatable(0f) }
    val infinite = rememberInfiniteTransition(label = "${item.word}_motion")
    val floatY by infinite.animateFloat(
        -2.8f, 2.8f,
        infiniteRepeatable(tween(4300, easing = FastOutSlowInEasing), RepeatMode.Reverse),
        label = "${item.word}_float"
    )
    val rotation by infinite.animateFloat(
        -.55f, .55f,
        infiniteRepeatable(tween(5200, easing = FastOutSlowInEasing), RepeatMode.Reverse),
        label = "${item.word}_rotation"
    )

    LaunchedEffect(Unit) {
        delay(item.delay.toLong())
        entry.animateTo(1f, tween(1450, easing = FastOutSlowInEasing))
    }

    Column(
        Modifier.width(98.dp).graphicsLayer {
            translationY = 82f * (1f - entry.value) + floatY
            alpha = entry.value
            scaleX = .90f + .10f * entry.value
            scaleY = .90f + .10f * entry.value
            rotationZ = rotation * entry.value
        },
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Box(
            Modifier.width(56.dp).height(8.dp).shadow(7.dp, RoundedCornerShape(50)).background(Color.Black.copy(alpha = .13f), RoundedCornerShape(50))
        )
        Spacer(Modifier.height(3.dp))

        Box(
            Modifier.size(94.dp)
                .shadow(15.dp, CircleShape)
                .background(
                    Brush.radialGradient(
                        0.0f to Color.White.copy(alpha = .72f),
                        .28f to item.colors[0].copy(alpha = .96f),
                        1.0f to item.colors[1]
                    ), CircleShape
                )
                .border(2.dp, Color.White.copy(alpha = .60f), CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Box(
                Modifier.size(18.dp).offset(x = (-22).dp, y = (-25).dp)
                    .background(Color.White.copy(alpha = .55f), CircleShape)
            )
            Box(
                Modifier.fillMaxWidth(.84f).height(39.dp)
                    .background(Color.Black.copy(alpha = .10f), RoundedCornerShape(14.dp))
                    .border(1.dp, Color.White.copy(alpha = .30f), RoundedCornerShape(14.dp)),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    item.word,
                    fontFamily = FontFamily.Serif,
                    fontSize = if (item.word == "والأرقام") 15.sp else 17.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = Color.White,
                    textAlign = TextAlign.Center,
                    maxLines = 1,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }

        Box(Modifier.size(9.dp).background(item.colors[1], RoundedCornerShape(3.dp)))
        Box(Modifier.width(2.dp).height(20.dp).background(Color.White.copy(alpha = .72f), RoundedCornerShape(50)))
    }
}

@Composable
private fun StoneMotto() {
    Box(
        Modifier.fillMaxWidth(.92f)
            .background(Color(0xFF365E69).copy(alpha = .72f), RoundedCornerShape(22.dp))
            .border(1.dp, Color.White.copy(alpha = .28f), RoundedCornerShape(22.dp))
            .padding(horizontal = 12.dp, vertical = 11.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            "التعلّم في الصغر كالنقش بالحجر",
            fontFamily = FontFamily.Serif,
            fontSize = 18.sp,
            fontWeight = FontWeight.Black,
            color = Color.White,
            textAlign = TextAlign.Center
        )
    }
}

@Composable
private fun FullGreenProgress() {
    Box(
        Modifier.fillMaxWidth(.86f)
            .height(12.dp)
            .background(Color.White.copy(alpha = .30f), RoundedCornerShape(50))
            .border(1.dp, Color.White.copy(alpha = .48f), RoundedCornerShape(50))
            .padding(2.dp)
    ) {
        Box(
            Modifier.fillMaxSize().background(
                Brush.horizontalGradient(listOf(Color(0xFFB5FF63), Color(0xFF35DC63), Color(0xFF0AA94C))),
                RoundedCornerShape(50)
            )
        )
    }
}
