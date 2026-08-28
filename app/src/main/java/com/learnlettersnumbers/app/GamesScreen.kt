@file:OptIn(androidx.compose.material3.ExperimentalMaterial3Api::class)

package com.learnlettersnumbers.app

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlin.math.cos
import kotlin.math.sin
import kotlin.random.Random

data class LearningGame(val id: String, val title: String, val subtitle: String, val accent: Color, val kind: GameKind)
enum class GameKind { LETTER, NUMBER, MEMORY, ORDER, WORD, LISTEN, SHAPE, COUNT, BUILD, MIXED }

private val games = listOf(
    LearningGame("match", "طابق الحرف", "ابحث عن الحرف الصحيح", Color(0xFF4F8EF7), GameKind.LETTER),
    LearningGame("hunt", "صيد الأرقام", "التقط الرقم المطلوب", Color(0xFFFF8A4C), GameKind.NUMBER),
    LearningGame("memory", "ذاكرة الحروف", "اكشف وطابق الأزواج", Color(0xFF9B7EDE), GameKind.MEMORY),
    LearningGame("order", "رتّب الأرقام", "من الأصغر إلى الأكبر", Color(0xFF35B779), GameKind.ORDER),
    LearningGame("missing", "الكلمة المفقودة", "أكمل الكلمة الناقصة", Color(0xFFE96A8D), GameKind.WORD),
    LearningGame("listen", "اسمع واختر", "استمع ثم اختر", Color(0xFF0FA3B1), GameKind.LISTEN),
    LearningGame("shape", "شكل الحرف", "تعرّف على الشكل", Color(0xFFF2B134), GameKind.SHAPE),
    LearningGame("count", "عدّ الأشياء", "عدّ واختر العدد", Color(0xFF5E8CFF), GameKind.COUNT),
    LearningGame("build", "سباق الكلمات", "كوّن الكلمة", Color(0xFFEF6C3B), GameKind.BUILD),
    LearningGame("quick", "التحدي السريع", "أسئلة متنوعة", Color(0xFF7A5AF8), GameKind.MIXED)
)

private data class GameQuestion(val prompt: String, val options: List<String>, val answer: String, val speech: String = prompt)

@Composable
fun GamesScreen(onBack: () -> Unit, repo: ProgressRepository, onSpeak: ((String, String) -> Unit)? = null) {
    var selected by remember { mutableStateOf<LearningGame?>(null) }
    AnimatedContent(targetState = selected, transitionSpec = { fadeIn(tween(180)) togetherWith fadeOut(tween(120)) }, label = "games_navigation") { game ->
        if (game == null) GameLobby(onBack) { selected = it }
        else GamePlayScreen(game, { selected = null }, repo, onSpeak)
    }
}

@Composable
private fun GameLobby(onBack: () -> Unit, onGameClick: (LearningGame) -> Unit) {
    Scaffold(
        containerColor = Color(0xFFF5F7FF),
        topBar = { TopAppBar(title = { Text("الألعاب التعليمية", fontWeight = FontWeight.ExtraBold) }, navigationIcon = { IconButton(onClick = onBack) { Icon(Icons.Default.ArrowBack, "رجوع") } }, colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.Transparent)) }
    ) { padding ->
        Column(Modifier.fillMaxSize().padding(padding).padding(horizontal = 16.dp), horizontalAlignment = Alignment.CenterHorizontally) {
            GameHero()
            Spacer(Modifier.height(12.dp))
            Text("هيا نلعب ونتعلم! 🌟", fontSize = 27.sp, fontWeight = FontWeight.ExtraBold)
            Text("ألعاب قصيرة، ممتعة، وتعمل بالكامل بدون إنترنت.", fontSize = 15.sp, color = Color(0xFF667085), textAlign = TextAlign.Center)
            Spacer(Modifier.height(18.dp))
            LazyVerticalGrid(columns = GridCells.Fixed(2), modifier = Modifier.fillMaxSize(), horizontalArrangement = Arrangement.spacedBy(12.dp), verticalArrangement = Arrangement.spacedBy(12.dp), contentPadding = PaddingValues(bottom = 20.dp)) {
                items(games, key = { it.id }) { GameCard(it, onGameClick) }
            }
        }
    }
}

@Composable
private fun GameHero() {
    Card(Modifier.fillMaxWidth().height(135.dp), shape = RoundedCornerShape(30.dp), colors = CardDefaults.cardColors(containerColor = Color.Transparent), elevation = CardDefaults.cardElevation(6.dp)) {
        Box(Modifier.fillMaxSize().background(Brush.linearGradient(listOf(Color(0xFF4F8EF7), Color(0xFF7A5AF8))))) {
            Canvas(Modifier.fillMaxSize()) {
                repeat(14) { i -> drawCircle(Color.White.copy(.12f), 5f + i % 4 * 3f, Offset(size.width * ((i * 83 % 100) / 100f), size.height * ((i * 47 % 100) / 100f))) }
            }
            Row(Modifier.fillMaxSize().padding(horizontal = 22.dp), verticalAlignment = Alignment.CenterVertically) {
                Canvas(Modifier.size(92.dp)) { drawGameIllustration(GameKind.MIXED, Color.White) }
                Spacer(Modifier.width(18.dp))
                Column { Text("مدينة الألعاب", color = Color.White, fontSize = 24.sp, fontWeight = FontWeight.ExtraBold); Text("كل إجابة صحيحة تمنحك نجمة ⭐", color = Color.White.copy(.92f), fontSize = 14.sp) }
            }
        }
    }
}

@Composable
private fun GameCard(game: LearningGame, onClick: (LearningGame) -> Unit) {
    var pressed by remember { mutableStateOf(false) }
    val scale by animateFloatAsState(if (pressed) .94f else 1f, tween(100), label = "game_card_scale")
    Card(Modifier.fillMaxWidth().height(184.dp).scale(scale).clickable { pressed = true; onClick(game); pressed = false }, shape = RoundedCornerShape(26.dp), colors = CardDefaults.cardColors(containerColor = Color.White), elevation = CardDefaults.cardElevation(5.dp)) {
        Column(Modifier.fillMaxSize().padding(12.dp), horizontalAlignment = Alignment.CenterHorizontally) {
            Box(Modifier.fillMaxWidth().height(105.dp).background(game.accent.copy(.13f), RoundedCornerShape(20.dp))) { Canvas(Modifier.fillMaxSize().padding(12.dp)) { drawGameIllustration(game.kind, game.accent) } }
            Spacer(Modifier.height(8.dp))
            Text(game.title, fontSize = 19.sp, fontWeight = FontWeight.ExtraBold, textAlign = TextAlign.Center)
            Text(game.subtitle, fontSize = 12.sp, color = Color(0xFF667085), textAlign = TextAlign.Center)
        }
    }
}

private fun DrawScope.drawGameIllustration(kind: GameKind, color: Color) {
    val w = size.width; val h = size.height
    when (kind) {
        GameKind.LETTER, GameKind.SHAPE -> {
            drawRoundRect(color.copy(.16f), Offset(w*.17f,h*.08f), Size(w*.66f,h*.78f), CornerRadius(22f,22f)); drawCircle(color,h*.22f,Offset(w*.5f,h*.48f)); drawCircle(Color.White,h*.13f,Offset(w*.44f,h*.44f)); drawCircle(Color.White,h*.13f,Offset(w*.56f,h*.44f)); drawCircle(color,h*.055f,Offset(w*.44f,h*.44f)); drawCircle(color,h*.055f,Offset(w*.56f,h*.44f)); drawArc(color,20f,140f,false,Offset(w*.40f,h*.46f),Size(w*.20f,h*.20f),style=Stroke(5f))
        }
        GameKind.NUMBER, GameKind.ORDER, GameKind.COUNT -> {
            for (i in 0..2) { val x=w*.28f+i*w*.22f; drawRoundRect(color,Offset(x-22f,h*.20f),Size(44f,52f),CornerRadius(12f,12f)); drawCircle(Color.White,6f,Offset(x,h*.46f)) }; drawLine(color,Offset(w*.20f,h*.78f),Offset(w*.80f,h*.78f),7f,StrokeCap.Round); drawCircle(color,8f,Offset(w*.50f,h*.78f))
        }
        GameKind.MEMORY -> for (r in 0..1) for (col in 0..2) { val x=w*.25f+col*w*.25f; val y=h*.30f+r*h*.30f; drawRoundRect(color,Offset(x-17f,y-22f),Size(34f,44f),CornerRadius(9f,9f)); drawCircle(Color.White.copy(.9f),5f,Offset(x,y)) }
        GameKind.WORD, GameKind.BUILD -> { drawRoundRect(color,Offset(w*.10f,h*.25f),Size(w*.80f,h*.42f),CornerRadius(18f,18f)); for(i in 0..2) drawCircle(Color.White,11f,Offset(w*(.30f+i*.20f),h*.46f)); drawLine(color,Offset(w*.28f,h*.78f),Offset(w*.72f,h*.78f),6f,StrokeCap.Round) }
        GameKind.LISTEN -> { drawCircle(color,9f,Offset(w*.28f,h*.52f)); drawLine(color,Offset(w*.31f,h*.52f),Offset(w*.55f,h*.38f),11f,StrokeCap.Round); drawLine(color,Offset(w*.55f,h*.38f),Offset(w*.55f,h*.66f),11f,StrokeCap.Round); drawArc(color,-55f,110f,false,Offset(w*.54f,h*.27f),Size(w*.30f,h*.50f),style=Stroke(8f)) }
        GameKind.MIXED -> { val cx=w*.5f; val cy=h*.48f; drawCircle(color,22f,Offset(cx,cy)); for(i in 0..7){val a=i*(Math.PI/4).toFloat(); drawLine(Color.White,Offset(cx+sin(a)*32f,cy+cos(a)*32f),Offset(cx+sin(a)*50f,cy+cos(a)*50f),7f,StrokeCap.Round)} }
    }
}

private fun questionFor(game: LearningGame, index: Int): GameQuestion {
    val random = Random(index * 31 + game.id.hashCode())
    return when (game.kind) {
        GameKind.LETTER, GameKind.SHAPE, GameKind.MEMORY -> { val letters=listOf("أ","ب","ت","ث","ج","ح","خ","د","ذ","ر","س","ش"); val answer=letters[index%letters.size]; GameQuestion("أين الحرف $answer ؟",(listOf(answer)+letters.shuffled(random).filter{it!=answer}.take(3)).shuffled(random),answer,"اختر الحرف $answer") }
        GameKind.NUMBER -> { val answer=index%9+1; GameQuestion("اعثر على الرقم $answer",(listOf(answer)+(1..9).shuffled(random).filter{it!=answer}.take(3)).map(Int::toString).shuffled(random),answer.toString(),"اعثر على الرقم $answer") }
        GameKind.ORDER -> { val nums=(1..9).shuffled(random).take(4); GameQuestion("ما الرقم الأصغر؟",nums.map(Int::toString),nums.min().toString()) }
        GameKind.WORD -> { val items=listOf("ب_ب" to "ا","ك_ب" to "ت","ق_م" to "ل","م_رس" to "د","س_م" to "م"); val (word,answer)=items[index%items.size]; GameQuestion("أكمل الكلمة: $word",listOf(answer,"ب","ن","ر").shuffled(random),answer) }
        GameKind.LISTEN -> { val items=listOf("ألف" to "أ","باء" to "ب","تاء" to "ت","جيم" to "ج","حاء" to "ح"); val (name,answer)=items[index%items.size]; GameQuestion("استمع ثم اختر",listOf(answer,"د","خ","س").shuffled(random),answer,name) }
        GameKind.COUNT -> { val count=index%6+2; GameQuestion("كم نجمة تراها؟\n${"★ ".repeat(count)}",(listOf(count)+listOf(2,3,4,5,6,7).shuffled(random).filter{it!=count}.take(3)).map(Int::toString).shuffled(random),count.toString()) }
        GameKind.BUILD -> { val items=listOf("ب + ا =" to "با","م + ا =" to "ما","د + ا =" to "دا","ل + ا =" to "لا","س + ا =" to "سا"); val (prompt,answer)=items[index%items.size]; GameQuestion(prompt,listOf(answer,"بو","مي","دو").shuffled(random),answer) }
        GameKind.MIXED -> when(index%3){0->questionFor(games[0],index);1->questionFor(games[1],index+2);else->questionFor(games[3],index+3)}
    }
}

@Composable
private fun GamePlayScreen(game: LearningGame,onBack:()->Unit,repo:ProgressRepository,onSpeak:((String,String)->Unit)?) {
    var questionIndex by remember { mutableStateOf(0) }; var score by remember { mutableStateOf(0) }; var answered by remember { mutableStateOf(false) }; var selectedAnswer by remember { mutableStateOf<String?>(null) }; var success by remember { mutableStateOf(false) }
    val question=remember(game,questionIndex){questionFor(game,questionIndex)}; val progress=((questionIndex%10)+1)/10f
    LaunchedEffect(game){onSpeak?.invoke("هيا نلعب ${game.title}","ar")}
    Scaffold(containerColor=Color(0xFFF6F8FF),topBar={TopAppBar(title={Text(game.title,fontWeight=FontWeight.ExtraBold)},navigationIcon={IconButton(onClick=onBack){Icon(Icons.Default.ArrowBack,"رجوع")}},colors=TopAppBarDefaults.topAppBarColors(containerColor=Color.Transparent))}){padding->
        Column(Modifier.fillMaxSize().padding(padding).padding(horizontal=18.dp),horizontalAlignment=Alignment.CenterHorizontally){
            Row(Modifier.fillMaxWidth(),verticalAlignment=Alignment.CenterVertically){Box(Modifier.size(54.dp).background(game.accent.copy(.14f),CircleShape),contentAlignment=Alignment.Center){Canvas(Modifier.size(44.dp).padding(4.dp)){drawGameIllustration(game.kind,game.accent)}};Spacer(Modifier.width(10.dp));Column(Modifier.weight(1f)){Text("الجولة ${questionIndex+1}",fontWeight=FontWeight.Bold);LinearProgressIndicator(progress=progress,modifier=Modifier.fillMaxWidth().height(8.dp),color=game.accent,trackColor=game.accent.copy(.12f))};Spacer(Modifier.width(10.dp));Surface(shape=RoundedCornerShape(14.dp),color=Color.White,shadowElevation=2.dp){Text("⭐ $score",Modifier.padding(horizontal=12.dp,vertical=8.dp),fontWeight=FontWeight.ExtraBold)}}
            Spacer(Modifier.height(18.dp)); Card(Modifier.fillMaxWidth().weight(1f,false),shape=RoundedCornerShape(30.dp),colors=CardDefaults.cardColors(containerColor=Color.White),elevation=CardDefaults.cardElevation(5.dp)){Column(Modifier.fillMaxWidth().padding(20.dp),horizontalAlignment=Alignment.CenterHorizontally){Canvas(Modifier.size(110.dp)){drawGameIllustration(game.kind,game.accent)};Spacer(Modifier.height(8.dp));Text(question.prompt,fontSize=25.sp,fontWeight=FontWeight.ExtraBold,textAlign=TextAlign.Center);Spacer(Modifier.height(10.dp));FilledTonalButton(onClick={onSpeak?.invoke(question.speech,"ar")}){Icon(Icons.Default.PlayArrow,null);Spacer(Modifier.width(7.dp));Text("اسمع السؤال")};Spacer(Modifier.height(14.dp));question.options.forEach{option->AnswerButton(option,option==question.answer,selectedAnswer,answered,game.accent){if(!answered){selectedAnswer=option;answered=true;val correct=option==question.answer;repo.recordAnswer(correct);if(correct){score++;repo.addStars(1);success=true;onSpeak?.invoke("أحسنت! إجابة رائعة","ar")}else{success=false;onSpeak?.invoke("حاول مرة أخرى، أنت تستطيع","ar")}}}}}}
            Spacer(Modifier.height(12.dp));AnimatedVisibility(visible=answered,enter=slideInHorizontally()+fadeIn(),exit=fadeOut()){Button(onClick={questionIndex++;answered=false;selectedAnswer=null;success=false},modifier=Modifier.fillMaxWidth().height(54.dp),shape=RoundedCornerShape(18.dp),colors=ButtonDefaults.buttonColors(containerColor=game.accent)){Text(if(success)"رائع! السؤال التالي ➜" else "السؤال التالي ➜",fontSize=18.sp,fontWeight=FontWeight.Bold)}};Spacer(Modifier.height(8.dp))
        }
    }
}

@Composable
private fun AnswerButton(option:String,correct:Boolean,selected:String?,answered:Boolean,accent:Color,onClick:()->Unit){
    val selectedThis=selected==option; val container=when{!answered->Color(0xFFF7F8FC);selectedThis&&correct->Color(0xFFDFF7E8);selectedThis->Color(0xFFFFE4E4);correct->Color(0xFFEAF8EF);else->Color(0xFFF7F8FC)}
    Card(Modifier.fillMaxWidth().padding(vertical=4.dp).clickable(enabled=!answered,onClick=onClick),shape=RoundedCornerShape(18.dp),colors=CardDefaults.cardColors(containerColor=container),border=if(!answered)androidx.compose.foundation.BorderStroke(1.dp,accent.copy(.20f))else null){Row(Modifier.fillMaxWidth().padding(horizontal=18.dp,vertical=13.dp),verticalAlignment=Alignment.CenterVertically){Text(option,Modifier.weight(1f),fontSize=20.sp,fontWeight=FontWeight.Bold,textAlign=TextAlign.Center);if(answered&&(selectedThis||correct))Text(if(correct)"✓" else "✕",fontSize=22.sp,fontWeight=FontWeight.ExtraBold)}}
}
