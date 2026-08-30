@file:OptIn(androidx.compose.material3.ExperimentalMaterial3Api::class)
package com.learnlettersnumbers.app

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun SettingsScreen(
    repo: SettingsRepository,
    darkMode: Boolean,
    onDarkModeChanged: (Boolean) -> Unit,
    onSoundsChanged: (Boolean) -> Unit = {},
    onEffectsChanged: (Boolean) -> Unit = {},
    onBack: () -> Unit
) {
    var sounds by remember { mutableStateOf(repo.soundsEnabled()) }
    var effects by remember { mutableStateOf(repo.effectsEnabled()) }
    var showUsage by remember { mutableStateOf(false) }
    var showPrivacy by remember { mutableStateOf(false) }
    var showTerms by remember { mutableStateOf(false) }
    var showAbout by remember { mutableStateOf(false) }
    var showDelete by remember { mutableStateOf(false) }
    var showAudioTest by remember { mutableStateOf(false) }
    val context = LocalContext.current
    val audio = remember { LocalAudioManager(context.applicationContext) }

    DisposableEffect(Unit) {
        onDispose { audio.releaseAll() }
    }

    val bg = if (darkMode) {
        Brush.verticalGradient(listOf(Color(0xFF26365F), Color(0xFF493B78), Color(0xFF355184)))
    } else {
        Brush.verticalGradient(listOf(Color(0xFFBFE9FF), Color(0xFFC9F7D5), Color(0xFFFFD6B3)))
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("الإعدادات") },
                navigationIcon = { TextButton(onClick = onBack) { Text("رجوع") } }
            )
        }
    ) { padding ->
        Column(
            Modifier.fillMaxSize().background(bg).verticalScroll(rememberScrollState())
                .padding(padding).padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text("إعدادات التطبيق", fontSize = 27.sp, fontWeight = FontWeight.Bold)

            SettingSwitchCard("🔊 الأصوات", "تشغيل أو إيقاف النطق والترحيب والأصوات التشجيعية.", sounds) {
                sounds = it; repo.setSoundsEnabled(it); onSoundsChanged(it)
            }
            SettingSwitchCard("✨ المؤثرات", "تشغيل أو إيقاف مؤثرات الضغط والحركة والرسوم.", effects) {
                effects = it; repo.setEffectsEnabled(it); onEffectsChanged(it)
            }
            SettingSwitchCard(
                if (darkMode) "🌙 الوضع الليلي" else "☀️ الوضع النهاري",
                "ألوان زاهية في الوضعين، بدون خلفية سوداء أو بيضاء صافية.",
                darkMode
            ) {
                repo.setDarkMode(it); onDarkModeChanged(it)
            }

            AudioTestCard(audio, showAudioTest) { showAudioTest = !showAudioTest }

            InfoButton("📘 تعليم استخدام التطبيق", "شرح الأقسام والأزرار وطريقة التعلم") { showUsage = true }
            InfoButton("🔐 الخصوصية", "البيانات المحلية وسياسة الخصوصية") { showPrivacy = true }
            InfoButton("📜 شروط الاستخدام", "قواعد استخدام التطبيق") { showTerms = true }
            InfoButton("⭐ حول البرنامج وقيمه", "الفكرة التعليمية والأهداف والقيم") { showAbout = true }
            InfoButton("🗑️ حذف بيانات الطفل", "حذف الاسم والصورة والتقدم والنجوم والألقاب المحلية") { showDelete = true }

            Text("التطبيق مصمم لتعمل الميزات التعليمية الأساسية دون اتصال بالإنترنت.", fontSize = 13.sp)
            Text("الإصدار الموحد: 1.0.0", fontSize = 14.sp, fontWeight = FontWeight.Bold)
        }
    }

    if (showUsage) DetailDialog("تعليم استخدام التطبيق", """
ابدأ بتسجيل اسم الطفل واختيار صورته أو تخطي التسجيل.
العربية: الحروف، الأرقام، تعلم الكتابة، والقراءة.
English: Letters، Numbers، Learn to Write، وWriting.
تقدمي يعرض إنجاز الطفل، ومكافأة للنجوم والمتجر، والاختبارات والقصص والمراحل.
يمكن تشغيل أو إيقاف الأصوات والمؤثرات من هذه الصفحة.
""".trimIndent()) { showUsage = false }

    if (showPrivacy) DetailDialog("الخصوصية", """
يحفظ التطبيق ملف الطفل والتقدم والنجوم والألقاب محلياً على الجهاز بحسب الوظائف الموجودة.
الميزات التعليمية الأساسية لا تتطلب اتصالاً بالإنترنت.
قبل النشر على Google Play يجب أن تكون هناك سياسة خصوصية عامة على رابط HTTPS، وأن تطابق سياسة الخصوصية وData Safety البيانات الفعلية التي يجمعها التطبيق وأي مكتبات خارجية.
""".trimIndent()) { showPrivacy = false }

    if (showTerms) DetailDialog("شروط الاستخدام", """
هذا تطبيق تعليمي للأطفال للتعلم والترفيه التعليمي.
المكافآت والنجوم داخل التطبيق تحفيزية وليست أموالاً.
يفضل استخدام التطبيق تحت إشراف ولي الأمر.
لا تدخل معلومات حساسة أو غير ضرورية في ملف الطفل.
يمكن حذف البيانات المحلية من زر حذف بيانات الطفل.
""".trimIndent()) { showTerms = false }

    if (showAbout) DetailDialog("حول البرنامج وقيمه", """
تعلم الحروف والأرقام تطبيق تعليمي يركز على التعلم بالممارسة والصوت والتفاعل.
قيمه: التعلم المرح، التدرج، التشجيع، احترام خصوصية الطفل، وتقليل الاعتماد على الإنترنت، مع إعطاء ولي الأمر صورة واضحة عن التقدم.
""".trimIndent()) { showAbout = false }

    if (showDelete) AlertDialog(
        onDismissRequest = { showDelete = false },
        title = { Text("حذف بيانات الطفل؟") },
        text = { Text("سيتم حذف البيانات المحلية للطفل والتقدم. لا يمكن التراجع عن العملية.") },
        confirmButton = {
            Button(onClick = { repo.clearChildData(); showDelete = false }) { Text("حذف") }
        },
        dismissButton = { TextButton(onClick = { showDelete = false }) { Text("إلغاء") } }
    )
}

@Composable
private fun AudioTestCard(audio: LocalAudioManager, expanded: Boolean, onToggle: () -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(24.dp),
        elevation = CardDefaults.cardElevation(7.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Column(Modifier.fillMaxWidth().padding(16.dp), verticalArrangement = Arrangement.spacedBy(9.dp)) {
            Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                Column(Modifier.weight(1f)) {
                    Text("🎧 اختبار الصوت", fontSize = 20.sp, fontWeight = FontWeight.Bold)
                    Text("تجربة عينات عربية وإنجليزية محلياً دون إنترنت", fontSize = 13.sp)
                }
                TextButton(onClick = onToggle) { Text(if (expanded) "إخفاء" else "فتح") }
            }

            if (expanded) {
                Text("الحروف العربية", fontWeight = FontWeight.Bold, fontSize = 16.sp)
                SampleRow(listOf("أ" to "ar_letter_01_sound", "ب" to "ar_letter_02_sound", "ح" to "ar_letter_06_sound", "م" to "ar_letter_24_sound", "ي" to "ar_letter_28_sound")) { audio.playRequired(it) }

                Text("الأرقام العربية", fontWeight = FontWeight.Bold, fontSize = 16.sp)
                SampleRow(listOf("١" to "ar_number_001", "٢" to "ar_number_002", "٥" to "ar_number_005", "١٠" to "ar_number_010", "٢٠" to "ar_number_020")) { audio.playRequired(it) }

                Text("English letters", fontWeight = FontWeight.Bold, fontSize = 16.sp)
                SampleRow(listOf("A" to "en_letter_01_sound", "B" to "en_letter_02_sound", "C" to "en_letter_03_sound", "M" to "en_letter_13_sound", "S" to "en_letter_19_sound")) { audio.playRequired(it) }

                Text("English numbers", fontWeight = FontWeight.Bold, fontSize = 16.sp)
                SampleRow(listOf("1" to "en_number_001", "2" to "en_number_002", "5" to "en_number_005", "10" to "en_number_010", "20" to "en_number_020")) { audio.playRequired(it) }

                Text("رسائل تشجيع", fontWeight = FontWeight.Bold, fontSize = 16.sp)
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Button(onClick = { audio.speakOffline("أحسنت! عمل رائع!", "ar") }, modifier = Modifier.weight(1f)) { Text("عربي") }
                    Button(onClick = { audio.speakOffline("Great job! Keep going!", "en") }, modifier = Modifier.weight(1f)) { Text("English") }
                }
                Text("ملاحظة: رسائل التشجيع التجريبية تستخدم صوت Android المثبت على الجهاز فقط إذا كان الصوت غير متصل بالشبكة.", fontSize = 12.sp)
            }
        }
    }
}

@Composable
private fun SampleRow(samples: List<Pair<String, String>>, play: (String) -> Unit) {
    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(6.dp)) {
        samples.forEach { (label, resource) ->
            Button(
                onClick = { play(resource) },
                modifier = Modifier.weight(1f),
                contentPadding = PaddingValues(horizontal = 3.dp, vertical = 5.dp),
                shape = RoundedCornerShape(14.dp)
            ) { Text("🔊 $label", fontSize = 13.sp, maxLines = 1) }
        }
    }
}

@Composable
private fun SettingSwitchCard(title: String, subtitle: String, checked: Boolean, onChecked: (Boolean) -> Unit) {
    Card(Modifier.fillMaxWidth(), shape = RoundedCornerShape(24.dp), elevation = CardDefaults.cardElevation(7.dp)) {
        Row(Modifier.fillMaxWidth().padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
            Column(Modifier.weight(1f)) {
                Text(title, fontSize = 20.sp, fontWeight = FontWeight.Bold)
                Text(subtitle, fontSize = 13.sp)
            }
            Switch(checked = checked, onCheckedChange = onChecked)
        }
    }
}

@Composable
private fun InfoButton(title: String, subtitle: String, onClick: () -> Unit) {
    Card(modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(24.dp), elevation = CardDefaults.cardElevation(6.dp), onClick = onClick) {
        Column(Modifier.padding(16.dp)) {
            Text(title, fontSize = 19.sp, fontWeight = FontWeight.Bold)
            Text(subtitle, fontSize = 13.sp)
        }
    }
}

@Composable
private fun DetailDialog(title: String, body: String, onClose: () -> Unit) {
    AlertDialog(
        onDismissRequest = onClose,
        title = { Text(title) },
        text = { Text(body, lineHeight = 21.sp) },
        confirmButton = { Button(onClick = onClose) { Text("حسناً") } }
    )
}
