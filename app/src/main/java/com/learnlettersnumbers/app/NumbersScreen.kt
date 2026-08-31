package com.learnlettersnumbers.app

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.platform.LocalLayoutDirection
import kotlin.random.Random

private enum class NumberMode { NUMBERS, OPERATIONS, PRACTICE }
private enum class OperationMode { ADD, SUBTRACT }
private enum class PictureKind { FLOWERS, FRUITS, ANIMALS, BIKES }
private data class OperationExample(val a:Int,val b:Int,val result:Int,val kind:PictureKind,val add:Boolean)

@Composable
internal fun NumbersScreen(audio: LocalAudioManager, onTap: () -> Unit, onBack: () -> Unit, soundsEnabled: () -> Boolean = { true }) {
    val context=LocalContext.current
    val repo=remember{ProgressRepository(context)}
    var mode by remember{mutableStateOf(NumberMode.NUMBERS)}
    var operation by remember{mutableStateOf(OperationMode.ADD)}
    var selected by remember{mutableIntStateOf(1)}
    var exampleSeed by remember{mutableIntStateOf(0)}
    var practiceSeed by remember{mutableIntStateOf(1)}
    var selectedAnswer by remember{mutableStateOf<Int?>(null)}
    var practiceAnswered by remember{mutableStateOf(false)}
    val example=remember(operation,exampleSeed){makeExample(operation,exampleSeed)}
    val practice=remember(operation,practiceSeed){makeExample(operation,practiceSeed+1000)}
    LaunchedEffect(mode,selected){if(mode==NumberMode.NUMBERS&&soundsEnabled())audio.playRequired("ar_number_%03d".format(selected))}
    fun speakExample(e:OperationExample){if(!soundsEnabled())return;val op=if(e.add)"زائد" else "ناقص";val text=if(e.add)"لدينا ${numberWords(e.a)}، نضيف إليها ${numberWords(e.b)}. ${numberWords(e.a)} زائد ${numberWords(e.b)} يساوي ${numberWords(e.result)}." else "لدينا ${numberWords(e.a)}، نأخذ منها ${numberWords(e.b)}. ${numberWords(e.a)} ناقص ${numberWords(e.b)} يساوي ${numberWords(e.result)}.";audio.speakOffline(text,"ar")}
    CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Rtl){Column(Modifier.fillMaxSize().background(Brush.verticalGradient(listOf(MaterialTheme.colorScheme.background,MaterialTheme.colorScheme.surfaceVariant))).padding(14.dp),horizontalAlignment=Alignment.CenterHorizontally){
        Row(Modifier.fillMaxWidth(),verticalAlignment=Alignment.CenterVertically){Number3DButton("رجوع",Color(0xFF7E57C2),Modifier.width(100.dp)){onBack();onTap()};Spacer(Modifier.weight(1f));Text("الأرقام والعمليات",fontSize=28.sp,fontWeight=FontWeight.ExtraBold,color=Color(0xFF0C5C86))}
        Spacer(Modifier.height(10.dp));Row(Modifier.fillMaxWidth().background(MaterialTheme.colorScheme.surface.copy(alpha=.82f),RoundedCornerShape(20.dp)).padding(6.dp),horizontalArrangement=Arrangement.spacedBy(7.dp)){
            NumberModeButton("الأرقام ١–١٠٠",mode==NumberMode.NUMBERS,Modifier.weight(1f)){mode=NumberMode.NUMBERS;onTap()};NumberModeButton("الجمع والطرح",mode==NumberMode.OPERATIONS,Modifier.weight(1f)){mode=NumberMode.OPERATIONS;onTap()};NumberModeButton("تدرّب",mode==NumberMode.PRACTICE,Modifier.weight(1f)){mode=NumberMode.PRACTICE;practiceSeed++;selectedAnswer=null;practiceAnswered=false;onTap()}}
        Spacer(Modifier.height(8.dp));when(mode){
            NumberMode.NUMBERS->NumbersPager(selected,onTap,{if(selected>1)selected--},{if(selected<100)selected++},audio,soundsEnabled)
            NumberMode.OPERATIONS->{Row(Modifier.fillMaxWidth(),horizontalArrangement=Arrangement.spacedBy(10.dp)){OperationButton("الجمع +",operation==OperationMode.ADD,Modifier.weight(1f),Color(0xFF2EAD69)){operation=OperationMode.ADD;onTap()};OperationButton("الطرح −",operation==OperationMode.SUBTRACT,Modifier.weight(1f),Color(0xFFE85D5D)){operation=OperationMode.SUBTRACT;onTap()}};Spacer(Modifier.height(10.dp));OperationCard(example,onTap={onTap()},speak={speakExample(example)});Spacer(Modifier.height(10.dp));Number3DButton("مثال جديد ✨",Color(0xFF039BE5),Modifier.fillMaxWidth()){exampleSeed++;onTap()}}
            NumberMode.PRACTICE->{Row(Modifier.fillMaxWidth(),horizontalArrangement=Arrangement.spacedBy(10.dp)){OperationButton("جمع +",operation==OperationMode.ADD,Modifier.weight(1f),Color(0xFF2EAD69)){operation=OperationMode.ADD;practiceSeed++;selectedAnswer=null;practiceAnswered=false;onTap()};OperationButton("طرح −",operation==OperationMode.SUBTRACT,Modifier.weight(1f),Color(0xFFE85D5D)){operation=OperationMode.SUBTRACT;practiceSeed++;selectedAnswer=null;practiceAnswered=false;onTap()}};Spacer(Modifier.height(10.dp));PracticeCard(practice,selectedAnswer,practiceAnswered,onSpeak={if(soundsEnabled()){val op=if(practice.add)"زائد" else "ناقص";audio.speakOffline("${numberWords(practice.a)} $op ${numberWords(practice.b)} يساوي كم؟ خذ وقتك وفكر ثم اختر الإجابة.","ar")}},onAnswer={answer->if(!practiceAnswered){selectedAnswer=answer;practiceAnswered=true;repo.recordAnswer(answer==practice.result);if(answer==practice.result)repo.addStars(1);if(soundsEnabled())audio.speakOffline(if(answer==practice.result)"أحسنت! إجابة صحيحة." else "حاول مرة أخرى. الإجابة الصحيحة هي ${numberWords(practice.result)}.","ar")}},onNext={practiceSeed++;selectedAnswer=null;practiceAnswered=false;onTap()})}
        }
    }}
}

@Composable private fun NumbersPager(selected:Int,onTap:()->Unit,onPrev:()->Unit,onNext:()->Unit,audio:LocalAudioManager,soundsEnabled:()->Boolean){Column(Modifier.fillMaxSize(),horizontalAlignment=Alignment.CenterHorizontally){Box(Modifier.fillMaxWidth().weight(1f).shadow(12.dp,RoundedCornerShape(30.dp)).background(MaterialTheme.colorScheme.surface,RoundedCornerShape(30.dp)).border(5.dp,numberColor(selected),RoundedCornerShape(30.dp)).clickable{if(soundsEnabled())audio.playRequired("ar_number_%03d".format(selected));onTap()},contentAlignment=Alignment.Center){Column(horizontalAlignment=Alignment.CenterHorizontally,verticalArrangement=Arrangement.Center){Text(arabicDigits(selected),fontSize=128.sp,fontWeight=FontWeight.Black,color=numberColor(selected),textAlign=TextAlign.Center);Spacer(Modifier.height(10.dp));Text(numberWords(selected),fontSize=25.sp,fontWeight=FontWeight.Bold,color=Color(0xFF155E8A),textAlign=TextAlign.Center);Spacer(Modifier.height(8.dp));Text("اضغط على الرقم لسماع النطق 🔊",fontSize=16.sp,fontWeight=FontWeight.Bold,color=Color(0xFF6B7280))}};Spacer(Modifier.height(12.dp));Row(Modifier.fillMaxWidth().height(62.dp),horizontalArrangement=Arrangement.spacedBy(12.dp)){NumberNavigationButton("السابق",selected>1,Color(0xFF7E57C2),Modifier.weight(1f),onPrev);NumberNavigationButton("التالي",selected<100,Color(0xFF039BE5),Modifier.weight(1f),onNext)}}}

@Composable private fun PracticeCard(example:OperationExample,answer:Int?,answered:Boolean,onSpeak:()->Unit,onAnswer:(Int)->Unit,onNext:()->Unit){val options=remember(example){listOf(example.result,example.result+1,maxOf(0,example.result-1),example.result+2).distinct().shuffled(Random(example.a*31+example.b*17+example.result))};Column(Modifier.fillMaxWidth().shadow(12.dp,RoundedCornerShape(28.dp)).background(MaterialTheme.colorScheme.surface,RoundedCornerShape(28.dp)).padding(16.dp),horizontalAlignment=Alignment.CenterHorizontally){Text("🎯 تدرّب بنفسك",fontSize=26.sp,fontWeight=FontWeight.Black,color=Color(0xFF245B8A));Spacer(Modifier.height(10.dp));Text("${arabicDigits(example.a)} ${if(example.add)"+" else "−"} ${arabicDigits(example.b)} = ؟",fontSize=48.sp,fontWeight=FontWeight.Black,color=Color(0xFF7E57C2),textAlign=TextAlign.Center);Spacer(Modifier.height(6.dp));OutlinedButton(onClick=onSpeak,shape=RoundedCornerShape(16.dp)){Text("🔊 اسمع شرح السؤال",fontWeight=FontWeight.ExtraBold)};Spacer(Modifier.height(16.dp));Text("اختر الإجابة",fontSize=18.sp,fontWeight=FontWeight.Bold);Spacer(Modifier.height(8.dp));Column(Modifier.fillMaxWidth(),verticalArrangement=Arrangement.spacedBy(8.dp)){options.forEach{value->val color=when{answered&&value==example.result->Color(0xFF2EAD69);answered&&answer==value->Color(0xFFE85D5D);else->Color(0xFF039BE5)};Button(onClick={onAnswer(value)},enabled=!answered,modifier=Modifier.fillMaxWidth().height(52.dp),shape=RoundedCornerShape(16.dp),colors=ButtonDefaults.buttonColors(containerColor=color)){Text(arabicDigits(value),fontSize=24.sp,fontWeight=FontWeight.Black)}}};if(answered){Spacer(Modifier.height(12.dp));Text(if(answer==example.result)"أحسنت! إجابة صحيحة ⭐" else "الإجابة الصحيحة: ${arabicDigits(example.result)}",fontSize=19.sp,fontWeight=FontWeight.Black,color=if(answer==example.result)Color(0xFF2E7D32) else Color(0xFFC62828),textAlign=TextAlign.Center);Spacer(Modifier.height(8.dp));Number3DButton("السؤال التالي ➜",Color(0xFF7E57C2),Modifier.fillMaxWidth(),onNext)}}}

private fun makeExample(mode:OperationMode,seed:Int):OperationExample{val rnd=Random(seed+41);val a=rnd.nextInt(1,6);val b=if(mode==OperationMode.SUBTRACT)rnd.nextInt(1,a+1) else rnd.nextInt(1,6);return OperationExample(a,b,if(mode==OperationMode.ADD)a+b else a-b,PictureKind.entries[seed.coerceAtLeast(0)%PictureKind.entries.size],mode==OperationMode.ADD)}
@Composable private fun OperationCard(example:OperationExample,onTap:()->Unit,speak:()->Unit){val accent=when(example.kind){PictureKind.FLOWERS->Color(0xFFEC407A);PictureKind.FRUITS->Color(0xFFFFA726);PictureKind.ANIMALS->Color(0xFF42A5F5);PictureKind.BIKES->Color(0xFF26A69A)};Column(Modifier.fillMaxWidth().shadow(12.dp,RoundedCornerShape(30.dp)).background(MaterialTheme.colorScheme.surface,RoundedCornerShape(30.dp)).border(4.dp,accent,RoundedCornerShape(30.dp)).clickable{speak();onTap()}.padding(12.dp),horizontalAlignment=Alignment.CenterHorizontally){Row(horizontalArrangement=Arrangement.Center,verticalAlignment=Alignment.CenterVertically){PictureGroup(example.a,example.kind,accent);Text(if(example.add)"+" else "−",fontSize=48.sp,fontWeight=FontWeight.Black,color=Color(0xFF7A4A00),modifier=Modifier.padding(horizontal=8.dp));PictureGroup(example.b,example.kind,accent)};Spacer(Modifier.height(8.dp));Text("${arabicDigits(example.a)} ${if(example.add)"+" else "−"} ${arabicDigits(example.b)} = ${arabicDigits(example.result)}",fontSize=34.sp,fontWeight=FontWeight.Black,color=accent);Spacer(Modifier.height(7.dp));OutlinedButton(onClick=speak,shape=RoundedCornerShape(16.dp)){Text("🔊 اسمع الشرح",fontWeight=FontWeight.ExtraBold)}}}
@Composable private fun PictureGroup(count:Int,kind:PictureKind,accent:Color){Row(horizontalArrangement=Arrangement.spacedBy(4.dp),verticalAlignment=Alignment.CenterVertically){repeat(count){PictureIcon(kind,accent,Modifier.size(30.dp))}}}
@Composable private fun PictureIcon(kind:PictureKind,accent:Color,modifier:Modifier){Canvas(modifier){val w=size.width;val h=size.height;when(kind){PictureKind.FLOWERS->{drawCircle(Color(0xFFFFD54F),w*.15f,Offset(w*.5f,h*.45f));listOf(Offset(.5f,.2f),Offset(.25f,.42f),Offset(.75f,.42f),Offset(.35f,.7f),Offset(.65f,.7f)).forEach{p->drawCircle(accent,w*.15f,Offset(w*p.x,h*p.y))};drawLine(Color(0xFF43A047),Offset(w*.5f,h*.58f),Offset(w*.5f,h),strokeWidth=w*.08f)};PictureKind.FRUITS->{drawCircle(accent,w*.34f,Offset(w*.5f,h*.55f));drawLine(Color(0xFF5D4037),Offset(w*.5f,h*.28f),Offset(w*.58f,h*.15f),strokeWidth=w*.07f);drawOval(Color(0xFF43A047),Offset(w*.55f,h*.12f),Size(w*.28f,h*.13f))};PictureKind.ANIMALS->{drawCircle(Color(0xFFFFCC80),w*.34f,Offset(w*.5f,h*.56f));drawCircle(Color(0xFFFFCC80),w*.15f,Offset(w*.26f,h*.28f));drawCircle(Color(0xFFFFCC80),w*.15f,Offset(w*.74f,h*.28f));drawCircle(Color.Black,w*.045f,Offset(w*.42f,h*.52f));drawCircle(Color.Black,w*.045f,Offset(w*.58f,h*.52f));drawCircle(Color(0xFF5D4037),w*.07f,Offset(w*.5f,h*.68f))};PictureKind.BIKES->{drawCircle(Color.Transparent,w*.22f,Offset(w*.25f,h*.7f),style=Stroke(width=w*.06f));drawCircle(Color.Transparent,w*.22f,Offset(w*.75f,h*.7f),style=Stroke(width=w*.06f));drawLine(accent,Offset(w*.25f,h*.7f),Offset(w*.48f,h*.42f),strokeWidth=w*.06f);drawLine(accent,Offset(w*.48f,h*.42f),Offset(w*.75f,h*.7f),strokeWidth=w*.06f);drawLine(accent,Offset(w*.25f,h*.7f),Offset(w*.75f,h*.7f),strokeWidth=w*.06f);drawLine(accent,Offset(w*.48f,h*.42f),Offset(w*.38f,h*.28f),strokeWidth=w*.06f);drawLine(accent,Offset(w*.38f,h*.28f),Offset(w*.52f,h*.28f),strokeWidth=w*.06f)}}}}
@Composable private fun NumberNavigationButton(text:String,enabled:Boolean,color:Color,modifier:Modifier,onClick:()->Unit){val scale by animateFloatAsState(if(enabled)1f else .97f,spring(),label="nav_$text");Box(modifier.scale(scale).shadow(if(enabled)7.dp else 2.dp,RoundedCornerShape(18.dp)).background(if(enabled)color else color.copy(alpha=.28f),RoundedCornerShape(18.dp)).border(2.dp,if(enabled)color else color.copy(alpha=.2f),RoundedCornerShape(18.dp)).clickable(enabled=enabled,onClick=onClick),contentAlignment=Alignment.Center){Text(text,fontSize=20.sp,fontWeight=FontWeight.Black,color=if(enabled)Color.White else Color.Gray)}}
@Composable private fun NumberModeButton(text:String,selected:Boolean,modifier:Modifier,onClick:()->Unit){Box(modifier.height(52.dp).shadow(if(selected)8.dp else 3.dp,RoundedCornerShape(17.dp)).background(if(selected)Color(0xFF039BE5) else Color(0xFFEAF8FF),RoundedCornerShape(17.dp)).clickable(onClick=onClick),contentAlignment=Alignment.Center){Text(text,fontSize=17.sp,fontWeight=FontWeight.ExtraBold,color=if(selected)Color.White else Color(0xFF075B86),textAlign=TextAlign.Center)}}
