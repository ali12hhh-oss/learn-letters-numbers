package com.learnlettersnumbers.app

import android.content.Intent
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.DialogProperties

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
    val context = LocalContext.current
    var childName by remember { mutableStateOf(ChildProfileRepository.loadName()) }
    var avatar by remember { mutableStateOf(ChildProfileRepository.loadAvatar()) }
    var showProfile by remember { mutableStateOf(!ChildProfileRepository.promptSeen()) }
    val imagePicker = rememberLauncherForActivityResult(ActivityResultContracts.OpenDocument()) { uri ->
        uri?.let {
            try {
                context.contentResolver.takePersistableUriPermission(it, Intent.FLAG_GRANT_READ_URI_PERMISSION)
            } catch (_: SecurityException) { }
            avatar = it.toString()
            ChildProfileRepository.saveAvatar(avatar)
        }
    }

    LaunchedEffect(Unit) { speak("أهلاً بك في تطبيق تعلم الحروف والأرقام!") }

    CompositionLocalProvider(
        androidx.compose.ui.platform.LocalLayoutDirection provides androidx.compose.ui.unit.LayoutDirection.Rtl
    ) {
        BoxWithConstraints(Modifier.fillMaxSize()) {
            Box(
                Modifier.fillMaxSize().background(
                    Brush.verticalGradient(listOf(Color(0xFF0879D1), Color(0xFF48C9F5), Color(0xFF8BD86B)))
                )
            )
            Image(
                painter = painterResource(R.drawable.home_background),
                contentDescription = null,
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.Crop
            )

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .statusBarsPadding()
                    .navigationBarsPadding()
                    .padding(horizontal = 12.dp, vertical = 8.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    HomeRoundButton("⚙", "الإعدادات", Color(0xFF6D35C9), onSettings)
                    HomeProfileCard(childName, avatar) { showProfile = true }
                }

                Spacer(Modifier.height(8.dp))

                Card(
                    modifier = Modifier.fillMaxWidth(0.86f),
                    shape = RoundedCornerShape(28.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White.copy(alpha = .88f)),
                    elevation = CardDefaults.cardElevation(8.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(horizontal = 18.dp, vertical = 8.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text("تعلم الحروف والأرقام", fontSize = 30.sp, fontWeight = FontWeight.Black, color = Color(0xFFEF8A00), textAlign = TextAlign.Center)
                        Text("تعلم • العب • اكتب • أنجز ⭐", fontSize = 15.sp, fontWeight = FontWeight.Bold, color = Color(0xFF34526F), textAlign = TextAlign.Center)
                    }
                }

                Spacer(Modifier.height(10.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    HomeLanguageCard(Modifier.weight(1f), R.drawable.arabic_card, "العربية", "حروف • أرقام • نطق\nألعاب • قصص • اختبارات", onArabic)
                    HomeLanguageCard(Modifier.weight(1f), R.drawable.english_card, "English", "Letters • Numbers • Pronunciation\nGames • Stories • Tests", onEnglish)
                }

                Spacer(Modifier.height(10.dp))

                Text(
                    "اختر ما تريد أن تتعلمه",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = Color.White,
                    modifier = Modifier
                        .background(Color(0xFF185B8E).copy(alpha = .72f), RoundedCornerShape(16.dp))
                        .padding(horizontal = 14.dp, vertical = 5.dp)
                )

                Spacer(Modifier.height(6.dp))

                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(7.dp)) {
                    HomeFeatureButton("📊", "تقدمي", Color(0xFF00AFAF), Modifier.weight(1f), onProgress)
                    HomeFeatureButton("🎁", "مكافآت", Color(0xFF1777D3), Modifier.weight(1f), onRewards)
                    HomeFeatureButton("📝", "اختبارات", Color(0xFFE88A19), Modifier.weight(1f), onTests)
                    HomeFeatureButton("📖", "قصص", Color(0xFFD83B72), Modifier.weight(1f), onStories)
                    HomeFeatureButton("🎮", "ألعاب", Color(0xFF4FAE32), Modifier.weight(1f), onGames)
                    HomeFeatureButton("🏆", "المراحل", Color(0xFF6A35C5), Modifier.weight(1f), onStages)
                }

                Spacer(Modifier.weight(1f))

                Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.Bottom) {
                    Text(
                        "أحسنت! ⭐ استمر هكذا لتصبح الأفضل",
                        modifier = Modifier
                            .weight(1f)
                            .padding(horizontal = 8.dp)
                            .background(Color(0xFFFFE08A), RoundedCornerShape(20.dp))
                            .padding(horizontal = 10.dp, vertical = 9.dp),
                        color = Color(0xFFB83B28),
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Black,
                        textAlign = TextAlign.Center
                    )
                    Spacer(Modifier.width(8.dp))
                    HomeRoundButton("🔊", "الصوت", Color(0xFF6531C5), onSettings)
                }
            }
        }
    }

    if (showProfile) {
        AlertDialog(
            onDismissRequest = { if (childName.isNotBlank()) showProfile = false },
            title = { Text("مرحباً بك 🌟") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text("اكتب اسم الطفل، اختر صورة طالب أو طالبة، أو اختر صورة من الجهاز.")
                    var draft by remember(childName) { mutableStateOf(childName) }
                    OutlinedTextField(value = draft, onValueChange = { draft = it }, label = { Text("اسم الطفل") }, singleLine = true, modifier = Modifier.fillMaxWidth())
                    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        OutlinedButton(onClick = { avatar = "boy"; ChildProfileRepository.saveAvatar(avatar) }, modifier = Modifier.weight(1f)) { Text("👦 طالب") }
                        OutlinedButton(onClick = { avatar = "girl"; ChildProfileRepository.saveAvatar(avatar) }, modifier = Modifier.weight(1f)) { Text("👧 طالبة") }
                        OutlinedButton(onClick = { imagePicker.launch(arrayOf("image/*")) }, modifier = Modifier.weight(1f)) { Text("📷") }
                    }
                    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.CenterVertically) {
                        TextButton(onClick = { ChildProfileRepository.markPromptSeen(); showProfile = false }) { Text("تخطي") }
                        Button(onClick = {
                            val savedName = draft.trim()
                            ChildProfileRepository.saveName(savedName)
                            ProgressRepository(context).setChildName(savedName)
                            childName = savedName
                            ChildProfileRepository.markPromptSeen()
                            showProfile = false
                        }) { Text("حفظ") }
                    }
                }
            },
            confirmButton = {},
            properties = DialogProperties(dismissOnBackPress = true, dismissOnClickOutside = childName.isNotBlank())
        )
    }
}

@Composable
private fun HomeLanguageCard(modifier: Modifier, image: Int, title: String, subtitle: String, onClick: () -> Unit) {
    var pressed by remember { mutableStateOf(false) }
    val scale by androidx.compose.animation.core.animateFloatAsState(if (pressed) .96f else 1f, label = "language_card_scale")
    Card(
        modifier = modifier.scale(scale).clickable {
            pressed = true
            onClick()
            pressed = false
        },
        shape = RoundedCornerShape(28.dp),
        elevation = CardDefaults.cardElevation(10.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White.copy(alpha = .94f))
    ) {
        Column(Modifier.padding(7.dp), horizontalAlignment = Alignment.CenterHorizontally) {
            Image(painterResource(image), contentDescription = title, modifier = Modifier.fillMaxWidth().height(112.dp).clip(RoundedCornerShape(22.dp)), contentScale = ContentScale.Crop)
            Text(title, fontSize = 22.sp, fontWeight = FontWeight.Black, color = Color(0xFF214C72))
            Text(subtitle, fontSize = 10.sp, fontWeight = FontWeight.Bold, textAlign = TextAlign.Center, color = Color(0xFF35566F))
            Spacer(Modifier.height(5.dp))
            Text("اضغط للدخول", fontSize = 12.sp, fontWeight = FontWeight.Black, color = Color.White, modifier = Modifier.background(Color(0xFF1786D5), RoundedCornerShape(14.dp)).padding(horizontal = 12.dp, vertical = 4.dp))
        }
    }
}

@Composable
private fun HomeFeatureButton(icon: String, title: String, color: Color, modifier: Modifier, onClick: () -> Unit) {
    var pressed by remember { mutableStateOf(false) }
    val scale by androidx.compose.animation.core.animateFloatAsState(if (pressed) .93f else 1f, label = "feature_button_scale")
    Surface(
        modifier = modifier.scale(scale).clickable {
            pressed = true
            onClick()
            pressed = false
        },
        shape = RoundedCornerShape(18.dp),
        color = color,
        shadowElevation = 7.dp
    ) {
        Column(Modifier.padding(vertical = 7.dp, horizontal = 2.dp), horizontalAlignment = Alignment.CenterHorizontally) {
            Text(icon, fontSize = 25.sp)
            Text(title, color = Color.White, fontSize = 11.sp, fontWeight = FontWeight.Black, textAlign = TextAlign.Center)
        }
    }
}

@Composable
private fun HomeRoundButton(icon: String, title: String, color: Color, onClick: () -> Unit) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Surface(Modifier.size(50.dp).clickable(onClick = onClick), shape = androidx.compose.foundation.shape.CircleShape, color = color, shadowElevation = 8.dp) {
            Box(contentAlignment = Alignment.Center) { Text(icon, fontSize = 25.sp) }
        }
        Text(title, color = Color.White, fontSize = 10.sp, fontWeight = FontWeight.Bold)
    }
}

@Composable
private fun HomeProfileCard(childName: String, avatar: String, onClick: () -> Unit) {
    Surface(
        modifier = Modifier.widthIn(max = 235.dp).clickable(onClick = onClick),
        shape = RoundedCornerShape(26.dp),
        color = Color(0xFFFFF0D2).copy(alpha = .95f),
        shadowElevation = 8.dp
    ) {
        Row(Modifier.padding(horizontal = 10.dp, vertical = 7.dp), verticalAlignment = Alignment.CenterVertically) {
            val avatarRes = when (avatar) {
                "boy" -> R.drawable.student_boy_avatar
                "girl" -> R.drawable.student_girl_avatar
                else -> R.drawable.child_avatar
            }
            Image(painterResource(avatarRes), contentDescription = "صورة الطفل", modifier = Modifier.size(50.dp), contentScale = ContentScale.Crop)
            Spacer(Modifier.width(8.dp))
            Column(horizontalAlignment = Alignment.End) {
                Text(if (childName.isBlank()) "مرحباً بك 🌟" else "مرحباً $childName", fontSize = 17.sp, fontWeight = FontWeight.Black, color = Color(0xFF59331E))
                Text("⭐  اضغط لتعديل الملف", fontSize = 10.sp, color = Color(0xFF7D5737))
            }
        }
    }
}
