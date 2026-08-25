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
    Card(Modifier.fillMaxWidth(), shape = RoundedCornerShape(24.dp), elevation = CardDefaults.cardElevation(6.dp), onClick = onClick) {
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
