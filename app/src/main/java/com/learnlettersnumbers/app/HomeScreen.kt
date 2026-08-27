package com.learnlettersnumbers.app

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.foundation.layout.BoxWithConstraints

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
    var showLearnMenu by remember { mutableStateOf(false) }
    val imagePicker = rememberLauncherForActivityResult(ActivityResultContracts.OpenDocument()) { uri ->
        uri?.let {
            try {
                context.contentResolver.takePersistableUriPermission(it, android.content.Intent.FLAG_GRANT_READ_URI_PERMISSION)
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
            val w = maxWidth
            val h = maxHeight

            Image(
                painter = painterResource(R.drawable.home_screen),
                contentDescription = "الصفحة الرئيسية",
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.FillBounds
            )

            HomeHotspot(Modifier.offset(x = w * 0.025f, y = h * 0.018f).size(w * 0.085f, h * 0.065f), onSettings)
            HomeHotspot(Modifier.offset(x = w * 0.115f, y = h * 0.018f).size(w * 0.085f, h * 0.065f), onSettings)
            HomeHotspot(Modifier.offset(x = w * 0.665f, y = h * 0.025f).size(w * 0.305f, h * 0.115f)) { showProfile = true }

            // الصف الأول في الصورة: الألعاب، تدرب، تعلم، إنجازاتي.
            HomeHotspot(Modifier.offset(x = w * 0.035f, y = h * 0.395f).size(w * 0.215f, h * 0.205f), onGames)
            HomeHotspot(Modifier.offset(x = w * 0.295f, y = h * 0.395f).size(w * 0.215f, h * 0.205f), onTests)
            HomeHotspot(Modifier.offset(x = w * 0.555f, y = h * 0.395f).size(w * 0.215f, h * 0.205f)) { showLearnMenu = true }
            HomeHotspot(Modifier.offset(x = w * 0.805f, y = h * 0.395f).size(w * 0.17f, h * 0.205f), onProgress)

            // الصف الثاني: القصص، الاختبارات، المتجر.
            HomeHotspot(Modifier.offset(x = w * 0.035f, y = h * 0.635f).size(w * 0.325f, h * 0.205f), onStories)
            HomeHotspot(Modifier.offset(x = w * 0.39f, y = h * 0.635f).size(w * 0.255f, h * 0.205f), onTests)
            HomeHotspot(Modifier.offset(x = w * 0.675f, y = h * 0.635f).size(w * 0.30f, h * 0.205f), onRewards)

            HomeHotspot(Modifier.offset(x = w * 0.735f, y = h * 0.835f).size(w * 0.215f, h * 0.075f), onProgress)

            // شريط RTL كما في الصورة: المزيد، تقدمي، التعلم، الرئيسية؛ الرئيسية في أقصى اليمين.
            HomeHotspot(Modifier.offset(x = w * 0.02f, y = h * 0.925f).size(w * 0.20f, h * 0.065f), onStages)
            HomeHotspot(Modifier.offset(x = w * 0.255f, y = h * 0.925f).size(w * 0.20f, h * 0.065f), onProgress)
            HomeHotspot(Modifier.offset(x = w * 0.49f, y = h * 0.925f).size(w * 0.20f, h * 0.065f)) { showLearnMenu = true }
            HomeHotspot(Modifier.offset(x = w * 0.745f, y = h * 0.925f).size(w * 0.235f, h * 0.065f)) { }
        }
    }

    if (showLearnMenu) {
        AlertDialog(
            onDismissRequest = { showLearnMenu = false },
            title = { Text("التعلّم") },
            text = { Text("اختر اللغة التي تريد التعلّم بها") },
            confirmButton = {
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Button(onClick = { showLearnMenu = false; onArabic() }) { Text("العربية") }
                    Button(onClick = { showLearnMenu = false; onEnglish() }) { Text("English") }
                }
            }
        )
    }

    if (showProfile) {
        AlertDialog(
            onDismissRequest = { if (childName.isNotBlank()) showProfile = false },
            title = { Text("مرحباً بك 🌟") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text("اكتب اسم الطفل، اختر صورة طالب أو طالبة، أو اختر صورة من الجهاز. يمكنك التغيير لاحقاً بالضغط على بطاقة الطفل.")
                    var draft by remember(childName) { mutableStateOf(childName) }
                    OutlinedTextField(value = draft, onValueChange = { draft = it }, label = { Text("اسم الطفل") }, singleLine = true)
                    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        OutlinedButton(onClick = { avatar = "boy"; ChildProfileRepository.saveAvatar(avatar) }, modifier = Modifier.weight(1f)) { Text("👦 طالب") }
                        OutlinedButton(onClick = { avatar = "girl"; ChildProfileRepository.saveAvatar(avatar) }, modifier = Modifier.weight(1f)) { Text("👧 طالبة") }
                        OutlinedButton(onClick = { imagePicker.launch(arrayOf("image/*")) }, modifier = Modifier.weight(1f)) { Text("📷 من الجهاز") }
                    }
                    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        TextButton(onClick = { ChildProfileRepository.markPromptSeen(); showProfile = false }) { Text("تخطي") }
                        Button(onClick = {
                            val savedName = draft.trim()
                            ChildProfileRepository.saveName(savedName)
                            ProgressRepository(context).setChildName(savedName)
                            childName = savedName
                            showProfile = false
                        }) { Text("حفظ") }
                    }
                }
            },
            confirmButton = {}
        )
    }
}

@Composable
private fun HomeHotspot(modifier: Modifier, onClick: () -> Unit = {}) {
    Box(modifier = modifier.clickable(onClick = onClick))
}
