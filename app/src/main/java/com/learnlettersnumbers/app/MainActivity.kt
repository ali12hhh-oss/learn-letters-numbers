package com.learnlettersnumbers.app

import android.os.Bundle
import android.media.AudioManager
import android.media.ToneGenerator
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlin.math.min

class MainActivity : ComponentActivity() {
    private var tone: ToneGenerator? = null
    private lateinit var localAudio: LocalAudioManager
    private lateinit var progressRepo: ProgressRepository
    private lateinit var settingsRepo: SettingsRepository

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        tone = ToneGenerator(AudioManager.STREAM_NOTIFICATION, 80)
        progressRepo = ProgressRepository(this)
        settingsRepo = SettingsRepository(this)
        localAudio = LocalAudioManager(this)
        AudioCatalogValidator.validateOrThrow(this)
        localAudio.setEnabled(settingsRepo.soundsEnabled())
        ChildProfileRepository.init(this)
        setContent { App() }
    }

    fun speak(text: String) {
        if (settingsRepo.soundsEnabled()) localAudio.playSemantic(text, "en")
        if (settingsRepo.effectsEnabled()) tone?.startTone(ToneGenerator.TONE_PROP_BEEP, 70)
    }

    fun speakArabic(text: String) {
        if (settingsRepo.soundsEnabled()) localAudio.playSemantic(text, "ar")
        if (settingsRepo.effectsEnabled()) tone?.startTone(ToneGenerator.TONE_PROP_BEEP, 70)
    }

    override fun onPause() { localAudio.stop(); tone?.stopTone(); super.onPause() }
    override fun onDestroy() { localAudio.releaseAll(); tone?.release(); super.onDestroy() }

    @Composable
    fun App() {
        var screen by remember { mutableStateOf("home") }
        var settingsReturnScreen by remember { mutableStateOf("home") }
        var darkMode by remember { mutableStateOf(settingsRepo.darkMode()) }
        LearnLettersNumbersTheme(darkTheme = darkMode) {
            DisposableEffect(screen) { onDispose { localAudio.stop(); tone?.stopTone() } }
            when (screen) {
                "home" -> HomeSection(onArabic={screen="arabic"}, onEnglish={screen="english"}, onProgress={settingsReturnScreen="home";screen="progress"}, onRewards={settingsReturnScreen="home";screen="rewards"}, onTests={settingsReturnScreen="home";screen="tests"}, onStories={settingsReturnScreen="home";screen="stories"}, onGames={settingsReturnScreen="home";screen="games"}, onStages={settingsReturnScreen="home";screen="stages"}, onSettings={settingsReturnScreen="home";screen="settings"}, speak={speakArabic(it)})
                "arabic" -> ArabicSection(onSettings={settingsReturnScreen="arabic";screen="settings"}, onLetters={screen="arabic_letters"}, onNumbers={screen="arabic_numbers"}, onTutorial={screen="arabic_tutorial"}, onWriting={screen="arabic_reading"}, onBack={screen="home"}, speak={speakArabic(it)})
                "arabic_letters" -> LettersScreen(audio=localAudio,onTap={if(settingsRepo.effectsEnabled())tone?.startTone(ToneGenerator.TONE_PROP_BEEP,70)},onBack={screen="arabic"},soundsEnabled={settingsRepo.soundsEnabled()})
                "arabic_numbers" -> NumbersScreen(audio=localAudio,onTap={if(settingsRepo.effectsEnabled())tone?.startTone(ToneGenerator.TONE_PROP_BEEP,70)},onBack={screen="arabic"},soundsEnabled={settingsRepo.soundsEnabled()})
                "arabic_reading" -> ReadingScreen(audio=localAudio,onTap={if(settingsRepo.effectsEnabled())tone?.startTone(ToneGenerator.TONE_PROP_BEEP,70)},onBack={screen="arabic"},soundsEnabled={settingsRepo.soundsEnabled()})
                "arabic_tutorial" -> WritingStrokeLessonScreen(language="ar",numbers=false,onBack={screen="arabic"},speak={msg,lang->if(lang=="ar")speakArabic(msg)else speak(msg)})
                "english_tutorial" -> WritingStrokeLessonScreen(language="en",numbers=false,onBack={screen="english"},speak={msg,lang->if(lang=="ar")speakArabic(msg)else speak(msg)})
                "english" -> EnglishSection(onSettings={settingsReturnScreen="english";screen="settings"},onLetters={screen="letters"},onNumbers={screen="numbers"},onWriting={screen="writing"},onTutorial={screen="english_tutorial"},onProgress={screen="progress"},onRewards={screen="rewards"},onTests={screen="tests"},onStories={screen="stories"},onStages={screen="stages"},onGames={screen="games"},onBack={screen="home"},speak={speak(it)})
                "letters" -> EnglishLettersScreen(audio=localAudio,onTap={if(settingsRepo.effectsEnabled())tone?.startTone(ToneGenerator.TONE_PROP_BEEP,70)},onBack={screen="english"},onLetterSeen={progressRepo.recordLetterSeen(it)},soundsEnabled={settingsRepo.soundsEnabled()})
                "numbers" -> EnglishNumbers(onBack={screen="english"},speak={speak(it)},playNumber={n->localAudio.playRequired("en_number_%03d".format(n))},repo=progressRepo)
                "writing" -> EnglishWriting(onBack={screen="english"},speak={speak(it)},repo=progressRepo)
                "progress" -> ParentProgressScreen(repo=progressRepo,onBack={screen=settingsReturnScreen},speak={speak(it)})
                "rewards" -> RewardStoreScreen(repo=progressRepo,onBack={screen=settingsReturnScreen},speak={speak(it)})
                "tests" -> TestsScreen(repo=progressRepo,audio=localAudio,onBack={screen=settingsReturnScreen})
                "stories" -> StoriesScreen(audio=localAudio,onBack={screen=settingsReturnScreen})
                "stages" -> StagesScreen(repo=progressRepo,onBack={screen=settingsReturnScreen},onStageOpen={stage->screen=when(stage){1->"arabic";2->"arabic_tutorial";3->"tests";else->"writing"}},speakArabic={speakArabic(it)})
                "games" -> GamesScreen(onBack={screen=settingsReturnScreen},repo=progressRepo,onSpeak={msg,lang->if(lang=="ar")speakArabic(msg)else speak(msg)})
                "settings" -> SettingsScreen(repo=settingsRepo,darkMode=darkMode,onDarkModeChanged={darkMode=it},onSoundsChanged={localAudio.setEnabled(it)},onEffectsChanged={if(!it)tone?.stopTone()},onBack={screen=settingsReturnScreen})
            }
        }
    }

    @Composable
    fun ArabicSection(onSettings:()->Unit,onLetters:()->Unit,onNumbers:()->Unit,onTutorial:()->Unit,onWriting:()->Unit,onBack:()->Unit,speak:(String)->Unit) {
        LaunchedEffect(Unit){speak("أهلاً بك في قسم اللغة العربية. هيا نتعلم معاً!")}
        CompositionLocalProvider(androidx.compose.ui.platform.LocalLayoutDirection provides androidx.compose.ui.unit.LayoutDirection.Rtl){
            Column(Modifier.fillMaxSize().background(Brush.verticalGradient(listOf(MaterialTheme.colorScheme.background,MaterialTheme.colorScheme.surfaceVariant))).padding(18.dp),horizontalAlignment=Alignment.CenterHorizontally){
                Row(Modifier.fillMaxWidth(),horizontalArrangement=Arrangement.SpaceBetween,verticalAlignment=Alignment.CenterVertically){Row(horizontalArrangement=Arrangement.spacedBy(8.dp)){Button(onClick=onSettings,shape=RoundedCornerShape(18.dp)){Text("⚙ الإعدادات")};Button(onClick=onBack,shape=RoundedCornerShape(18.dp)){Text("رجوع")}};Text("العربية",fontSize=30.sp,fontWeight=FontWeight.ExtraBold,color=Color(0xFF2C5F8A))}
                Text("قسم اللغة العربية",fontSize=18.sp,color=Color(0xFF5B5B5B),modifier=Modifier.padding(bottom=14.dp))
                ArabicCard("🔤","الحروف","الحروف العربية وأصواتها",Color(0xFF4C8BF5),onLetters)
                ArabicCard("🔢","الأرقام","الأرقام والجمع والطرح",Color(0xFFFF8A4C),onNumbers)
                ArabicCard("🖊️","تعلم الكتابة","اتجاه القلم من البداية إلى النهاية",Color(0xFF9B7EDE),onTutorial)
                ArabicCard("✏️","القراءة","اكتب وتدرّب على السبورة",Color(0xFF6BCB77),onWriting)
            }
        }
    }

    @Composable
    fun ArabicCard(icon:String,title:String,subtitle:String,color:Color,onClick:()->Unit){
        var pressed by remember{mutableStateOf(false)};val scale by animateFloatAsState(if(pressed).96f else 1f,label="arabic_card")
        Card(Modifier.fillMaxWidth().padding(vertical=6.dp).scale(scale).clickable{pressed=true;onClick();pressed=false},shape=RoundedCornerShape(28.dp),colors=CardDefaults.cardColors(containerColor=color),elevation=CardDefaults.cardElevation(10.dp)){Row(Modifier.fillMaxWidth().padding(18.dp),verticalAlignment=Alignment.CenterVertically){Text(icon,fontSize=40.sp);Spacer(Modifier.width(16.dp));Column(Modifier.weight(1f)){Text(title,color=Color.White,fontSize=25.sp,fontWeight=FontWeight.Bold);Text(subtitle,color=Color.White.copy(.95f),fontSize=16.sp)}}}
    }

    @Composable
    fun EnglishSection(onSettings:()->Unit,onLetters:()->Unit,onNumbers:()->Unit,onTutorial:()->Unit,onWriting:()->Unit,onProgress:()->Unit,onRewards:()->Unit,onTests:()->Unit,onStories:()->Unit,onStages:()->Unit,onGames:()->Unit,onBack:()->Unit,speak:(String)->Unit){
        LaunchedEffect(Unit){speak("Hello! Welcome to the English section. Let's learn together!")}
        Column(Modifier.fillMaxSize().background(Brush.verticalGradient(listOf(MaterialTheme.colorScheme.background,MaterialTheme.colorScheme.surfaceVariant))).padding(horizontal=18.dp,vertical=10.dp),horizontalAlignment=Alignment.CenterHorizontally){
            Row(Modifier.fillMaxWidth(),horizontalArrangement=Arrangement.SpaceBetween,verticalAlignment=Alignment.CenterVertically){Row(horizontalArrangement=Arrangement.spacedBy(6.dp),verticalAlignment=Alignment.CenterVertically){EnglishTopButton("📊 تقدمي",Color(0xFF4C8BF5),onProgress);EnglishTopButton("🏆 مكافأة",Color(0xFFFFA726),onRewards);EnglishTopButton("📝 اختبارات",Color(0xFF9B72E8),onTests);EnglishTopButton("📖 قصص",Color(0xFF26A69A),onStories);EnglishTopButton("🎯 مراحل",Color(0xFF66BB6A),onStages);EnglishTopButton("🎮 ألعاب",Color(0xFFEC407A),onGames);EnglishTopButton("⚙ الإعدادات",Color(0xFF5C6BC0),onSettings);EnglishTopButton("↩ رجوع",Color(0xFF546E7A),onBack)};Text("English",fontSize=30.sp,fontWeight=FontWeight.ExtraBold,color=Color(0xFF2357A6))}
            Text("قسم اللغة الإنجليزية",fontSize=17.sp,color=Color(0xFF5B5B5B),modifier=Modifier.padding(bottom=8.dp));EnglishCard("🔤","Letters","الحروف الإنجليزية",Color(0xFF4C8BF5),onLetters);EnglishCard("🔢","Numbers","الأرقام الإنجليزية",Color(0xFFFF8A4C),onNumbers);EnglishCard("🖊️","Learn to Write","تعلم الكتابة خطوة بخطوة",Color(0xFF9B72E8),onTutorial);EnglishCard("✏️","Writing","الكتابة والتدريب",Color(0xFF6BCB77),onWriting)
        }
    }

    @Composable fun EnglishTopButton(text:String,color:Color,onClick:()->Unit){Button(onClick=onClick,modifier=Modifier.height(40.dp),shape=RoundedCornerShape(14.dp),contentPadding=PaddingValues(horizontal=9.dp,vertical=0.dp),colors=ButtonDefaults.buttonColors(containerColor=color),elevation=ButtonDefaults.buttonElevation(defaultElevation=5.dp)){Text(text,fontSize=12.sp,fontWeight=FontWeight.ExtraBold,maxLines=1)}}
    @Composable fun EnglishCard(icon:String,title:String,arabic:String,color:Color,onClick:()->Unit){var pressed by remember{mutableStateOf(false)};val scale by animateFloatAsState(if(pressed).96f else 1f,label="scale");Card(Modifier.fillMaxWidth().padding(vertical=5.dp).scale(scale).clickable{pressed=true;onClick();pressed=false},shape=RoundedCornerShape(28.dp),colors=CardDefaults.cardColors(containerColor=color),elevation=CardDefaults.cardElevation(10.dp)){Row(Modifier.padding(16.dp),verticalAlignment=Alignment.CenterVertically){Text(icon,fontSize=38.sp);Spacer(Modifier.width(18.dp));Column{Text(title,color=Color.White,fontSize=25.sp,fontWeight=FontWeight.Bold);Text(arabic,color=Color.White.copy(.95f),fontSize=17.sp)}}}}

    @Composable
    fun EnglishNumbers(onBack:()->Unit,speak:(String)->Unit,playNumber:(Int)->Unit,repo:ProgressRepository){var mode by remember{mutableStateOf("ones")};var selected by remember{mutableStateOf(1)};val nums=if(mode=="ones")(1..9).toList()else(10..100 step 10).toList();LaunchedEffect(Unit){repo.addStars(1);speak("Welcome to English numbers. Choose a number and listen!")};Column(Modifier.fillMaxSize().background(Brush.verticalGradient(listOf(MaterialTheme.colorScheme.background,MaterialTheme.colorScheme.surfaceVariant))).padding(14.dp)){Row(Modifier.fillMaxWidth(),horizontalArrangement=Arrangement.SpaceBetween,verticalAlignment=Alignment.CenterVertically){Button(onClick=onBack,shape=RoundedCornerShape(18.dp)){Text("Back")};Column(horizontalAlignment=Alignment.End){Text("Numbers",fontSize=29.sp,fontWeight=FontWeight.ExtraBold,color=Color(0xFF2357A6));Text("الأرقام الإنجليزية",fontSize=16.sp)}};Row(Modifier.fillMaxWidth().padding(vertical=12.dp),horizontalArrangement=Arrangement.spacedBy(10.dp)){ModeButton("1–9","الآحاد",mode=="ones",Color(0xFF4C8BF5),Modifier.weight(1f)){mode="ones";selected=1;speak("One to nine")};ModeButton("10–100","العشرات",mode=="tens",Color(0xFFFF8A4C),Modifier.weight(1f)){mode="tens";selected=10;speak("Ten to one hundred")}};Card(Modifier.fillMaxWidth().padding(bottom=12.dp),shape=RoundedCornerShape(30.dp),colors=CardDefaults.cardColors(containerColor=MaterialTheme.colorScheme.surface),elevation=CardDefaults.cardElevation(9.dp)){Column(Modifier.padding(18.dp),horizontalAlignment=Alignment.CenterHorizontally){Text(selected.toString(),fontSize=68.sp,fontWeight=FontWeight.Black,color=Color(0xFF2357A6));Button(onClick={playNumber(selected)},shape=RoundedCornerShape(18.dp)){Text("🔊 Listen",fontSize=18.sp)};Text(if(mode=="ones")"Choose a number from 1 to 9" else "Choose a tens number from 10 to 100",fontSize=16.sp,modifier=Modifier.padding(top=7.dp),textAlign=TextAlign.Center)}};LazyVerticalGrid(columns=GridCells.Fixed(3),modifier=Modifier.fillMaxWidth().weight(1f),contentPadding=PaddingValues(4.dp),horizontalArrangement=Arrangement.spacedBy(10.dp),verticalArrangement=Arrangement.spacedBy(10.dp)){items(nums){n->NumberTile(n,n==selected){selected=n;repo.recordNumberSeen(n);repo.recordLesson("English numbers",n.toString());playNumber(n)}}}}
    }

    @Composable fun ModeButton(en:String,ar:String,selected:Boolean,color:Color,modifier:Modifier,onClick:()->Unit){val bg by animateColorAsState(if(selected)color else MaterialTheme.colorScheme.surface,label="modeColor");Button(onClick=onClick,modifier=modifier.height(64.dp),shape=RoundedCornerShape(22.dp),colors=ButtonDefaults.buttonColors(containerColor=bg,contentColor=if(selected)Color.White else color),elevation=ButtonDefaults.buttonElevation(defaultElevation=7.dp)){Column(horizontalAlignment=Alignment.CenterHorizontally){Text(en,fontWeight=FontWeight.ExtraBold);Text(ar,fontSize=13.sp)}}}
    @Composable fun NumberTile(n:Int,selected:Boolean,onClick:()->Unit){val colors=listOf(Color(0xFF4C8BF5),Color(0xFFFF8A4C),Color(0xFF6BCB77),Color(0xFF9B72E8),Color(0xFFE85D9E));val c=colors[(n-1)%colors.size];Card(Modifier.fillMaxWidth().height(82.dp).clickable{onClick()},shape=RoundedCornerShape(22.dp),colors=CardDefaults.cardColors(containerColor=if(selected)Color(0xFFFFC857)else c),elevation=CardDefaults.cardElevation(if(selected)12.dp else 7.dp)){Box(contentAlignment=Alignment.Center){Text(n.toString(),fontSize=30.sp,fontWeight=FontWeight.ExtraBold,color=Color.White)}}}

    @Composable
    fun EnglishWriting(onBack:()->Unit,speak:(String)->Unit,repo:ProgressRepository){
        var mode by remember{mutableStateOf("letters")};var index by remember{mutableStateOf(0)};var selectedCase by remember{mutableStateOf("upper")};var inkColor by remember{mutableStateOf(Color(0xFF2563EB))}
        val strokes=remember{mutableStateListOf<List<Offset>>()};var currentStroke by remember{mutableStateOf<List<Offset>>(emptyList())};val letters=('A'..'Z').map{it.toString()};val total=if(mode=="letters")26 else 100;val baseLetter=letters[index.coerceIn(0,letters.lastIndex)][0];val target=if(mode=="letters"){if(selectedCase=="upper")baseLetter.toString()else baseLetter.lowercase()}else(index+1).toString()
        LaunchedEffect(Unit){repo.addStars(1)}
        fun clearBoard(){strokes.clear();currentStroke=emptyList()}
        fun speakTarget(){if(mode=="letters")speak(englishLetterSound(baseLetter))else speak(numberName(index+1))}
        LaunchedEffect(mode,index,selectedCase){clearBoard();speakTarget()}
        Column(Modifier.fillMaxSize().background(Brush.verticalGradient(listOf(Color(0xFFE9F8FF),Color(0xFFFFF1D7)))).padding(horizontal=10.dp,vertical=5.dp)){
            Row(Modifier.fillMaxWidth().height(48.dp),horizontalArrangement=Arrangement.SpaceBetween,verticalAlignment=Alignment.CenterVertically){Button(onClick=onBack,shape=RoundedCornerShape(16.dp)){Text("Back")};Text("Writing",fontSize=28.sp,fontWeight=FontWeight.ExtraBold,color=Color(0xFF2357A6))}
            Row(Modifier.fillMaxWidth().height(54.dp),horizontalArrangement=Arrangement.spacedBy(8.dp)){ModeButton("Letters","الحروف",mode=="letters",Color(0xFF4C8BF5),Modifier.weight(1f)){mode="letters";index=0;selectedCase="upper"};ModeButton("Numbers","الأرقام",mode=="numbers",Color(0xFFFF8A4C),Modifier.weight(1f)){mode="numbers";index=0}}
            if(mode=="letters"){Row(Modifier.fillMaxWidth().padding(vertical=5.dp),horizontalArrangement=Arrangement.spacedBy(8.dp)){LetterCaseChoice(letter=baseLetter.toString(),title="UPPERCASE",arabic="حرف كبير",selected=selectedCase=="upper",color=Color(0xFF4C8BF5),modifier=Modifier.weight(1f)){selectedCase="upper";speak("Capital letter ${baseLetter}")};LetterCaseChoice(letter=baseLetter.lowercase(),title="lowercase",arabic="حرف صغير",selected=selectedCase=="lower",color=Color(0xFFFF8A4C),modifier=Modifier.weight(1f)){selectedCase="lower";speak("Small letter ${baseLetter.lowercase()}")}}}
            Row(Modifier.fillMaxWidth().height(46.dp),horizontalArrangement=Arrangement.Center,verticalAlignment=Alignment.CenterVertically){Text(if(mode=="letters")"${index+1} / 26" else "${index+1} / 100",fontWeight=FontWeight.ExtraBold,fontSize=13.sp);Spacer(Modifier.width(10.dp));Text(target,fontSize=34.sp,fontWeight=FontWeight.Black,color=Color(0xFF2357A6));Spacer(Modifier.width(8.dp));IconButton(onClick={speakTarget()}){Text("🔊",fontSize=22.sp)}}
            Card(Modifier.fillMaxWidth().weight(1f),shape=RoundedCornerShape(24.dp),colors=CardDefaults.cardColors(containerColor=MaterialTheme.colorScheme.surface),elevation=CardDefaults.cardElevation(9.dp)){
                Column(Modifier.fillMaxSize().padding(6.dp),horizontalAlignment=Alignment.CenterHorizontally){
                    val boardModifier=Modifier.fillMaxWidth().weight(1f).background(Color(0xFFFDFEFF),RoundedCornerShape(20.dp))
                        .pointerInput(inkColor){detectTapGestures(onTap={point->strokes.add(listOf(point))})}
                        .pointerInput(inkColor){detectDragGestures(onDragStart={currentStroke=listOf(it)},onDrag={change,_->change.consume();currentStroke=currentStroke+change.position},onDragEnd={if(currentStroke.isNotEmpty())strokes.add(currentStroke);currentStroke=emptyList()},onDragCancel={currentStroke=emptyList()})}
                    Canvas(boardModifier){
                        val all=strokes+listOf(currentStroke)
                        all.forEach{pts->
                            if(pts.size==1){drawCircle(inkColor,8f,pts.first())}
                            else if(pts.size>1){val path=Path().apply{moveTo(pts[0].x,pts[0].y);for(i in 1 until pts.size)lineTo(pts[i].x,pts[i].y)};drawPath(path,inkColor,style=androidx.compose.ui.graphics.drawscope.Stroke(width=15f,cap=StrokeCap.Round,join=StrokeJoin.Round))}
                        }
                    }
                    Row(Modifier.fillMaxWidth().height(42.dp),horizontalArrangement=Arrangement.Center,verticalAlignment=Alignment.CenterVertically){val colors=listOf(Color(0xFF2563EB),Color(0xFF16A34A),Color(0xFFE11D48),Color(0xFF9333EA),Color(0xFFF59E0B));colors.forEach{c->Box(Modifier.padding(4.dp).size(32.dp).background(c,CircleShape).clickable{inkColor=c})}}
                }
            }
            Spacer(Modifier.height(5.dp));Row(Modifier.fillMaxWidth().height(56.dp),horizontalArrangement=Arrangement.spacedBy(7.dp)){OutlinedButton(onClick={if(index>0)index--},enabled=index>0,modifier=Modifier.weight(1f)){Text("Previous")};Button(onClick={clearBoard();repo.recordWritingPractice()},modifier=Modifier.weight(1f),colors=ButtonDefaults.buttonColors(containerColor=Color(0xFFE85D9E))){Text("Clear")};Button(onClick={repo.recordWritingPractice();if(index<total-1)index++else{repo.recordLesson("English writing",target,true);repo.addStars(1)}},modifier=Modifier.weight(1f),colors=ButtonDefaults.buttonColors(containerColor=Color(0xFF4C8BF5))){Text("Next")}}
        }
    }

    @Composable private fun LetterCaseChoice(letter:String,title:String,arabic:String,selected:Boolean,color:Color,modifier:Modifier,onClick:()->Unit){val scale by animateFloatAsState(if(selected)1.04f else 1f,label="caseChoice_$title");Card(modifier.scale(scale).clickable{onClick()},shape=RoundedCornerShape(18.dp),colors=CardDefaults.cardColors(containerColor=if(selected)color else MaterialTheme.colorScheme.surface),elevation=CardDefaults.cardElevation(if(selected)9.dp else 3.dp)){Column(Modifier.fillMaxWidth().padding(vertical=7.dp),horizontalAlignment=Alignment.CenterHorizontally){Text(letter,fontSize=34.sp,fontWeight=FontWeight.Black,color=if(selected)Color.White else color);Text(title,fontSize=14.sp,fontWeight=FontWeight.ExtraBold,color=if(selected)Color.White else Color(0xFF245B8A));Text(arabic,fontSize=12.sp,color=if(selected)Color.White else Color(0xFF666666))}}
    }

    private fun englishLetterSound(c:Char):String=when(c.lowercaseChar()){'a'->"ah";'b'->"buh";'c'->"kuh";'d'->"duh";'e'->"eh";'f'->"fff";'g'->"guh";'h'->"huh";'i'->"ih";'j'->"juh";'k'->"kuh";'l'->"lll";'m'->"mmm";'n'->"nnn";'o'->"ah";'p'->"puh";'q'->"kwuh";'r'->"rrr";'s'->"sss";'t'->"tuh";'u'->"uh";'v'->"vvv";'w'->"wuh";'x'->"ks";'y'->"yuh";'z'->"zzz";else->c.toString()}
    private fun numberName(n:Int):String=when(n){1->"one";2->"two";3->"three";4->"four";5->"five";6->"six";7->"seven";8->"eight";9->"nine";10->"ten";20->"twenty";30->"thirty";40->"forty";50->"fifty";60->"sixty";70->"seventy";80->"eighty";90->"ninety";100->"one hundred";else->n.toString()}
}
