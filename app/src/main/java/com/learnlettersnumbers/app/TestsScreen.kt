package com.learnlettersnumbers.app

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlin.random.Random
import kotlinx.coroutines.delay

private enum class TestMode { ARABIC, ENGLISH }
private enum class TestKind { LETTERS, NUMBERS }
private data class TestQuestion(val prompt:String,val spokenPrompt:String,val answer:String,val options:List<String>,val kind:TestKind)

@Composable
fun TestsScreen(repo:ProgressRepository,audio:LocalAudioManager,onBack:()->Unit){
    var language by remember{mutableStateOf(TestMode.ARABIC)}
    var kind by remember{mutableStateOf(TestKind.LETTERS)}
    var questionIndex by remember{mutableStateOf(0)}
    var score by remember{mutableStateOf(0)}
    var answered by remember{mutableStateOf(false)}
    var selected by remember{mutableStateOf<String?>(null)}
    var question by remember{mutableStateOf(makeQuestion(language,kind,0))}
    fun nextQuestion(){val i=questionIndex+1;questionIndex=i;selected=null;answered=false;question=makeQuestion(language,kind,i)}
    LaunchedEffect(language,kind){questionIndex=0;score=0;selected=null;answered=false;question=makeQuestion(language,kind,0)}
    LaunchedEffect(questionIndex,language,kind){delay(120);playTestAudio(audio,language,kind,question.answer)}
    Column(Modifier.fillMaxSize().background(Brush.verticalGradient(listOf(Color(0xFFE8F9FF),Color(0xFFFFF1D7)))).padding(14.dp)){
        Row(Modifier.fillMaxWidth(),horizontalArrangement=Arrangement.SpaceBetween,verticalAlignment=Alignment.CenterVertically){Button(onClick=onBack,shape=RoundedCornerShape(18.dp)){Text("رجوع")};Column(horizontalAlignment=Alignment.End){Text("الاختبارات",fontSize=29.sp,fontWeight=FontWeight.ExtraBold,color=Color(0xFF2357A6));Text("اختبر مهاراتك وتعلم من أخطائك",fontSize=15.sp)}}
        Row(Modifier.fillMaxWidth().padding(vertical=10.dp),horizontalArrangement=Arrangement.spacedBy(10.dp)){TestModeButton("العربي",language==TestMode.ARABIC,Color(0xFF4C8BF5),Modifier.weight(1f)){language=TestMode.ARABIC};TestModeButton("English",language==TestMode.ENGLISH,Color(0xFF6BCB77),Modifier.weight(1f)){language=TestMode.ENGLISH}}
        Row(Modifier.fillMaxWidth().padding(bottom=10.dp),horizontalArrangement=Arrangement.spacedBy(10.dp)){TestModeButton(if(language==TestMode.ARABIC)"الحروف" else "Letters",kind==TestKind.LETTERS,Color(0xFF9B72E8),Modifier.weight(1f)){kind=TestKind.LETTERS};TestModeButton(if(language==TestMode.ARABIC)"الأرقام" else "Numbers",kind==TestKind.NUMBERS,Color(0xFFFF8A4C),Modifier.weight(1f)){kind=TestKind.NUMBERS}}
        Card(Modifier.fillMaxWidth(),shape=RoundedCornerShape(28.dp),colors=CardDefaults.cardColors(containerColor=MaterialTheme.colorScheme.surface),elevation=CardDefaults.cardElevation(9.dp)){Column(Modifier.padding(18.dp),horizontalAlignment=Alignment.CenterHorizontally){Text("السؤال ${questionIndex+1}",color=Color(0xFF6B7280),fontWeight=FontWeight.Bold);Spacer(Modifier.height(8.dp));Text(question.prompt,fontSize=29.sp,fontWeight=FontWeight.ExtraBold,color=Color(0xFF2357A6),textAlign=TextAlign.Center);Spacer(Modifier.height(8.dp));Button(onClick={playTestAudio(audio,language,kind,question.answer)},shape=RoundedCornerShape(18.dp)){Text("🔊 اسمع السؤال")};Spacer(Modifier.height(8.dp));Text("⭐ نتيجتك: $score",fontSize=17.sp,fontWeight=FontWeight.Bold)}}
        Spacer(Modifier.height(12.dp))
        LazyVerticalGrid(columns=GridCells.Fixed(2),modifier=Modifier.weight(1f),contentPadding=PaddingValues(4.dp),horizontalArrangement=Arrangement.spacedBy(12.dp),verticalArrangement=Arrangement.spacedBy(12.dp)){
            items(items=question.options,key={it}){option->val color=when{selected==option&&option==question.answer->Color(0xFF55C878);selected==option&&option!=question.answer->Color(0xFFE96B6B);answered&&option==question.answer->Color(0xFF55C878);else->optionColor(option,question.options)};Card(Modifier.fillMaxWidth().height(100.dp).clickable(enabled=!answered){val correct=option==question.answer;selected=option;answered=true;repo.recordAnswer(correct);if(correct){score+=1;repo.addStars(1)};audio.playRequired(if(correct)"correct_en" else "wrong_en")},shape=RoundedCornerShape(24.dp),colors=CardDefaults.cardColors(containerColor=color),elevation=CardDefaults.cardElevation(8.dp)){Box(Modifier.fillMaxSize(),contentAlignment=Alignment.Center){Text(option,Modifier.fillMaxWidth(),color=Color.White,fontSize=if(kind==TestKind.LETTERS)38.sp else 30.sp,fontWeight=FontWeight.ExtraBold,textAlign=TextAlign.Center)}}}
        }
        if(answered){val correct=selected==question.answer;Text(if(language==TestMode.ARABIC){if(correct)"🎉 رائع! أحسنت! إجابتك صحيحة ⭐" else "💪 أحسنت المحاولة! لا بأس، حاول مرة أخرى ⭐"}else{if(correct)"🎉 Great! Excellent choice!" else "💪 Good try! Learn from the next attempt!"},Modifier.fillMaxWidth().padding(vertical=8.dp),textAlign=TextAlign.Center,fontSize=18.sp,fontWeight=FontWeight.Bold,color=if(correct)Color(0xFF238636) else Color(0xFFB45309));Button(onClick={nextQuestion()},modifier=Modifier.fillMaxWidth().height(56.dp),shape=RoundedCornerShape(20.dp)){Text(if(language==TestMode.ARABIC)"السؤال التالي ➜" else "Next question ➜",fontSize=19.sp,fontWeight=FontWeight.Bold)}}
    }
}

private fun optionColor(option:String,options:List<String>):Color=when(options.indexOf(option).coerceAtLeast(0)%4){0->Color(0xFF4C8BF5);1->Color(0xFFFF8A4C);2->Color(0xFF9B72E8);else->Color(0xFF00A9A5)}
@Composable private fun TestModeButton(text:String,selected:Boolean,color:Color,modifier:Modifier,onClick:()->Unit){Button(onClick=onClick,modifier=modifier.height(58.dp),shape=RoundedCornerShape(20.dp),colors=ButtonDefaults.buttonColors(containerColor=if(selected)color else MaterialTheme.colorScheme.surface,contentColor=if(selected)Color.White else color),elevation=ButtonDefaults.buttonElevation(defaultElevation=7.dp)){Text(text,fontWeight=FontWeight.ExtraBold,fontSize=16.sp)}}
private fun makeQuestion(language:TestMode,kind:TestKind,seed:Int):TestQuestion{val r=Random(seed+9173);if(kind==TestKind.LETTERS)return if(language==TestMode.ARABIC){val letters=listOf("ا","ب","ت","ث","ج","ح","خ","د","ذ","ر","ز","س","ش","ص","ض","ط","ظ","ع","غ","ف","ق","ك","ل","م","ن","ه","و","ي");val answer=letters[r.nextInt(letters.size)];val options=(listOf(answer)+letters.shuffled(r).take(3)).distinct().shuffled(r);TestQuestion("اختر الحرف: $answer","أين الحرف $answer؟ اختره من بين الحروف",answer,options,kind)}else{val letters=('A'..'Z').map{it.toString()};val answer=letters[r.nextInt(letters.size)];val options=(listOf(answer)+letters.shuffled(r).take(3)).distinct().shuffled(r);TestQuestion("Choose the letter: $answer","Find the letter $answer",answer,options,kind)};val answer=(1..100).random(r).toString();val pool=(1..100).map{it.toString()}.shuffled(r);val options=(listOf(answer)+pool.filter{it!=answer}.take(3)).distinct().shuffled(r);return if(language==TestMode.ARABIC){val ar=answer.toArabicDigits();TestQuestion("اختر الرقم: $ar","ما الرقم الذي تسمعه؟ الرقم هو $answer",ar,options.map{it.toArabicDigits()},kind)}else TestQuestion("Choose the number: $answer","Find the number $answer",answer,options,kind)}
private fun String.toArabicDigits():String=map{c->if(c in '0'..'9')('٠'.code+(c.code-'0'.code)).toChar() else c}.joinToString("")
private fun playTestAudio(audio:LocalAudioManager,language:TestMode,kind:TestKind,answer:String){if(kind==TestKind.LETTERS){if(language==TestMode.ARABIC){val letters=listOf("ا","ب","ت","ث","ج","ح","خ","د","ذ","ر","ز","س","ش","ص","ض","ط","ظ","ع","غ","ف","ق","ك","ل","م","ن","ه","و","ي");val i=letters.indexOf(answer);if(i>=0)audio.playRequired("ar_letter_%02d_sound".format(i+1))}else{val i=('A'..'Z').indexOf(answer.firstOrNull()?:'?');if(i>=0)audio.playRequired("en_letter_%02d_sound".format(i+1))}}else{val n=answer.map{c->when(c){'٠'->'0';'١'->'1';'٢'->'2';'٣'->'3';'٤'->'4';'٥'->'5';'٦'->'6';'٧'->'7';'٨'->'8';'٩'->'9';else->c}}.joinToString("").toIntOrNull();if(n!=null&&n in 1..100)audio.playRequired(if(language==TestMode.ARABIC)"ar_number_%03d".format(n) else "en_number_%03d".format(n))}}
