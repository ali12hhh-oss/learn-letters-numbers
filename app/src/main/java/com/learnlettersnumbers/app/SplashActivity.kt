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
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.delay

class SplashActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        window.statusBarColor = android.graphics.Color.rgb(18, 113, 188)
        window.navigationBarColor = android.graphics.Color.rgb(25, 91, 112)
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
            SplashItem("تعلّم", listOf(Color(0xFF39C6E8), Color(0xFF0879C9)), 100),
            SplashItem("الحروف", listOf(Color(0xFFFF8A4C), Color(0xFFE44375)), 650),
            SplashItem("والأرقام", listOf(Color(0xFFD45BEB), Color(0xFF7047C9)), 1200)
        )
    }

    LaunchedEffect(Unit) {
        delay(5200)
        onFinished()
    }

    val infinite = rememberInfiniteTransition(label = "splash")
    val cloudShift by infinite.animateFloat(-12f, 12f, infiniteRepeatable(tween(7000, easing = FastOutSlowInEasing), RepeatMode.Reverse), label = "cloud_shift")
    val glow by infinite.animateFloat(.72f, 1f, infiniteRepeatable(tween(2400, easing = FastOutSlowInEasing), RepeatMode.Reverse), label = "glow")

    CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Rtl) {
        Box(
            Modifier.fillMaxSize().background(
                Brush.verticalGradient(listOf(Color(0xFF075CA8), Color(0xFF12A8DE), Color(0xFF75D8E9), Color(0xFFFFD59C)))
            )
        ) {
            Box(Modifier.size(260.dp).offset(x = 145.dp, y = 55.dp).background(Color(0xFFFFF1A8).copy(alpha = .22f * glow), CircleShape))
            Box(Modifier.size(150.dp).offset(x = 200.dp, y = 110.dp).background(Color(0xFFFFF6C5).copy(alpha = .35f * glow), CircleShape))
            SplashCloud(Modifier.align(Alignment.TopStart).offset(x = cloudShift.dp, y = 125.dp), 1.0f)
            SplashCloud(Modifier.align(Alignment.TopEnd).offset(x = (-cloudShift).dp, y = 205.dp), .82f)

            Column(
                Modifier.fillMaxSize().padding(horizontal = 14.dp, vertical = 30.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Spacer(Modifier.height(205.dp))
                Text("تعلّم بمرح", fontFamily = FontFamily.Serif, fontSize = 18.sp, fontWeight = FontWeight.Bold, color = Color.White.copy(alpha = .9f))
                Spacer(Modifier.height(18.dp))

                // RTL order is intentional: right = تعلّم, center = الحروف, left = والأرقام.
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.Center, verticalAlignment = Alignment.CenterVertically) {
                    items.forEachIndexed { index, item ->
                        SplashBalloon(item)
                        if (index < items.lastIndex) Spacer(Modifier.width(7.dp))
                    }
                }

                Spacer(Modifier.height(10.dp))
                // Explicit children guarantee Arabic visual order: ١ right, ٢ center, ٣ left.
                Row(horizontalArrangement = Arrangement.Center, verticalAlignment = Alignment.CenterVertically) {
                    ArabicNumeral("١", Color(0xFF0879C9))
                    Spacer(Modifier.width(30.dp))
                    ArabicNumeral("٢", Color(0xFFE44375))
                    Spacer(Modifier.width(30.dp))
                    ArabicNumeral("٣", Color(0xFF7047C9))
                }

                Spacer(Modifier.weight(1f))
                StoneMotto()
                Spacer(Modifier.height(18.dp))
                FullGreenProgress()
                Spacer(Modifier.height(8.dp))
            }
        }
    }
}

@Composable
private fun SplashBalloon(item: SplashItem) {
    val entry = remember { Animatable(0f) }
    val infinite = rememberInfiniteTransition(label = "${item.word}_motion")
    val floatY by infinite.animateFloat(-3.5f, 3.5f, infiniteRepeatable(tween(4200, easing = FastOutSlowInEasing), RepeatMode.Reverse), label = "${item.word}_float")
    val rotation by infinite.animateFloat(-.7f, .7f, infiniteRepeatable(tween(5200, easing = FastOutSlowInEasing), RepeatMode.Reverse), label = "${item.word}_rotation")

    LaunchedEffect(Unit) {
        delay(item.delay.toLong())
        entry.animateTo(1f, tween(1500, easing = FastOutSlowInEasing))
    }

    Column(
        Modifier.width(112.dp).graphicsLayer {
            translationY = 110f * (1f - entry.value) + floatY
            alpha = entry.value
            scaleX = .88f + .12f * entry.value
            scaleY = .88f + .12f * entry.value
            rotationZ = rotation * entry.value
        },
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Box(
            Modifier.size(108.dp).shadow(13.dp, CircleShape).background(
                Brush.radialGradient(listOf(Color.White.copy(alpha = .55f), item.colors[0], item.colors[1])), CircleShape
            ).border(2.dp, Color.White.copy(alpha = .55f), CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Box(Modifier.size(24.dp).offset(x = (-23).dp, y = (-27).dp).background(Color.White.copy(alpha = .42f), CircleShape))
            Box(Modifier.fillMaxWidth(.88f).height(43.dp).background(Color.Black.copy(alpha = .12f), RoundedCornerShape(15.dp)).border(1.dp, Color.White.copy(alpha = .25f), RoundedCornerShape(15.dp)), contentAlignment = Alignment.Center) {
                Text(item.word, fontFamily = FontFamily.Serif, fontSize = if (item.word == "والأرقام") 17.sp else 19.sp, fontWeight = FontWeight.ExtraBold, color = Color.White, textAlign = TextAlign.Center, modifier = Modifier.fillMaxWidth())
            }
        }
        Spacer(Modifier.height(2.dp))
        Box(Modifier.size(8.dp).background(item.colors[1], RoundedCornerShape(3.dp)))
        Box(Modifier.width(3.dp).height(30.dp).background(item.colors[1], RoundedCornerShape(50)))
    }
}

@Composable
private fun ArabicNumeral(value: String, color: Color) {
    Box(Modifier.size(38.dp).shadow(5.dp, CircleShape).background(Color.White.copy(alpha = .9f), CircleShape).border(2.dp, color.copy(alpha = .55f), CircleShape), contentAlignment = Alignment.Center) {
        Text(value, fontSize = 23.sp, fontWeight = FontWeight.Black, color = color)
    }
}

@Composable
private fun SplashCloud(modifier: Modifier, scale: Float) {
    Row(modifier, verticalAlignment = Alignment.CenterVertically) {
        Box(Modifier.size((42 * scale).dp).background(Color.White.copy(alpha = .62f), CircleShape))
        Box(Modifier.size((62 * scale).dp).offset(x = (-12 * scale).dp).background(Color.White.copy(alpha = .78f), CircleShape))
        Box(Modifier.size((44 * scale).dp).offset(x = (-23 * scale).dp).background(Color.White.copy(alpha = .62f), CircleShape))
    }
}

@Composable
private fun StoneMotto() {
    Box(Modifier.fillMaxWidth(.91f).background(Color(0xFF536D72).copy(alpha = .68f), RoundedCornerShape(24.dp)).border(2.dp, Color.White.copy(alpha = .25f), RoundedCornerShape(24.dp)).padding(horizontal = 12.dp, vertical = 13.dp), contentAlignment = Alignment.Center) {
        Text("التعلّم في الصغر كالنقش بالحجر", fontFamily = FontFamily.Serif, fontSize = 19.sp, fontWeight = FontWeight.Black, color = Color.White, textAlign = TextAlign.Center)
    }
}

@Composable
private fun FullGreenProgress() {
    Box(Modifier.fillMaxWidth(.86f).height(13.dp).background(Color.White.copy(alpha = .32f), RoundedCornerShape(50)).border(1.dp, Color.White.copy(alpha = .5f), RoundedCornerShape(50)).padding(2.dp)) {
        Box(Modifier.fillMaxSize().background(Brush.horizontalGradient(listOf(Color(0xFF9CFF45), Color(0xFF25D65A), Color(0xFF08A94B))), RoundedCornerShape(50)))
    }
}
