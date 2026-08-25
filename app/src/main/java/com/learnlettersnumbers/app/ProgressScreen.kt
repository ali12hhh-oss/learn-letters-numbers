package com.learnlettersnumbers.app

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun ParentProgressScreen(repo: ProgressRepository, onBack: () -> Unit, speak: (String) -> Unit) {
    var snapshot by remember { mutableStateOf(repo.load()) }
    LaunchedEffect(Unit) { snapshot = repo.load(); speak("Parent progress report") }

    CompositionLocalProvider(androidx.compose.ui.platform.LocalLayoutDirection provides androidx.compose.ui.unit.LayoutDirection.Rtl) {
        Column(
            Modifier.fillMaxSize().background(Brush.verticalGradient(listOf(MaterialTheme.colorScheme.background, MaterialTheme.colorScheme.surfaceVariant))).padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                Button(onClick = onBack, shape = RoundedCornerShape(18.dp)) { Text("رجوع") }
                Spacer(Modifier.weight(1f))
                Column(horizontalAlignment = Alignment.End) {
                    Text("متابعة الوالدين", fontSize = 28.sp, fontWeight = FontWeight.Black, color = Color(0xFF245B8A))
                    Text(if (snapshot.childName.isBlank()) "ملف الطفل غير مسجل" else snapshot.childName, fontSize = 15.sp)
                }
            }
            Spacer(Modifier.height(14.dp))
            Text("تقرير محفوظ محلياً داخل الجهاز", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = Color(0xFF356070))
            Spacer(Modifier.height(12.dp))
            StatGrid(snapshot)
            Spacer(Modifier.height(12.dp))
            Card(Modifier.fillMaxWidth(), shape = RoundedCornerShape(24.dp), elevation = CardDefaults.cardElevation(8.dp)) {
                Column(Modifier.padding(18.dp), horizontalAlignment = Alignment.End) {
                    Text("آخر نشاط", fontSize = 19.sp, fontWeight = FontWeight.Bold, color = Color(0xFF245B8A))
                    Text(snapshot.lastActivity, fontSize = 17.sp, modifier = Modifier.fillMaxWidth(), textAlign = TextAlign.Right)
                }
            }
            Spacer(Modifier.height(10.dp))
            Text("الإجابات الصحيحة والخاطئة لا تُحتسب إلا عندما يكون النشاط مزوداً بتصحيح فعلي؛ لن نعرض أرقاماً وهمية.", fontSize = 13.sp, color = Color(0xFF6A5A45), textAlign = TextAlign.Center)
            Spacer(Modifier.weight(1f))
            Button(onClick = { snapshot = repo.load(); speak("Progress updated") }, shape = RoundedCornerShape(18.dp)) { Text("تحديث التقرير") }
        }
    }
}

@Composable
private fun StatGrid(s: ProgressRepository.Snapshot) {
    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            StatCard("⭐ النجوم", s.stars.toString(), Color(0xFFFFB300), Modifier.weight(1f))
            StatCard("📚 الدروس المكتملة", s.completedLessons.toString(), Color(0xFF42A5F5), Modifier.weight(1f))
        }
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            StatCard("🔤 الحروف التي تم التدريب عليها", s.lettersLearned.toString(), Color(0xFF66BB6A), Modifier.weight(1f))
            StatCard("🔢 الأرقام التي تم التدريب عليها", s.numbersLearned.toString(), Color(0xFFAB47BC), Modifier.weight(1f))
        }
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            StatCard("✏️ مرات الكتابة", s.writingPracticed.toString(), Color(0xFFEF5350), Modifier.weight(1f))
            StatCard("🎯 إجمالي المحاولات المصححة", s.attemptedAnswers.toString(), Color(0xFF26A69A), Modifier.weight(1f))
        }
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            StatCard("✅ صحيحة", s.correctAnswers.toString(), Color(0xFF2E7D32), Modifier.weight(1f))
            StatCard("❌ خاطئة", s.wrongAnswers.toString(), Color(0xFFC62828), Modifier.weight(1f))
        }
    }
}

@Composable
private fun StatCard(title: String, value: String, color: Color, modifier: Modifier) {
    Card(modifier, shape = RoundedCornerShape(20.dp), colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface), elevation = CardDefaults.cardElevation(6.dp)) {
        Column(Modifier.fillMaxWidth().padding(14.dp), horizontalAlignment = Alignment.CenterHorizontally) {
            Text(title, fontSize = 14.sp, fontWeight = FontWeight.Bold, color = Color(0xFF555555), textAlign = TextAlign.Center)
            Spacer(Modifier.height(5.dp))
            Text(value, fontSize = 28.sp, fontWeight = FontWeight.Black, color = color)
        }
    }
}
