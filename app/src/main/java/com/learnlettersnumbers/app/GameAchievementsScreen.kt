package com.learnlettersnumbers.app

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.platform.LocalContext

/**
 * شاشة إنجازات الألعاب.
 * لا تحتاج إلى إنترنت؛ تقرأ التقدم المحفوظ محليًا من GameAchievements.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GameAchievementsScreen(onBack: () -> Unit) {
    val context = LocalContext.current
    var refreshKey by remember { mutableStateOf(0) }

    LaunchedEffect(refreshKey) {
        // إعادة تركيب الشاشة بعد العودة من لعبة لتحديث نسب الإنجازات.
    }

    val achievements = GameAchievements.all
    val unlockedCount = achievements.count { GameAchievements.isUnlocked(context, it.id) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("🏆 إنجازاتي", fontWeight = FontWeight.ExtraBold) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "رجوع")
                    }
                }
            )
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(26.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.primaryContainer
                    )
                ) {
                    Column(Modifier.padding(20.dp)) {
                        Text("رحلة الأبطال 🌟", fontSize = 25.sp, fontWeight = FontWeight.ExtraBold)
                        Spacer(Modifier.height(5.dp))
                        Text("فتحت $unlockedCount من ${achievements.size} إنجازات", fontSize = 15.sp)
                        Spacer(Modifier.height(12.dp))
                        LinearProgressIndicator(
                            progress = { if (achievements.isEmpty()) 0f else unlockedCount.toFloat() / achievements.size },
                            modifier = Modifier.fillMaxWidth().height(9.dp)
                        )
                    }
                }
            }

            items(achievements, key = { it.id }) { achievement ->
                val unlocked = GameAchievements.isUnlocked(context, achievement.id)
                val progress = GameAchievements.progress(context, achievement.id)
                val target = achievement.target.coerceAtLeast(1)
                val fraction = (progress.toFloat() / target.toFloat()).coerceIn(0f, 1f)

                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(22.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = if (unlocked) {
                            MaterialTheme.colorScheme.secondaryContainer
                        } else {
                            MaterialTheme.colorScheme.surfaceVariant
                        }
                    )
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(if (unlocked) achievement.icon else "🔒", fontSize = 38.sp)
                        Spacer(Modifier.padding(horizontal = 6.dp))
                        Column(Modifier.weight(1f)) {
                            Text(
                                achievement.title,
                                fontSize = 18.sp,
                                fontWeight = FontWeight.ExtraBold
                            )
                            Text(achievement.description, fontSize = 13.sp)
                            Spacer(Modifier.height(7.dp))
                            LinearProgressIndicator(
                                progress = { fraction },
                                modifier = Modifier.fillMaxWidth().height(7.dp)
                            )
                            Spacer(Modifier.height(3.dp))
                            Text(
                                if (unlocked) "تم فتحه ✓" else "$progress / $target",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            }

            item {
                Spacer(Modifier.height(4.dp))
                Button(
                    onClick = { refreshKey++ },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("تحديث الإنجازات 🔄")
                }
            }
        }
    }
}
