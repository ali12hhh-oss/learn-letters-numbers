@file:OptIn(androidx.compose.material3.ExperimentalMaterial3Api::class)
package com.learnlettersnumbers.app

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

data class LearningStage(
    val number: Int,
    val title: String,
    val subtitle: String,
    val goal: String,
    val unlockText: String,
    val icon: String,
    val color: Color
)

private val stages = listOf(
    LearningStage(1, "أتعرف", "البداية الجميلة", "التعرف على الحروف والأرقام وسماع أصواتها.", "متاح من البداية", "🌱", Color(0xFFFFE082)),
    LearningStage(2, "أتدرب", "خطوة بعد خطوة", "التدرب على أشكال الحروف والحركات والكتابة والأرقام.", "إكمال المرحلة 1 أو جمع 10 نجوم", "⭐", Color(0xFF9FE2BF)),
    LearningStage(3, "أتقن", "أصبح أفضل", "القراءة، الجمع والطرح البسيط، والاختبارات والألعاب.", "إكمال المرحلة 2 أو 25 نجمة و5 إجابات صحيحة", "🚀", Color(0xFF9FD8F7)),
    LearningStage(4, "بطل التعلم", "التحدي الكبير", "تحديات مختلطة في العربي والإنجليزي والكتابة والقراءة.", "إكمال المرحلة 3 أو 50 نجمة و15 إجابة صحيحة و3 تدريبات كتابة", "🏆", Color(0xFFFFB7C5))
)

@Composable
fun StagesScreen(
    repo: ProgressRepository,
    onBack: () -> Unit,
    onStageOpen: (Int) -> Unit,
    speakArabic: (String) -> Unit
) {
    val snapshot by remember { mutableStateOf(repo.load()) }
    var pressedStage by remember { mutableStateOf(0) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("مراحل التعلم", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "رجوع")
                    }
                },
                actions = {
                    Text("⭐ ${snapshot.stars}", modifier = Modifier.padding(end = 12.dp), fontWeight = FontWeight.Bold)
                }
            )
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier.fillMaxSize().padding(padding).padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            item {
                Text("رحلتك التعليمية 🌟", fontSize = 28.sp, fontWeight = FontWeight.ExtraBold)
                Spacer(Modifier.height(4.dp))
                Text("كل مرحلة تفتح لك مهارات جديدة. تعلّم بهدوء، وكل إنجاز يقربك من المرحلة التالية.")
            }
            items(stages) { stage ->
                val unlocked = repo.isStageUnlocked(stage.number)
                val completed = snapshot.completedStage >= stage.number
                val scale by animateFloatAsState(if (pressedStage == stage.number) 0.97f else 1f, label = "stage")
                Card(
                    modifier = Modifier.fillMaxWidth().scale(scale).clickable(enabled = unlocked) {
                        pressedStage = stage.number
                        speakArabic("أهلاً بك في مرحلة ${stage.title}")
                        onStageOpen(stage.number)
                    },
                    shape = RoundedCornerShape(26.dp),
                    colors = CardDefaults.cardColors(containerColor = if (unlocked) stage.color else Color(0xFFE6E6E6)),
                    elevation = CardDefaults.cardElevation(defaultElevation = if (unlocked) 9.dp else 2.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(18.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(if (unlocked) stage.icon else "🔒", fontSize = 46.sp)
                        Spacer(Modifier.width(14.dp))
                        Column(Modifier.weight(1f)) {
                            Text("المرحلة ${stage.number}: ${stage.title}", fontSize = 23.sp, fontWeight = FontWeight.Bold)
                            Text(stage.subtitle, fontSize = 15.sp, fontWeight = FontWeight.SemiBold)
                            Spacer(Modifier.height(5.dp))
                            Text(stage.goal, fontSize = 14.sp)
                            Spacer(Modifier.height(7.dp))
                            Text(
                                when {
                                    completed -> "✓ مكتملة"
                                    unlocked -> "مفتوحة — ابدأ الآن"
                                    else -> "🔒 ${stage.unlockText}"
                                },
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            }
            item {
                Text(
                    "نصيحة: لا نريد السرعة، نريد التعلم والاستمتاع. 💛",
                    modifier = Modifier.fillMaxWidth().padding(8.dp),
                    fontSize = 16.sp,
                    fontWeight = FontWeight.SemiBold
                )
            }
        }
    }
}
