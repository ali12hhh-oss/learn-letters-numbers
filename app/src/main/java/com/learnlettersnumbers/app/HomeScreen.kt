package com.learnlettersnumbers.app

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.ui.platform.LocalContext
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.viewinterop.AndroidView

@Composable
fun HomeSection(
    onArabic: () -> Unit,
    onEnglish: () -> Unit,
    onProgress: () -> Unit,
    onRewards: () -> Unit,
    onTests: () -> Unit,
    onStories: () -> Unit,
    onGames: () -> Unit,
    onStages: () -> Unit,
    onSettings: () -> Unit,
    speak: (String) -> Unit
) {
    val colors = MaterialTheme.colorScheme
    val context = LocalContext.current
    val progressRepo = remember { ProgressRepository(context) }
    var childName by remember { mutableStateOf(ChildProfileRepository.loadName()) }
    var avatar by remember { mutableStateOf(ChildProfileRepository.loadAvatar()) }
    var showProfile by remember { mutableStateOf(!ChildProfileRepository.promptSeen()) }
    val imagePicker = rememberLauncherForActivityResult(ActivityResultContracts.OpenDocument()) { uri ->
        uri?.let {
            try {
                context.contentResolver.takePersistableUriPermission(
                    it,
                    android.content.Intent.FLAG_GRANT_READ_URI_PERMISSION
                )
            } catch (_: SecurityException) { }
            avatar = it.toString()
            ChildProfileRepository.saveAvatar(avatar)
        }
    }
    LaunchedEffect(Unit) { speak("أهلاً بك في تطبيق تعلم الحروف والأرقام!") }

    Column(
        Modifier.fillMaxSize().background(Brush.verticalGradient(listOf(colors.background, colors.surfaceVariant))).padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
            Column(Modifier.weight(1f)) {
                Text("تعلم الحروف والأرقام", fontSize = 27.sp, fontWeight = FontWeight.Black, color = colors.primary)
                Text("تعلم • العب • اكتب • أنجز ⭐", fontSize = 15.sp, color = colors.onBackground)
            }
            TextButton(onClick = onSettings) { Text("⚙ الإعدادات") }
        }
        Spacer(Modifier.height(8.dp))
        Card(
            Modifier.fillMaxWidth().clickable { showProfile = true },
            shape = RoundedCornerShape(26.dp),
            colors = CardDefaults.cardColors(containerColor = colors.surface),
            elevation = CardDefaults.cardElevation(7.dp)
        ) {
            Row(Modifier.fillMaxWidth().padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
                if (avatar.startsWith("content://")) {
                    AndroidView(
                        factory = { context ->
                            android.widget.ImageView(context).apply {
                                scaleType = android.widget.ImageView.ScaleType.CENTER_CROP
                                setImageURI(android.net.Uri.parse(avatar))
                            }
                        },
                        update = { it.setImageURI(android.net.Uri.parse(avatar)) },
                        modifier = Modifier.size(62.dp)
                    )
                } else {
                    val res = when (avatar) {
                        "boy" -> R.drawable.student_boy_avatar
                        "girl" -> R.drawable.student_girl_avatar
                        else -> R.drawable.child_avatar
                    }
                    Image(painterResource(res), contentDescription = "صورة الطفل", modifier = Modifier.size(62.dp))
                }
                Spacer(Modifier.width(12.dp))
                Column(Modifier.weight(1f)) {
                    Text(if (childName.isBlank()) "سجّل اسم الطفل" else "مرحباً $childName ${progressRepo.load().earnedTitles.firstOrNull()?.let { "• $it" } ?: "👋"}", fontSize = 20.sp, fontWeight = FontWeight.Bold)
                    Text("اضغط لتسجيل أو تغيير الاسم", fontSize = 13.sp)
                }
            }
        }
        Spacer(Modifier.height(12.dp))
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            HomeCard("🇮🇶", "العربية", colors.primary, Modifier.weight(1f), onArabic)
            HomeCard("🇬🇧", "English", colors.secondary, Modifier.weight(1f), onEnglish)
        }
        Spacer(Modifier.height(10.dp))
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            HomeSmall("تقدمي", "📊", colors.tertiary, Modifier.weight(1f), onProgress)
            HomeSmall("مكافأة", "🎁", colors.primary, Modifier.weight(1f), onRewards)
            HomeSmall("اختبارات", "🎯", colors.secondary, Modifier.weight(1f), onTests)
        }
        Spacer(Modifier.height(8.dp))
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            HomeSmall("قصص", "📚", Color(0xFF3BA99C), Modifier.weight(1f), onStories)
            HomeSmall("ألعاب", "🎮", Color(0xFFE78AC3), Modifier.weight(1f), onGames)
            HomeSmall("مراحل", "🏆", Color(0xFFE6A23C), Modifier.weight(1f), onStages)
        }
    }

    if (showProfile) {
        AlertDialog(
            onDismissRequest = { if (childName.isNotBlank()) showProfile = false },
            title = { Text("مرحباً بك 🌟") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text("اكتب اسم الطفل، اختر صورة طالب أو طالبة، أو اختر صورة من الجهاز. يمكنك التغيير لاحقاً بالضغط على البطاقة.")
                    var draft by remember(childName) { mutableStateOf(childName) }
                    OutlinedTextField(value = draft, onValueChange = { draft = it }, label = { Text("اسم الطفل") }, singleLine = true)
                    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        OutlinedButton(onClick = { avatar = "boy"; ChildProfileRepository.saveAvatar(avatar) }, modifier = Modifier.weight(1f)) { Text("👦 طالب") }
                        OutlinedButton(onClick = { avatar = "girl"; ChildProfileRepository.saveAvatar(avatar) }, modifier = Modifier.weight(1f)) { Text("👧 طالبة") }
                        OutlinedButton(onClick = { imagePicker.launch("image/*") }, modifier = Modifier.weight(1f)) { Text("📷 من الجهاز") }
                    }
                    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        TextButton(onClick = { ChildProfileRepository.markPromptSeen(); showProfile = false }) { Text("تخطي") }
                        Button(onClick = { ChildProfileRepository.saveName(draft.trim()); progressRepo.setChildName(draft.trim()); childName = draft.trim(); showProfile = false }) { Text("حفظ") }
                    }
                }
            },
            confirmButton = {}
        )
    }
}

@Composable
private fun HomeCard(icon: String, title: String, color: Color, modifier: Modifier, onClick: () -> Unit) {
    Card(modifier.clickable(onClick = onClick), shape = RoundedCornerShape(28.dp), colors = CardDefaults.cardColors(containerColor = color), elevation = CardDefaults.cardElevation(9.dp)) {
        Column(Modifier.fillMaxWidth().padding(18.dp), horizontalAlignment = Alignment.CenterHorizontally) {
            Text(icon, fontSize = 42.sp)
            Text(title, color = Color.White, fontSize = 23.sp, fontWeight = FontWeight.Black)
        }
    }
}

@Composable
private fun HomeSmall(title: String, icon: String, color: Color, modifier: Modifier, onClick: () -> Unit) {
    Card(modifier.clickable(onClick = onClick), shape = RoundedCornerShape(20.dp), colors = CardDefaults.cardColors(containerColor = color), elevation = CardDefaults.cardElevation(6.dp)) {
        Column(Modifier.fillMaxWidth().padding(vertical = 12.dp), horizontalAlignment = Alignment.CenterHorizontally) {
            Text(icon, fontSize = 26.sp)
            Text(title, color = Color.White, fontSize = 14.sp, fontWeight = FontWeight.Bold, textAlign = TextAlign.Center)
        }
    }
}
