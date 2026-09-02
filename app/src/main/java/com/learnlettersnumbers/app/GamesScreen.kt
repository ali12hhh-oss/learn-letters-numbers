package com.learnlettersnumbers.app

import android.app.Activity
import android.content.Context
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.delay
import kotlin.random.Random

private data class LearningGame(val id:String,val title:String,val subtitle:String,val icon:String,val color:Color,val category:String)
private data class GameLevel(val number:Int,val title:String,val subtitle:String,val color:Color)
private data class RoundQuestion(val prompt:String,val options:List<String>,val answer:String,val spoken:String=answer)

private val games=listOf(
    LearningGame("match","صائد الحروف","التقط الحرف المطلوب","🔤",Color(0xFFFFD166),"حروف"),
    LearningGame("number","صيد الأرقام","اعثر على الرقم","🎯",Color(0xFF8ED1FC),"أرقام"),
    LearningGame("memory","ذاكرة الأبطال","احفظ ثم طابق","🧠",Color(0xFFCDB4DB),"ذاكرة"),
    LearningGame("sort","سباق الترتيب","اختر الأصغر","🏁",Color(0xFFA8E6CF),"أرقام"),
    LearningGame("word","الكلمة السحرية","أكمل الكلمة","🪄",Color(0xFFFFAAA5),"قراءة"),
    LearningGame("listen","اسمع واربح","استمع واختر","🔊",Color(0xFFFFD6A5),"استماع"),
    LearningGame("count","مزرعة الأعداد","عد النجوم","🌟",Color(0xFFCAFFBF),"أرقام"),
    LearningGame("build","صانع الكلمات","كوّن الكلمة","🧩",Color(0xFFFFC8DD),"قراءة"),
    LearningGame("quick","التحدي الذهبي","تحديات متنوعة","🏆",Color(0xFFFFE5B4),"متنوع"),
    LearningGame("shapes","شكل الحرف","تعرف على الحرف","✏️",Color(0xFFBDE0FE),"حروف")
)
private val gameLevels=listOf(
    GameLevel(1,"المستوى السهل","ابدأ وتعلّم بهدوء",Color(0xFFB8F2C8)),
    GameLevel(2,"المستوى المتوسط","تحدٍ أكبر وسرعة أعلى",Color(0xFFFFE08A)),
    GameLevel(3,"المستوى الصعب","لأبطال الألعاب فقط",Color(0xFFFFA6A6))
)
private const val GAMES_PREFS="professional_games_progress_v3"
private const val TOTAL_ROUNDS=10
private const val DAILY_KEY="daily_game_last"
private const val DAILY_SCORE_KEY="daily_game_best_score"
private val gameLetters=listOf("أ","ب","ت","ث","ج","ح","خ","د","ذ","ر","ز","س","ش","ص","ض","ط","ظ","ع","غ","ف","ق","ك","ل","م","ن","ه","و","ي")
private fun prefs(context:Context)=context.getSharedPreferences(GAMES_PREFS,Context.MODE_PRIVATE)
private fun bestScoreKey(game:LearningGame,level:Int)="best_score_${game.id}_$level"
private fun bestAccuracyKey(game:LearningGame,level:Int)="best_accuracy_${game.id}_$level"
private fun completedKey(game:LearningGame,level:Int)="completed_${game.id}_$level"
private fun attemptsKey(game:LearningGame,level:Int)="attempts_${game.id}_$level"
private fun isLevelUnlocked(context:Context,game:LearningGame,level:Int)=level==1||prefs(context).getInt(bestAccuracyKey(game,level-1),0)>=70
private fun todaySeed():Int { val c=java.util.Calendar.getInstance(); return c.get(java.util.Calendar.YEAR)*1000+c.get(java.util.Calendar.DAY_OF_YEAR) }
private fun uniqueOptions(answer:Int,max:Int,random:Random):List<Int>{
    val result=linkedSetOf(answer)
    for(candidate in (1..max).filter{it!=answer}.shuffled(random)){ result+=candidate; if(result.size==4)break }
    return result.toList().shuffled(random)
}
private fun uniqueLetters(answer:String,random:Random):List<String>{
    val result=linkedSetOf(answer)
    for(letter in gameLetters.shuffled(random)){if(letter!=answer)result+=letter;if(result.size==4)break}
    return result.toList().shuffled(random)
}
private fun questionFor(game:LearningGame,index:Int,level:Int,seed:Int):RoundQuestion{
    val random=Random(seed+index*7919+level*104729)
    return when(game.id){
        "match"->{val a=gameLetters[Math.floorMod(index*(2+level)+seed,gameLetters.size)];RoundQuestion("التقط الحرف المطلوب",uniqueLetters(a,random),a,a)}
        "number"->{val max=when(level){1->20;2->50;else->100};val a=random.nextInt(1,max+1);RoundQuestion("اعثر على الرقم المطلوب",uniqueOptions(a,max,random).map(Int::toString),a.toString(),a.toString())}
        "sort"->{val max=6+level*3;val options=uniqueOptions(random.nextInt(1,max+1),max,random);val a=options.minOrNull()?:1;RoundQuestion("ما الرقم الأصغر؟",options.map(Int::toString),a.toString())}
        "word"->{val data=listOf("ب_ب" to "ا","ك_ب" to "ت","ق_م" to "ل","م_رس" to "د","س_ك" to "م","ك_اب" to "ت","ج_ل" to "م","ن_ر" to "ه");val(pattern,a)=data[Math.floorMod(index+level+seed,data.size)];val wrong=listOf("ب","ن","س","د","ل","م").filter{it!=a}.shuffled(random).take(3);RoundQuestion("أكمل الكلمة: $pattern",(listOf(a)+wrong).shuffled(random),a,a)}
        "listen"->{val a=gameLetters[Math.floorMod(index+seed+level,gameLetters.size)];RoundQuestion("استمع إلى صوت الحرف ثم اختره",uniqueLetters(a,random),a,a)}
        "count"->{val count=index%(7+level*2)+2;RoundQuestion("كم نجمة؟",uniqueOptions(count,12,random).map(Int::toString),count.toString(),count.toString())}
        "build"->{val data=listOf("ب + ا" to "با","م + ا" to "ما","د + ا" to "دا","ل + ا" to "لا","س + ا" to "سا","ك + ا" to "كا","ر + ا" to "را","ن + ا" to "نا");val(prompt,a)=data[Math.floorMod(index+seed+level,data.size)];val wrong=listOf("بو","مي","دو","سو","كي").filter{it!=a}.shuffled(random).take(3);RoundQuestion("كوّن: $prompt",(listOf(a)+wrong).shuffled(random),a,a)}
        "memory"->{val a=gameLetters[Math.floorMod(index*(2+level)+seed,gameLetters.size)];RoundQuestion("احفظ الحرف ثم طابقه",uniqueLetters(a,random),a,a)}
        "shapes"->{val a=gameLetters[Math.floorMod(index+level+seed,gameLetters.size)];RoundQuestion("ما الحرف الظاهر؟",uniqueLetters(a,random),a,a)}
        else->{val max=when(level){1->10;2->30;else->60};val a=random.nextInt(1,max+1);val b=random.nextInt(1,level+3);val answer=a+b;val result=linkedSetOf(answer,a,answer+1,(answer-1).coerceAtLeast(0));for(v in (0..max+level+3).shuffled(random)){result+=v;if(result.size==4)break};RoundQuestion("$a + $b = ؟",result.toList().shuffled(random).map(Int::toString),answer.toString(),answer.toString())}
    }
}

@Composable fun GamesScreen(onBack:()->Unit,repo:ProgressRepository,onSpeak:((String,String)->Unit)?=null){
    var selected by remember{mutableStateOf<LearningGame?>(null)};var level by remember{mutableStateOf<Int?>(null)}
    when{selected==null->GameHubScreen(onBack){selected=it};level==null->LevelSelectionScreen(selected!!,{selected=null},{level=it},onSpeak);else->ProfessionalGameScreen(selected!!,level!!,{level=null},repo,onSpeak)}
}
@Composable private fun GameHubScreen(onBack:()->Unit,onGameSelected:(LearningGame)->Unit){
    val context=LocalContext.current;var category by remember{mutableStateOf("الكل")};val categories=listOf("الكل","حروف","أرقام","قراءة","ذاكرة","استماع","متنوع");val visible=if(category=="الكل")games else games.filter{it.category==category}
    Scaffold(topBar={TopAppBar(title={Text("🎮 عالم الألعاب",fontWeight=FontWeight.ExtraBold)},navigationIcon={IconButton(onClick=onBack){Icon(Icons.Default.ArrowBack,"رجوع")}})}){p->Column(Modifier.fillMaxSize().padding(p).padding(horizontal=14.dp)){LazyRow(Modifier.fillMaxWidth(),horizontalArrangement=Arrangement.spacedBy(6.dp),contentPadding=PaddingValues(vertical=8.dp)){items(categories){c->FilterChip(selected=category==c,onClick={category=c},label={Text(c)})}};Text("الألعاب",fontSize=20.sp,fontWeight=FontWeight.Bold);Spacer(Modifier.height(8.dp));LazyVerticalGrid(Modifier.weight(1f),columns=GridCells.Fixed(2),contentPadding=PaddingValues(bottom=20.dp),horizontalArrangement=Arrangement.spacedBy(12.dp),verticalArrangement=Arrangement.spacedBy(12.dp)){items(visible,key={it.id}){game->GameCard(game,context){onGameSelected(game)}}}}}}
}
@Composable private fun GameCard(game:LearningGame,context:Context,onClick:()->Unit){val best=gameLevels.maxOfOrNull{prefs(context).getInt(bestScoreKey(game,it.number),0)}?:0;Card(Modifier.fillMaxWidth().height(190.dp).clickable(onClick=onClick),shape=RoundedCornerShape(24.dp),colors=CardDefaults.cardColors(containerColor=game.color),elevation=CardDefaults.cardElevation(7.dp)){Column(Modifier.fillMaxSize().padding(12.dp),horizontalAlignment=Alignment.CenterHorizontally,verticalArrangement=Arrangement.Center){Text(game.icon,fontSize=42.sp);Text(game.title,fontSize=19.sp,fontWeight=FontWeight.ExtraBold,textAlign=TextAlign.Center);Text(game.subtitle,fontSize=12.sp,textAlign=TextAlign.Center);Text(game.category,fontSize=11.sp);if(best>0)Text("⭐ أفضل نتيجة $best",fontSize=11.sp,fontWeight=FontWeight.Bold)}}}
@Composable private fun LevelSelectionScreen(game:LearningGame,onBack:()->Unit,onLevelSelected:(Int)->Unit,onSpeak:((String,String)->Unit)?){val context=LocalContext.current;Scaffold(topBar={TopAppBar(title={Text("${game.icon} ${game.title}",fontWeight=FontWeight.ExtraBold)},navigationIcon={IconButton(onClick=onBack){Icon(Icons.Default.ArrowBack,"رجوع")}})}){p->Column(Modifier.fillMaxSize().padding(p).padding(18.dp),horizontalAlignment=Alignment.CenterHorizontally){Text("اختر مستوى التحدي",fontSize=27.sp,fontWeight=FontWeight.ExtraBold);Text("يفتح المستوى التالي بعد تحقيق دقة 70% أو أكثر",textAlign=TextAlign.Center);Spacer(Modifier.height(14.dp));gameLevels.forEach{l->val unlocked=isLevelUnlocked(context,game,l.number);Card(Modifier.fillMaxWidth().padding(vertical=6.dp).clickable(enabled=unlocked){onSpeak?.invoke("${l.title}. هيا نبدأ!","ar");onLevelSelected(l.number)},shape=RoundedCornerShape(22.dp),colors=CardDefaults.cardColors(containerColor=if(unlocked)l.color else MaterialTheme.colorScheme.surfaceVariant)){Row(Modifier.fillMaxWidth().padding(16.dp),verticalAlignment=Alignment.CenterVertically){Text(if(unlocked)"🎮" else "🔒",fontSize=32.sp);Spacer(Modifier.width(12.dp));Column(Modifier.weight(1f)){Text(l.title,fontSize=19.sp,fontWeight=FontWeight.ExtraBold);Text(if(unlocked)l.subtitle else "أكمل المستوى السابق بدقة 70%")}Text(if(unlocked)"ابدأ ➜" else "مغلق",fontWeight=FontWeight.Bold)}}}}}}

@Composable private fun ProfessionalGameScreen(game:LearningGame,level:Int,onBack:()->Unit,repo:ProgressRepository,onSpeak:((String,String)->Unit)?){
    var round by remember{mutableStateOf(0)};var score by remember{mutableStateOf(0)};var correct by remember{mutableStateOf(0)};var streak by remember{mutableStateOf(0)};var bestStreak by remember{mutableStateOf(0)};var lives by remember{mutableStateOf(3)};var answered by remember{mutableStateOf(false)};var selected by remember{mutableStateOf<String?>(null)};var finished by remember{mutableStateOf(false)};var timedOut by remember{mutableStateOf(false)};var gained by remember{mutableStateOf(0)};var paused by remember{mutableStateOf(false)};var quit by remember{mutableStateOf(false)};var completed by remember{mutableStateOf(false)};var memoryReady by remember{mutableStateOf(game.id!="memory")}
    val context=LocalContext.current;val activity=context as? Activity;val adManager=remember(context){AdMobManager(context.applicationContext)};val adFrequency=remember(context){AdFrequencyController(context.applicationContext)};val seed=remember(game.id,level){Random.nextInt()};val question=remember(game.id,level,round,seed){questionFor(game,round,level,seed)};val limit=when(level){1->18;2->14;else->11};var timeLeft by remember(level,round){mutableStateOf(limit)}
    LaunchedEffect(game.id,level,round,answered,paused,finished){memoryReady=game.id!="memory";if(game.id=="memory"&&!answered&&!paused&&!finished){delay(if(level==1)1800 else if(level==2)1500 else 1200);if(!answered&&!paused&&!finished)memoryReady=true}}
    LaunchedEffect(game.id,level,round,answered,paused,finished){if(!answered&&!paused&&!finished){timeLeft=limit;while(timeLeft>0&&!answered&&!paused&&!finished){delay(1000);timeLeft--};if(timeLeft==0&&!answered&&!paused&&!finished){timedOut=true;answered=true;lives--;streak=0;repo.recordAnswer(false);onSpeak?.invoke("انتهى الوقت، حاول مرة أخرى","ar")}}}
    if(finished){val accuracy=(correct*100f/TOTAL_ROUNDS).toInt();val bonus=if(completed&&correct>0)10+level*5 else 0;val finalScore=score+bonus;val p=prefs(context);val oldBest=p.getInt(bestScoreKey(game,level),0);val oldAcc=p.getInt(bestAccuracyKey(game,level),0);LaunchedEffect(game.id,level,finalScore,accuracy){p.edit().putInt(bestScoreKey(game,level),maxOf(oldBest,finalScore)).putInt(bestAccuracyKey(game,level),maxOf(oldAcc,accuracy)).putInt(attemptsKey(game,level),p.getInt(attemptsKey(game,level),0)+1).putBoolean(completedKey(game,level),completed&&accuracy>=70).apply()};GameResultScreen(game,level,finalScore,correct,accuracy,bestStreak,bonus,maxOf(oldBest,finalScore),completed,{if(completed&&activity!=null)adFrequency.showAfterCompletion(activity,adManager,AdFrequencyController.CompletionType.GAME);onBack()},{round=0;score=0;correct=0;streak=0;bestStreak=0;lives=3;answered=false;selected=null;finished=false;timedOut=false;gained=0;paused=false;completed=false});return}
    if(quit)AlertDialog(onDismissRequest={quit=false},title={Text("الخروج من اللعبة؟",fontWeight=FontWeight.ExtraBold)},text={Text("سيتم إنهاء الجولة الحالية.")},confirmButton={TextButton(onClick=onBack){Text("خروج")}},dismissButton={TextButton(onClick={quit=false}){Text("متابعة")}})
    if(paused)AlertDialog(onDismissRequest={paused=false},title={Text("⏸ اللعبة متوقفة")},text={Text("يمكنك المتابعة عندما تكون مستعداً.")},confirmButton={Button(onClick={paused=false}){Text("متابعة ▶")}},dismissButton={TextButton(onClick={paused=false;quit=true}){Text("إنهاء")}})
    Scaffold(topBar={TopAppBar(title={Text("${game.icon} ${game.title} • مستوى $level",fontWeight=FontWeight.Bold)},navigationIcon={IconButton(onClick={quit=true}){Icon(Icons.Default.ArrowBack,"رجوع")}},actions={IconButton(onClick={paused=true}){Text("⏸",fontSize=22.sp)}})}){p->Column(Modifier.fillMaxSize().padding(p).padding(horizontal=16.dp),horizontalAlignment=Alignment.CenterHorizontally){Row(Modifier.fillMaxWidth(),verticalAlignment=Alignment.CenterVertically){Text("${round+1}/$TOTAL_ROUNDS",fontWeight=FontWeight.Bold);Spacer(Modifier.width(8.dp));LinearProgressIndicator(progress={(round+if(answered)1 else 0).toFloat()/TOTAL_ROUNDS},Modifier.weight(1f).height(8.dp));Spacer(Modifier.width(8.dp));Text("⭐ $score",fontWeight=FontWeight.Bold)};Row(Modifier.fillMaxWidth().padding(vertical=8.dp),horizontalArrangement=Arrangement.SpaceBetween,verticalAlignment=Alignment.CenterVertically){Text(if(streak>=2)"🔥 سلسلة ×$streak" else "ابدأ سلسلة!",fontWeight=FontWeight.Bold);Text("⏱ $timeLeft",fontSize=18.sp,fontWeight=FontWeight.ExtraBold);Row{repeat(3){i->Icon(Icons.Default.Favorite,null,tint=if(i<lives)MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.surfaceVariant,modifier=Modifier.size(21.dp))}}};if(game.id=="count")CountChallenge(round,level) else GameQuestionPanel(game,question,onSpeak,memoryReady);Spacer(Modifier.height(12.dp));question.options.forEach{option->AnswerButton(option,!answered&&memoryReady&&!paused,selected==option,answered&&option==question.answer,answered&&selected==option&&option!=question.answer){selected=option;answered=true;val isCorrect=option==question.answer;repo.recordAnswer(isCorrect);if(isCorrect){correct++;val speed=if(timeLeft>=limit*.65f)2 else if(timeLeft>=limit*.35f)1 else 0;val combo=streak.coerceAtMost(4);gained=1+combo+speed+(level-1);score+=gained;streak++;bestStreak=maxOf(bestStreak,streak);repo.addStars(gained);onSpeak?.invoke("أحسنت! إجابة صحيحة","ar")}else{gained=0;lives--;streak=0;onSpeak?.invoke("حاول مرة أخرى، أنت تستطيع","ar")}};Spacer(Modifier.height(6.dp))};if(answered){Text(if(timedOut)"⏰ انتهى الوقت! الإجابة: ${question.answer}" else if(selected==question.answer)"رائع! +$gained ⭐" else "الإجابة الصحيحة: ${question.answer}",fontSize=17.sp,fontWeight=FontWeight.ExtraBold,textAlign=TextAlign.Center);Spacer(Modifier.height(6.dp));Button(onClick={timedOut=false;if(round+1>=TOTAL_ROUNDS){completed=true;finished=true}else if(lives<=0){completed=false;finished=true}else{round++;answered=false;selected=null;gained=0}}){Text(if(round+1>=TOTAL_ROUNDS||lives<=0)"عرض النتيجة 🏆" else "السؤال التالي ➜")}}}}
}
@Composable private fun GameQuestionPanel(game:LearningGame,question:RoundQuestion,onSpeak:((String,String)->Unit)?,memoryReady:Boolean){Card(Modifier.fillMaxWidth().height(180.dp),shape=RoundedCornerShape(28.dp),colors=CardDefaults.cardColors(containerColor=game.color.copy(alpha=.78f))){Column(Modifier.fillMaxSize().padding(16.dp),horizontalAlignment=Alignment.CenterHorizontally,verticalArrangement=Arrangement.Center){if(game.id=="memory"&&!memoryReady){Text("👀",fontSize=46.sp);Text(question.answer,fontSize=48.sp,fontWeight=FontWeight.ExtraBold);Text("احفظه جيدًا...",fontSize=17.sp,fontWeight=FontWeight.Bold)}else if(game.id=="shapes"){Text(question.answer,fontSize=64.sp,fontWeight=FontWeight.ExtraBold);Text(question.prompt,fontSize=21.sp,fontWeight=FontWeight.ExtraBold,textAlign=TextAlign.Center);if(onSpeak!=null)FilledTonalButton(onClick={onSpeak.invoke(question.spoken,"ar")}){Text("🔊 استمع لصوت الحرف")}}else{Text(game.icon,fontSize=40.sp);Text(question.prompt,fontSize=22.sp,fontWeight=FontWeight.ExtraBold,textAlign=TextAlign.Center);if(onSpeak!=null)FilledTonalButton(onClick={onSpeak.invoke(question.spoken,"ar"){Text(if(game.id=="listen")"🔊 استمع" else "🔊 اسمع السؤال")}})}}}}
@Composable private fun CountChallenge(round:Int,level:Int){val count=round%(7+level*2)+2;Card(Modifier.fillMaxWidth().height(180.dp),shape=RoundedCornerShape(28.dp),colors=CardDefaults.cardColors(containerColor=Color(0xFFCAFFBF).copy(alpha=.8f))){Column(Modifier.fillMaxSize(),horizontalAlignment=Alignment.CenterHorizontally,verticalArrangement=Arrangement.Center){Text("عد النجوم",fontSize=22.sp,fontWeight=FontWeight.Bold);Spacer(Modifier.height(5.dp));(0 until count).toList().chunked(7).forEach{row->Row{row.forEach{Text("⭐",fontSize=if(level==3)24.sp else 28.sp)}}};Spacer(Modifier.height(5.dp));Text("كم عددها؟",fontSize=18.sp,fontWeight=FontWeight.Bold)}}}
@Composable private fun AnswerButton(text:String,enabled:Boolean,selected:Boolean,correct:Boolean,wrong:Boolean,onClick:()->Unit){val scale by androidx.compose.animation.core.animateFloatAsState(if(selected).97f else 1f,label="answerScale");val bg=when{correct->Color(0xFF8BE28B);wrong->Color(0xFFFF9A9A);else->MaterialTheme.colorScheme.surface};Surface(Modifier.fillMaxWidth().height(52.dp).scale(scale).clickable(enabled=enabled,onClick=onClick),shape=RoundedCornerShape(18.dp),color=bg,tonalElevation=3.dp,shadowElevation=2.dp){Box(Modifier.fillMaxSize(),contentAlignment=Alignment.Center){Text(text,fontSize=20.sp,fontWeight=FontWeight.Bold)}}}
@Composable private fun GameResultScreen(game:LearningGame,level:Int,score:Int,correctCount:Int,accuracy:Int,bestStreak:Int,bonus:Int,bestScore:Int,completed:Boolean,onBack:()->Unit,onReplay:()->Unit){val rank=when{accuracy>=90->"أسطورة 🏆";accuracy>=70->"بطل ⭐";accuracy>=50->"ممتاز 👏";else->"واصل التدريب 💪"};Column(Modifier.fillMaxSize().padding(24.dp),horizontalAlignment=Alignment.CenterHorizontally,verticalArrangement=Arrangement.Center){Text(if(completed)"🏆" else "💪",fontSize=76.sp);Text(if(completed)"انتهت اللعبة!" else "انتهت المحاولة",fontSize=30.sp,fontWeight=FontWeight.ExtraBold);Text("${game.title} • المستوى $level",fontSize=20.sp,textAlign=TextAlign.Center);Text(rank,fontSize=22.sp,fontWeight=FontWeight.ExtraBold);Text("النتيجة: ⭐ $score",fontSize=25.sp,fontWeight=FontWeight.Bold);Text("أفضل نتيجة: ⭐ $bestScore",fontSize=17.sp);Text("الدقة: $accuracy%",fontSize=18.sp);Text("الإجابات الصحيحة: $correctCount/$TOTAL_ROUNDS",fontSize=16.sp);Text("أفضل سلسلة: 🔥 $bestStreak",fontSize=18.sp);if(bonus>0)Text("مكافأة الإنهاء: +$bonus ⭐",fontSize=16.sp,fontWeight=FontWeight.Bold);Spacer(Modifier.height(18.dp));Button(onClick=onReplay,Modifier.fillMaxWidth()){Text("العب مرة أخرى")};OutlinedButton(onClick=onBack,Modifier.fillMaxWidth()){Text("العودة للمستويات")}}}
