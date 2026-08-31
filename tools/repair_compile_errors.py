from pathlib import Path
import re

p = Path('app/src/main/java/com/learnlettersnumbers/app/NumbersScreen.kt')
s = p.read_text(encoding='utf-8')
old = '@Composable private fun PracticeCard(example:OperationExample,answer:Int?,answered:Boolean,onSpeak:()->Unit,onAnswer:(Int)->Unit,onNext:()->Unit)'
new = '@Composable private fun ColumnScope.PracticeCard(example:OperationExample,answer:Int?,answered:Boolean,onSpeak:()->Unit,onAnswer:(Int)->Unit,onNext:()->Unit)'
if old in s:
    s = s.replace(old, new, 1)
    p.write_text(s, encoding='utf-8')

games = Path('app/src/main/java/com/learnlettersnumbers/app/GamesScreen.kt')
s = games.read_text(encoding='utf-8')
if 'import androidx.compose.foundation.horizontalScroll' not in s:
    s = s.replace('import androidx.compose.foundation.clickable\n', 'import androidx.compose.foundation.clickable\nimport androidx.compose.foundation.horizontalScroll\nimport androidx.compose.foundation.rememberScrollState\n', 1)

intro = re.compile(r'            Card\(Modifier\.fillMaxWidth\(\)\.padding\(top = 8\.dp\).*?            Spacer\(Modifier\.height\(10\.dp\)\)\n            Text\("اختر الفئة".*?            Spacer\(Modifier\.height\(10\.dp\)\)\n', re.S)
compact = '''            Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                Column(Modifier.weight(1f)) {
                    Text("🎮 الألعاب", fontSize = 24.sp, fontWeight = FontWeight.ExtraBold)
                    Text("اختر لعبة وابدأ", fontSize = 13.sp, color = Color(0xFF667085))
                }
                Surface(shape = RoundedCornerShape(18.dp), color = MaterialTheme.colorScheme.primaryContainer) {
                    Text("🏅 $completed", Modifier.padding(horizontal = 12.dp, vertical = 8.dp), fontWeight = FontWeight.Bold, fontSize = 13.sp)
                }
            }
            Spacer(Modifier.height(8.dp))
            Row(
                Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(7.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                categories.forEach { c ->
                    FilterChip(selected = category == c, onClick = { category = c }, label = { Text(c, fontSize = 12.sp) })
                }
            }
            Spacer(Modifier.height(8.dp))
'''
if intro.search(s):
    s = intro.sub(compact, s, count=1)
s = s.replace('            Text("الألعاب", fontSize = 22.sp, fontWeight = FontWeight.Bold)\n            Spacer(Modifier.height(8.dp))\n', '', 1)
s = s.replace('.height(180.dp).scale(scale)', '.height(205.dp).scale(scale)', 1)
games.write_text(s, encoding='utf-8')

store = Path('app/src/main/java/com/learnlettersnumbers/app/RewardStoreScreen.kt')
t = store.read_text(encoding='utf-8')
t = t.replace('import androidx.compose.foundation.lazy.grid.GridCells\nimport androidx.compose.foundation.lazy.grid.LazyVerticalGrid\nimport androidx.compose.foundation.lazy.grid.items\n', 'import androidx.compose.foundation.lazy.LazyColumn\nimport androidx.compose.foundation.lazy.items\n', 1)
start = t.index('@Composable\nfun RewardStoreScreen')
new_store = '''@Composable
fun RewardStoreScreen(repo: ProgressRepository, onBack: () -> Unit, speak: (String) -> Unit) {
    var snapshot by remember { mutableStateOf(repo.load()) }
    val ownedItems = remember { mutableStateListOf<String>().apply { addAll(repo.ownedRewards()) } }
    LaunchedEffect(Unit) { snapshot = repo.load() }
    CompositionLocalProvider(androidx.compose.ui.platform.LocalLayoutDirection provides androidx.compose.ui.unit.LayoutDirection.Rtl) {
        Column(Modifier.fillMaxSize().background(Brush.verticalGradient(listOf(MaterialTheme.colorScheme.background, MaterialTheme.colorScheme.surfaceVariant))).padding(horizontal = 14.dp)) {
            Row(Modifier.fillMaxWidth().padding(vertical = 10.dp), verticalAlignment = Alignment.CenterVertically) {
                Button(onClick = onBack, shape = RoundedCornerShape(18.dp)) { Text("رجوع") }
                Spacer(Modifier.weight(1f))
                Column(horizontalAlignment = Alignment.End) {
                    Text("متجر المكافآت", fontSize = 28.sp, fontWeight = FontWeight.Black, color = Color(0xFF245B8A))
                    Text("⭐ ${snapshot.stars} نجمة", fontSize = 18.sp, fontWeight = FontWeight.Bold)
                }
            }
            LazyColumn(Modifier.fillMaxWidth().weight(1f), contentPadding = PaddingValues(bottom = 24.dp), verticalArrangement = Arrangement.spacedBy(9.dp)) {
                item {
                    Text("🎁 المكافآت", fontSize = 23.sp, fontWeight = FontWeight.Black)
                    Text("اختر مكافأة من القائمة", fontSize = 13.sp, color = Color(0xFF667085))
                }
                items(rewardItems, key = { "reward_${it.id}" }) { item ->
                    val owned = ownedItems.contains(item.id)
                    Card(Modifier.fillMaxWidth().clickable {
                        if (!owned) {
                            if (snapshot.stars >= item.price) {
                                repo.addStars(-item.price)
                                ownedItems.add(item.id)
                                repo.addOwnedReward(item.id)
                                snapshot = repo.load()
                                speak("أحسنت! اشتريت ${item.title}")
                            } else speak("تحتاج إلى نجوم أكثر")
                        }
                    }, shape = RoundedCornerShape(20.dp), colors = CardDefaults.cardColors(containerColor = if (owned) Color(0xFFE4F5E5) else MaterialTheme.colorScheme.surface), elevation = CardDefaults.cardElevation(5.dp)) {
                        Row(Modifier.fillMaxWidth().padding(11.dp), verticalAlignment = Alignment.CenterVertically) {
                            Text(item.emoji, fontSize = 35.sp)
                            Spacer(Modifier.width(12.dp))
                            Column(Modifier.weight(1f)) {
                                Text(item.title, fontWeight = FontWeight.ExtraBold, fontSize = 17.sp)
                                Text(if (owned) "تم الحصول عليها ✓" else "⭐ ${item.price}", color = if (owned) Color(0xFF2E7D32) else Color(0xFF8A5A00), fontSize = 13.sp)
                            }
                        }
                    }
                }
                item {
                    Spacer(Modifier.height(5.dp))
                    Text("🏅 الألقاب التحفيزية", fontSize = 23.sp, fontWeight = FontWeight.Black)
                    Text("جميع الألقاب في نفس القائمة وبالتسلسل", fontSize = 13.sp, color = Color(0xFF667085))
                }
                items(titles, key = { "title_$it" }) { title ->
                    val owned = snapshot.earnedTitles.contains(title)
                    Card(Modifier.fillMaxWidth().clickable {
                        if (!owned) {
                            if (snapshot.stars >= 40) {
                                repo.addStars(-40)
                                repo.addTitle(title)
                                snapshot = repo.load()
                                speak("مبروك! حصلت على لقب $title")
                            } else speak("تحتاج إلى أربعين نجمة")
                        }
                    }, shape = RoundedCornerShape(17.dp), colors = CardDefaults.cardColors(containerColor = if (owned) Color(0xFFFFF0B8) else MaterialTheme.colorScheme.surface), elevation = CardDefaults.cardElevation(4.dp)) {
                        Row(Modifier.fillMaxWidth().padding(10.dp), verticalAlignment = Alignment.CenterVertically) {
                            Text("🏅", fontSize = 25.sp)
                            Spacer(Modifier.width(10.dp))
                            Column(Modifier.weight(1f)) {
                                Text(title, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                                Text(if (owned) "مملوك ✓" else "⭐ 40", fontSize = 13.sp)
                            }
                        }
                    }
                }
            }
        }
    }
}
'''
t = t[:start] + new_store
store.write_text(t, encoding='utf-8')
