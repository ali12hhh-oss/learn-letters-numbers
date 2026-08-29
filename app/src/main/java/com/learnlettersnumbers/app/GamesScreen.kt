@file:OptIn(androidx.compose.material3.ExperimentalMaterial3Api::class)

package com.learnlettersnumbers.app

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.delay

private data class LearningGame(val id: String, val title: String, val subtitle: String, val icon: String, val color: Color, val category: String)
private val games = listOf(
    LearningGame("match", "صائد الحروف", "التقط الحرف المطلوب", "🔤", Color(0xFFFFD166), "حروف"),
    LearningGame("number", "صيد الأرقام", "اعثر على الرقم", "🎯", Color(0xFF8ED1FC), "أرقام"),
    LearningGame("memory", "ذاكرة الأبطال", "احفظ ثم طابق", "🧠", Color(0xFFCDB4DB), "ذاكرة"),
    LearningGame("sort", "سباق الترتيب", "اختر الأصغر", "🏁", Color(0xFFA8E6CF), "أرقام"),
    LearningGame("word", "الكلمة السحرية", "أكمل الكلمة", "🪄", Color(0xFFFFAAA5), "قراءة"),
    LearningGame("listen", "اسمع واربح", "استمع واختر", "🔊", Color(0xFFFFD6A5), "استماع"),
    LearningGame("count", "مزرعة الأعداد", "عد النجوم", "🌟", Color(0xFFCAFFBF), "أرقام"),
    LearningGame("build", "صانع الكلمات", "كوّن الكلمة", "🧩", Color(0xFFFFC8DD), "قراءة"),
    LearningGame("quick", "التحدي الذهبي", "تحديات متنوعة", "🏆", Color(0xFFFFE5B4), "متنوع"),
    LearningGame("shapes", "شكل الحرف", "تعرف على الحرف", "✏️", Color(0xFFBDE0FE), "حروف")
)

@Composable
fun GamesScreen(onBack: () -> Unit, repo: ProgressRepository, onSpeak: ((String, String) -> Unit)? = null) {
    var selected by remember { mutableStateOf<LearningGame?>(null) }
    if (selected == null) GameHubScreen(onBack) { selected = it }
    else ProfessionalGameScreen(selected!!, { selected = null }, repo, onSpeak)
}

@Composable
private fun GameHubScreen(onBack: () -> Unit, onGameSelected: (LearningGame) -> Unit) {
    Scaffold(topBar = {
        TopAppBar(title = { Text("🎮 عالم الألعاب", fontWeight = FontWeight.ExtraBold) }, navigationIcon = {
            IconButton(onClick = onBack) { Icon(Icons.Default.ArrowBack, "رجوع") }
        })
    }) { padding ->
        Column(Modifier.fillMaxSize().padding(padding).padding(horizontal = 14.dp)) {
            Card(Modifier.fillMaxWidth().padding(top = 8.dp), shape = RoundedCornerShape(28.dp), colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer)) {
                Column(Modifier.padding(20.dp)) {
                    Text("مستعد للتحدي؟ 🚀", fontSize = 28.sp, fontWeight = FontWeight.ExtraBold)
                    Spacer(Modifier.height(5.dp))
                    Text("اختر لعبتك، اجمع النجوم، وابنِ سلسلة انتصاراتك!", fontSize = 15.sp)
                }
            }
            Spacer(Modifier.height(14.dp))
            Text("الألعاب", fontSize = 22.sp, fontWeight = FontWeight.Bold)
            Spacer(Modifier.height(8.dp))
            LazyVerticalGrid(columns = GridCells.Fixed(2), contentPadding = PaddingValues(bottom = 20.dp), horizontalArrangement = Arrangement.spacedBy(12.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                items(games, key = { it.id }) { game -> GameCard(game) { onGameSelected(game) } }
            }
        }
    }
}

@Composable
private fun GameCard(game: LearningGame, onClick: () -> Unit) {
    var pressed by remember { mutableStateOf(false) }
    val scale by animateFloatAsState(if (pressed) .94f else 1f, tween(110), label = "gameCardScale")
    Card(Modifier.fillMaxWidth().height(174.dp).scale(scale).clickable { pressed = true; onClick(); pressed = false }, shape = RoundedCornerShape(26.dp), colors = CardDefaults.cardColors(containerColor = game.color), elevation = CardDefaults.cardElevation(7.dp)) {
        Column(Modifier.fillMaxSize().padding(13.dp), horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.Center) {
            Text(game.icon, fontSize = 45.sp)
            Spacer(Modifier.height(4.dp))
            Text(game.title, fontSize = 20.sp, fontWeight = FontWeight.ExtraBold)
            Text(game.subtitle, fontSize = 12.sp)
            Spacer(Modifier.height(6.dp))
            Surface(shape = RoundedCornerShape(50), color = Color.White.copy(alpha = .45f)) {
                Text(game.category, Modifier.padding(horizontal = 10.dp, vertical = 3.dp), fontSize = 11.sp)
            }
        }
    }
}

private data class RoundQuestion(val prompt: String, val options: List<String>, val answer: String, val spoken: String = prompt)
private fun questionFor(game: LearningGame, index: Int): RoundQuestion {
    val letters = listOf("أ","ب","ت","ث","ج","ح","خ","د","ذ","ر","ز","س","ش","ص","ض","ط","ظ","ع","غ","ف","ق","ك","ل","م","ن","ه","و","ي")
    val level = index / 3
    return when (game.id) {
        "match" -> { val a=letters[index%letters.size]; RoundQuestion("التقط الحرف: $a", listOf(a,letters[(index+3+level)%letters.size],letters[(index+7+level)%letters.size],letters[(index+11+level)%letters.size]).distinct().shuffled(),a) }
        "number" -> { val max=(9+level*5).coerceAtMost(99); val a=((index*7)%max+1); RoundQuestion("اعثر على الرقم $a",listOf(a,a+2,a+4,(a+7).coerceAtMost(99)).distinct().map(Int::toString).shuffled(),a.toString()) }
        "sort" -> { val base=index%(5+level)+1; RoundQuestion("ما الرقم الأصغر؟",listOf(base+3+level,base+1,base+5+level,base).map(Int::toString).shuffled(),base.toString()) }
        "word" -> { val d=listOf("ب_ب" to "ا","ك_ب" to "ت","ق_م" to "ل","م_رس" to "د","س_مك" to "م","ك_تاب" to "ت"); val(p,a)=d[index%d.size]; RoundQuestion("أكمل الكلمة: $p",listOf(a,"ب","ن","س").shuffled(),a) }
        "listen" -> { val d=listOf("أ" to "ألف","ب" to "باء","ت" to "تاء","ج" to "جيم","م" to "ميم","س" to "سين"); val(a,n)=d[index%d.size]; RoundQuestion("استمع ثم اختر الحرف",listOf(a,"د","ف","ك").distinct().shuffled(),a,n) }
        "count" -> { val a=index%(7+level*2)+2; RoundQuestion("كم نجمة؟",listOf(a,a+1,a-1,a+2).map(Int::toString).distinct().shuffled(),a.toString()) }
        "build" -> { val d=listOf("ب + ا" to "با","م + ا" to "ما","د + ا" to "دا","ل + ا" to "لا","س + ا" to "سا","ك + ا" to "كا"); val(p,a)=d[index%d.size]; RoundQuestion("كوّن: $p",listOf(a,"بو","مي","دو").shuffled(),a) }
        "memory" -> { val a=letters[(index*2+level)%letters.size]; RoundQuestion("احفظ الحرف ثم طابقه",listOf(a,letters[(index+4)%letters.size],letters[(index+9)%letters.size],letters[(index+13)%letters.size]).distinct().shuffled(),a,a) }
        "shapes" -> { val a=letters[(index+1+level)%letters.size]; RoundQuestion("أي حرف تراه؟",listOf(a,letters[(index+3+level)%letters.size],letters[(index+6+level)%letters.size],letters[(index+8+level)%letters.size]).distinct().shuffled(),a) }
        else -> { val a=index%(8+level*2)+1; RoundQuestion("$a + ${level+1} = ؟",listOf(a+level+1,a,a+level+2,9+level).map(Int::toString).distinct().shuffled(),(a+level+1).toString()) }
    }
}

@Composable
private fun ProfessionalGameScreen(game:LearningGame,onBack:()->Unit,repo:ProgressRepository,onSpeak:((String,String)->Unit)?) {
    var round by remember{mutableStateOf(0)}; var score by remember{mutableStateOf(0)}; var correctCount by remember{mutableStateOf(0)}; var streak by remember{mutableStateOf(0)}; var bestStreak by remember{mutableStateOf(0)}; var lives by remember{mutableStateOf(3)}; var timeLeft by remember{mutableStateOf(15)}; var answered by remember{mutableStateOf(false)}; var selected by remember{mutableStateOf<String?>(null)}; var finished by remember{mutableStateOf(false)}; var timedOut by remember{mutableStateOf(false)}; var lastGained by remember{mutableStateOf(0)}; var memoryReady by remember{mutableStateOf(game.id!="memory")}
    val total=10
    val question=remember(game.id,round){questionFor(game,round)}
    val progress=((round+if(answered)1 else 0).toFloat()/total.toFloat()).coerceIn(0f,1f)
    LaunchedEffect(round,answered,finished,game.id){memoryReady=game.id!="memory";if(game.id=="memory"&&!answered&&!finished){delay(1600);memoryReady=true}}
    LaunchedEffect(round,answered,finished){if(!answered&&!finished){timeLeft=(15-(round/3)).coerceAtLeast(8);while(timeLeft>0&&!answered&&!finished){delay(1000);timeLeft--};if(timeLeft==0&&!answered&&!finished){timedOut=true;answered=true;selected=null;lastGained=0;lives--;streak=0;repo.recordAnswer(false);onSpeak?.invoke("انتهى الوقت، حاول مرة أخرى","ar")}}}
    if(finished){GameResultScreen(game,score,correctCount,bestStreak,onBack){round=0;score=0;correctCount=0;streak=0;bestStreak=0;lives=3;timeLeft=15;answered=false;selected=null;timedOut=false;lastGained=0;finished=false};return}
    Scaffold(topBar={TopAppBar(title={Text(game.title,fontWeight=FontWeight.Bold)},navigationIcon={IconButton(onClick=onBack){Icon(Icons.Default.ArrowBack,"رجوع")}})}){padding->Column(Modifier.fillMaxSize().padding(padding).padding(horizontal=16.dp),horizontalAlignment=Alignment.CenterHorizontally){Row(Modifier.fillMaxWidth(),verticalAlignment=Alignment.CenterVertically){Text("${round+1}/$total",fontWeight=FontWeight.Bold);Spacer(Modifier.width(8.dp));LinearProgressIndicator(progress={progress},Modifier.weight(1f).height(9.dp));Spacer(Modifier.width(8.dp));Text("⭐ $score",fontWeight=FontWeight.Bold)};Spacer(Modifier.height(7.dp));Row(Modifier.fillMaxWidth(),horizontalArrangement=Arrangement.SpaceBetween,verticalAlignment=Alignment.CenterVertically){Text(if(streak>=2)"🔥 سلسلة ×$streak" else "ابدأ سلسلة!",fontWeight=FontWeight.Bold);Text("⏱ $timeLeft",fontSize=18.sp,fontWeight=FontWeight.ExtraBold);Row{repeat(3){i->Icon(Icons.Default.Favorite,null,tint=if(i<lives)MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.surfaceVariant,modifier=Modifier.size(22.dp))}}};Spacer(Modifier.height(8.dp));if(game.id=="count")CountChallenge(round)else GameQuestionPanel(game,question,onSpeak,memoryReady);Spacer(Modifier.height(14.dp));question.options.forEach{option->AnswerButton(option,!answered&&memoryReady,selected==option,answered&&option==question.answer,answered&&selected==option&&option!=question.answer){selected=option;answered=true;val correct=option==question.answer;repo.recordAnswer(correct);if(correct){correctCount++;val speedBonus=if(timeLeft>=10)2 else if(timeLeft>=5)1 else 0;val comboBonus=streak.coerceAtMost(4);lastGained=1+comboBonus+speedBonus;score+=lastGained;streak++;bestStreak=maxOf(bestStreak,streak);repo.addStars(lastGained);onSpeak?.invoke("أحسنت! إجابة صحيحة","ar")}else{lastGained=0;lives--;streak=0;onSpeak?.invoke("حاول مرة أخرى، أنت تستطيع","ar")}};Spacer(Modifier.height(7.dp))};AnimatedVisibility(answered,enter=fadeIn()+scaleIn(),exit=fadeOut()+scaleOut()){Column(horizontalAlignment=Alignment.CenterHorizontally){Text(if(timedOut)"⏰ انتهى الوقت! الإجابة: ${question.answer}"else if(selected==question.answer)"رائع! +$lastGained ⭐"else"الإجابة الصحيحة: ${question.answer}",fontSize=17.sp,fontWeight=FontWeight.ExtraBold);Spacer(Modifier.height(7.dp));Button(onClick={timedOut=false;if(round+1>=total||lives<=0)finished=true else{round++;answered=false;selected=null;lastGained=0}}){Text(if(round+1>=total||lives<=0)"عرض النتيجة 🏆"else"السؤال التالي ➜")}}}}}
}

@Composable private fun GameQuestionPanel(game:LearningGame,question:RoundQuestion,onSpeak:((String,String)->Unit)?,memoryReady:Boolean){Card(Modifier.fillMaxWidth().height(180.dp),shape=RoundedCornerShape(30.dp),colors=CardDefaults.cardColors(containerColor=game.color.copy(alpha=.78f))){Column(Modifier.fillMaxSize().padding(18.dp),horizontalAlignment=Alignment.CenterHorizontally,verticalArrangement=Arrangement.Center){if(game.id=="memory"&&!memoryReady){Text("👀",fontSize=48.sp);Text(question.answer,fontSize=48.sp,fontWeight=FontWeight.ExtraBold);Text("احفظه جيدًا...",fontSize=17.sp,fontWeight=FontWeight.Bold)}else{Text(game.icon,fontSize=42.sp);Text(question.prompt,fontSize=23.sp,fontWeight=FontWeight.ExtraBold);if(game.id=="listen"){Spacer(Modifier.height(5.dp));FilledTonalButton(onClick={onSpeak?.invoke(question.spoken,"ar")}){Text("🔊 استمع")}}}}}}
@Composable private fun CountChallenge(round:Int){val count=round%7+2;Card(Modifier.fillMaxWidth().height(180.dp),shape=RoundedCornerShape(30.dp),colors=CardDefaults.cardColors(containerColor=Color(0xFFCAFFBF).copy(alpha=.8f))){Column(Modifier.fillMaxSize(),horizontalAlignment=Alignment.CenterHorizontally,verticalArrangement=Arrangement.Center){Text("عد النجوم",fontSize=22.sp,fontWeight=FontWeight.Bold);Spacer(Modifier.height(8.dp));Row{repeat(count){Text("⭐",fontSize=28.sp)}};Spacer(Modifier.height(6.dp));Text("كم عددها؟",fontSize=18.sp,fontWeight=FontWeight.Bold)}}}
@Composable private fun AnswerButton(text:String,enabled:Boolean,selected:Boolean,correct:Boolean,wrong:Boolean,onClick:()->Unit){val scale by animateFloatAsState(if(selected).97f else 1f,tween(120),label="answerScale");val bg=when{correct->Color(0xFF8BE28B);wrong->Color(0xFFFF9A9A);else->MaterialTheme.colorScheme.surface};Surface(Modifier.fillMaxWidth().height(52.dp).scale(scale).clickable(enabled=enabled,onClick=onClick),shape=RoundedCornerShape(18.dp),color=bg,tonalElevation=3.dp,shadowElevation=2.dp){Box(Modifier.fillMaxSize(),contentAlignment=Alignment.Center){Text(text,fontSize=20.sp,fontWeight=FontWeight.Bold)}}}
@Composable private fun GameResultScreen(game:LearningGame,score:Int,correctCount:Int,bestStreak:Int,onBack:()->Unit,onReplay:()->Unit){val accuracy=correctCount*10;val rank=when{accuracy>=90->"أسطورة 🏆";accuracy>=70->"بطل ⭐";accuracy>=50->"ممتاز 👏";else->"واصل التدريب 💪"};Column(Modifier.fillMaxSize().padding(24.dp),horizontalAlignment=Alignment.CenterHorizontally,verticalArrangement=Arrangement.Center){Text("🏆",fontSize=80.sp);Text("انتهت اللعبة!",fontSize=30.sp,fontWeight=FontWeight.ExtraBold);Text(game.title,fontSize=21.sp);Spacer(Modifier.height(8.dp));Text(rank,fontSize=22.sp,fontWeight=FontWeight.ExtraBold);Text("النتيجة: ⭐ $score",fontSize=25.sp,fontWeight=FontWeight.Bold);Text("الدقة: $accuracy%",fontSize=18.sp);Text("أفضل سلسلة: 🔥 $bestStreak",fontSize=18.sp);Spacer(Modifier.height(20.dp));Button(onClick=onReplay,modifier=Modifier.fillMaxWidth()){Text("العب مرة أخرى")};OutlinedButton(onClick=onBack,modifier=Modifier.fillMaxWidth()){Text("العودة للألعاب")}}}
