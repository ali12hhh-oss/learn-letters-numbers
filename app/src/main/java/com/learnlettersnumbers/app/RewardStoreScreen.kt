package com.learnlettersnumbers.app

import android.app.Activity
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

private data class RewardItem(val id: String, val title: String, val emoji: String, val price: Int)

private val rewardItems = listOf(
    RewardItem("star_badge", "وسام النجمة", "⭐", 10), RewardItem("rainbow", "قوس قزح", "🌈", 15),
    RewardItem("rocket", "صاروخ التعلم", "🚀", 20), RewardItem("trophy", "كأس بطل التعلم", "🏆", 25),
    RewardItem("medal", "ميدالية التفوق", "🏅", 30), RewardItem("crown", "تاج صغير", "👑", 35),
    RewardItem("sun", "شمس مشرقة", "☀️", 12), RewardItem("heart", "قلب الفرح", "❤️", 12),
    RewardItem("balloon", "بالون المرح", "🎈", 14), RewardItem("rocket2", "مركبة فضائية", "🛸", 28),
    RewardItem("book", "كتاب المعرفة", "📚", 18), RewardItem("pencil", "قلم المبدع", "✏️", 16),
    RewardItem("sparkles", "نجوم لامعة", "✨", 20), RewardItem("gift", "هدية مفاجأة", "🎁", 25),
    RewardItem("butterfly", "فراشة جميلة", "🦋", 18), RewardItem("rainbow2", "ألوان الفرح", "🌈", 22)
)

private val titles = listOf(
    "الشاطر", "العبقري", "بطل التعلم", "نجم الحروف", "نجم الأرقام", "صديق الكتاب", "المتعلم السريع", "المبدع الصغير", "بطل الكتابة", "بطل القراءة",
    "ذكي جداً", "رائع جداً", "متميز", "متفوق", "مكتشف صغير", "عقل لامع", "نجم اليوم", "بطل التحدي", "سريع التعلم", "محب المعرفة",
    "صاحب الهمة", "ملك الحروف", "ملك الأرقام", "سيد الكلمات", "فنان الكتابة", "بطل النجوم", "المثابر", "المجتهد", "المغامر الذكي", "أسطورة التعلم",
    "نجم المستقبل", "قائد المعرفة", "بطل الإبداع", "فارس التعلم", "نجم التفوق"
)

@Composable
fun RewardStoreScreen(repo: ProgressRepository, onBack: () -> Unit, speak: (String) -> Unit) {
    var snapshot by remember { mutableStateOf(repo.load()) }
    val ownedItems = remember { mutableStateListOf<String>().apply { addAll(repo.ownedRewards()) } }
    val context = LocalContext.current
    val adManager = remember(context) { AdMobManager(context.applicationContext) }

    LaunchedEffect(Unit) {
        snapshot = repo.load()
        speak("أهلاً بك في متجر المكافآت! هيا نتعلم ونكافئ أنفسنا.")
    }

    CompositionLocalProvider(androidx.compose.ui.platform.LocalLayoutDirection provides androidx.compose.ui.unit.LayoutDirection.Rtl) {
        Column(Modifier.fillMaxSize().verticalScroll(rememberScrollState()).background(Brush.verticalGradient(listOf(MaterialTheme.colorScheme.background, MaterialTheme.colorScheme.surfaceVariant))).padding(14.dp)) {
            Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                Button(onClick = onBack, shape = RoundedCornerShape(18.dp)) { Text("رجوع") }
                Spacer(Modifier.weight(1f))
                Column(horizontalAlignment = Alignment.End) {
                    Text("متجر المكافآت", fontSize = 28.sp, fontWeight = FontWeight.Black, color = Color(0xFF245B8A))
                    Text("⭐ ${snapshot.stars} نجمة", fontSize = 18.sp, fontWeight = FontWeight.Bold)
                }
            }
            Spacer(Modifier.height(10.dp))
            Card(Modifier.fillMaxWidth(), shape = RoundedCornerShape(22.dp), colors = CardDefaults.cardColors(containerColor = Color(0xFFFFE8A3)), elevation = CardDefaults.cardElevation(6.dp)) {
                Column(Modifier.fillMaxWidth().padding(14.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("🎁 مكافأة اختيارية", fontSize = 21.sp, fontWeight = FontWeight.Black)
                    Text("شاهد إعلاناً اختيارياً لتحصل على نجوم إضافية.", textAlign = TextAlign.Center, fontSize = 14.sp)
                    Spacer(Modifier.height(8.dp))
                    Button(onClick = {
                        val activity = context as? Activity ?: return@Button
                        adManager.showRewarded(activity) {
                            repo.addStars(10)
                            snapshot = repo.load()
                            speak("أحسنت! حصلت على عشر نجوم إضافية.")
                        }
                    }, shape = RoundedCornerShape(18.dp), colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFFFA726))) {
                        Text("▶ شاهد واحصل على 10 نجوم", fontWeight = FontWeight.ExtraBold)
                    }
                }
            }
            Spacer(Modifier.height(10.dp))
            Text("اجمع النجوم من التعلم والكتابة، ثم اختر مكافآتك وألقابك.", modifier = Modifier.fillMaxWidth(), textAlign = TextAlign.Center, fontSize = 16.sp)
            Spacer(Modifier.height(12.dp))
            Text("🎁 المكافآت والألقاب", fontSize = 23.sp, fontWeight = FontWeight.Black)
            Spacer(Modifier.height(8.dp))
            LazyVerticalGrid(columns = GridCells.Fixed(2), modifier = Modifier.fillMaxWidth().height(330.dp), verticalArrangement = Arrangement.spacedBy(9.dp), horizontalArrangement = Arrangement.spacedBy(9.dp)) {
                items(rewardItems) { item ->
                    val owned = ownedItems.contains(item.id)
                    Card(Modifier.fillMaxWidth().clickable {
                        if (!owned) {
                            if (snapshot.stars >= item.price) {
                                repo.addStars(-item.price); ownedItems.add(item.id); repo.addOwnedReward(item.id); snapshot = repo.load(); speak("أحسنت! لقد حصلت على ${item.title}. استمتع بمكافأتك!")
                            } else speak("لا توجد نجوم كافية لشراء هذه المكافأة. اجمع المزيد من النجوم وحاول مرة أخرى!")
                        }
                    }, shape = RoundedCornerShape(20.dp), colors = CardDefaults.cardColors(containerColor = if (owned) Color(0xFFE4F5E5) else MaterialTheme.colorScheme.surface), elevation = CardDefaults.cardElevation(6.dp)) {
                        Column(Modifier.fillMaxWidth().padding(8.dp), horizontalAlignment = Alignment.CenterHorizontally) { Text(item.emoji, fontSize = 35.sp); Text(item.title, fontWeight = FontWeight.Bold, textAlign = TextAlign.Center); Text(if (owned) "تم الحصول عليها ✓" else "⭐ ${item.price}", color = if (owned) Color(0xFF2E7D32) else Color(0xFF8A5A00)) }
                    }
                }
            }
            Spacer(Modifier.height(12.dp))
            Text("🏅 الألقاب التحفيزية", fontSize = 23.sp, fontWeight = FontWeight.Black)
            Text("يُحفظ اللقب ويظهر مع بطاقة الطفل بعد الشراء.", fontSize = 14.sp)
            Spacer(Modifier.height(6.dp))
            LazyVerticalGrid(columns = GridCells.Fixed(2), modifier = Modifier.fillMaxWidth().height(620.dp), verticalArrangement = Arrangement.spacedBy(7.dp), horizontalArrangement = Arrangement.spacedBy(7.dp), contentPadding = PaddingValues(bottom = 8.dp)) {
                items(titles) { title ->
                    val owned = snapshot.earnedTitles.contains(title)
                    Card(Modifier.fillMaxWidth().clickable {
                        if (!owned) {
                            if (snapshot.stars >= 40) { repo.addStars(-40); repo.addTitle(title); snapshot = repo.load(); speak("أحسنت! حصلت على لقب $title. واصل تقدمك!") }
                            else speak("لا توجد نجوم كافية للحصول على هذا اللقب. تحتاج إلى أربعين نجمة.")
                        }
                    }, shape = RoundedCornerShape(17.dp), colors = CardDefaults.cardColors(containerColor = if (owned) Color(0xFFFFF0B8) else MaterialTheme.colorScheme.surface), elevation = CardDefaults.cardElevation(4.dp)) {
                        Column(Modifier.padding(9.dp), horizontalAlignment = Alignment.CenterHorizontally) { Text("🏅", fontSize = 25.sp); Text(title, fontWeight = FontWeight.Bold, textAlign = TextAlign.Center); Text(if (owned) "مملوك ✓" else "⭐ 40", fontSize = 13.sp) }
                    }
                }
            }
        }
    }
}
