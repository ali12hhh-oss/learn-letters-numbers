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
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.rememberScrollState
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
                // Rewards are Arabic. Unlike general semantic messages, the store sends
                // complete Arabic sentences that must be spoken exactly as written.
                "rewards" -> RewardStoreScreen(repo=progressRepo,onBack={screen=settingsReturnScreen},speak={text ->
                    if (settingsRepo.soundsEnabled()) localAudio.speakOffline(text, "ar")
                    if (settingsRepo.effectsEnabled()) tone?.startTone(ToneGenerator.TONE_PROP_BEEP, 70)
                })
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
        CompositionLocalProvider(androidx.compose.ui.platform.LocalLayoutDirection provides androidx.compose.ui.unit.LayoutDirection.Rtl){
            Column(Modifier.fillMaxSize().background(Brush.verticalGradient(listOf(MaterialTheme.colorScheme.background,MaterialTheme.colorScheme.surfaceVariant))).padding(18.dp),horizontalAlignment=Alignment.CenterHorizontally){Row(Modifier.fillMaxWidth(),horizontalArrangement=Arrangement.SpaceBetween,verticalAlignment=Alignment.CenterVertically){Row(horizontalArrangement=Arrangement.spacedBy(8.dp)){Button(onClick=onSettings,shape=RoundedCornerShape(18.dp)){Text("⚙ الإعدادات")};Button(onClick=onBack,shape=RoundedCornerShape(18.dp)){Text("رجوع")}};Text("العربية",fontSize=30.sp,fontWeight=FontWeight.ExtraBold,color=Color(0xFF2C5F8A))};Text("قسم اللغة العربية",fontSize=18.sp,color=Color(0xFF5B5B5B),modifier=Modifier.padding(bottom=14.dp));ArabicCard("🔤","الحروف","الحروف العربية وأصواتها",Color(0xFF4C8BF5),onLetters);ArabicCard("🔢","الأرقام","الأرقام والجمع والطرح",Color(0xFFFF8A4C),onNumbers);ArabicCard("🖊️","تعلم الكتابة","اتجاه القلم من البداية إلى النهاية",Color(0xFF9B7EDE),onTutorial);ArabicCard("✏️","القراءة","اكتب وتدرّب على السبورة",Color(0xFF6BCB77),onWriting)}
        }
    }

    @Composable fun ArabicCard(icon:String,title:String,subtitle:String,color:Color,onClick:()->Unit){var pressed by remember{mutableStateOf(false)};val scale by animateFloatAsState(if(pressed).96f else 1f,label="arabic_card");Card(Modifier.fillMaxWidth().padding(vertical=6.dp).scale(scale).clickable{pressed=true;onClick();pressed=false},shape=RoundedCornerShape(28.dp),colors=CardDefaults.cardColors(containerColor=color),elevation=CardDefaults.cardElevation(10.dp)){Row(Modifier.fillMaxWidth().padding(18.dp),verticalAlignment=Alignment.CenterVertically){Text(icon,fontSize=40.sp);Spacer(Modifier.width(16.dp));Column(Modifier.weight(1f)){Text(title,color=Color.White,fontSize=25.sp,fontWeight=FontWeight.Bold);Text(subtitle,color=Color.White.copy(.95f),fontSize=16.sp)}}}}

    @Composable fun EnglishSection(onSettings:()->Unit,onLetters:()->Unit,onNumbers:()->Unit,onTutorial:()->Unit,onWriting:()->Unit,onProgress:()->Unit,onRewards:()->Unit,onTests:()->Unit,onStories:()->Unit,onStages:()->Unit,onGames:()->Unit,onBack:()->Unit,speak:(String)->Unit){Column(Modifier.fillMaxSize().background(Brush.verticalGradient(listOf(MaterialTheme.colorScheme.background,MaterialTheme.colorScheme.surfaceVariant))).padding(horizontal=18.dp,vertical=10.dp),horizontalAlignment=Alignment.CenterHorizontally){Row(Modifier.fillMaxWidth(),verticalAlignment=Alignment.CenterVertically){Button(onClick=onBack,modifier=Modifier.height(42.dp),shape=RoundedCornerShape(14.dp),colors=ButtonDefaults.buttonColors(containerColor=Color(0xFF546E7A)),elevation=ButtonDefaults.buttonElevation(defaultElevation=6.dp)){Text("↩ رجوع",fontSize=14.sp,fontWeight=FontWeight.ExtraBold,color=Color.White)};Spacer(Modifier.width(10.dp));Text("English",fontSize=30.sp,fontWeight=FontWeight.ExtraBold,color=Color(0xFF2357A6),modifier=Modifier.weight(1f),textAlign=TextAlign.End)};Spacer(Modifier.height(6.dp));Row(Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()),horizontalArrangement=Arrangement.spacedBy(6.dp),verticalAlignment=Alignment.CenterVertically){EnglishTopButton("📊 تقدمي",Color(0xFF4C8BF5),onProgress);EnglishTopButton("🏆 مكافأة",Color(0xFFFFA726),onRewards);EnglishTopButton("📝 اختبارات",Color(0xFF9B72E8),onTests);EnglishTopButton("📖 قصص",Color(0xFF26A69A),onStories);EnglishTopButton("🎯 مراحل",Color(0xFF66BB6A),onStages);EnglishTopButton("🎮 ألعاب",Color(0xFFEC407A),onGames);EnglishTopButton("⚙ الإعدادات",Color(0xFF5C6BC0),onSettings)};Text("قسم اللغة الإنجليزية",fontSize=17.sp,color=Color(0xFF5B5B5B),modifier=Modifier.padding(bottom=8.dp));EnglishCard("🔤","Letters","الحروف الإنجليزية",Color(0xFF4C8BF5),onLetters);EnglishCard("🔢","Numbers","الأرقام الإنجليزية",Color(0xFFFF8A4C),onNumbers);EnglishCard("🖊️","Learn to Write","تعلم الكتابة خطوة بخطوة",Color(0xFF9B72E8),onTutorial);EnglishCard("✏️","Writing","الكتابة والتدريب",Color(0xFF6BCB77),onWriting)}}

    @Composable fun EnglishTopButton(text:String,color:Color,onClick:()->Unit){Button(onClick=onClick,modifier=Modifier.height(40.dp),shape=RoundedCornerShape(14.dp),contentPadding=PaddingValues(horizontal=9.dp,vertical=0.dp),colors=ButtonDefaults.buttonColors(containerColor=color),elevation=ButtonDefaults.buttonElevation(defaultElevation=5.dp)){Text(text,fontSize=12.sp,fontWeight=FontWeight.ExtraBold,maxLines=1)}}
    @Composable fun EnglishCard(icon:String,title:String,arabic:String,color:Color,onClick:()->Unit){var pressed by remember{mutableStateOf(false)};val scale by animateFloatAsState(if(pressed).96f else 1f,label="scale");Card(Modifier.fillMaxWidth().padding(vertical=5.dp).scale(scale).clickable{pressed=true;onClick();pressed=false},shape=RoundedCornerShape(28.dp),colors=CardDefaults.cardColors(containerColor=color),elevation=CardDefaults.cardElevation(10.dp)){Row(Modifier.padding(16.dp),verticalAlignment=Alignment.CenterVertically){Text(icon,fontSize=38.sp);Spacer(Modifier.width(18.dp));Column{Text(title,color=Color.White,fontSize=25.sp,fontWeight=FontWeight.Bold);Text(arabic,color=Color.White.copy(.95f),fontSize=17.sp)}}}}

    @Composable fun EnglishNumbers(onBack:()->Unit,speak:(String)->Unit,playNumber:(Int)->Unit,repo:ProgressRepository){var selected by remember{mutableIntStateOf(1)};LaunchedEffect(Unit){repo.addStars(1)};LaunchedEffect(selected){speak(numberName(selected))};Column(Modifier.fillMaxSize().background(Brush.verticalGradient(listOf(MaterialTheme.colorScheme.background,MaterialTheme.colorScheme.surfaceVariant))).padding(14.dp)){Row(Modifier.fillMaxWidth(),horizontalArrangement=Arrangement.SpaceBetween,verticalAlignment=Alignment.CenterVertically){Button(onClick=onBack,shape=RoundedCornerShape(18.dp)){Text("Back")};Column(horizontalAlignment=Alignment.End){Text("Numbers",fontSize=29.sp,fontWeight=FontWeight.ExtraBold,color=Color(0xFF2357A6));Text("الأرقام الإنجليزية",fontSize=16.sp)}};Text("Numbers 1–100",Modifier.fillMaxWidth().padding(vertical=8.dp),textAlign=TextAlign.Center,fontSize=18.sp,fontWeight=FontWeight.Bold,color=Color(0xFF2357A6));Card(Modifier.fillMaxWidth().weight(1f),shape=RoundedCornerShape(30.dp),colors=CardDefaults.cardColors(containerColor=MaterialTheme.colorScheme.surface),elevation=CardDefaults.cardElevation(9.dp)){Column(Modifier.fillMaxSize().padding(18.dp),horizontalAlignment=Alignment.CenterHorizontally,verticalArrangement=Arrangement.Center){Text(selected.toString(),fontSize=112.sp,fontWeight=FontWeight.Black,color=Color(0xFF2357A6),textAlign=TextAlign.Center);Spacer(Modifier.height(12.dp));Text(numberName(selected),fontSize=25.sp,fontWeight=FontWeight.Bold,color=Color(0xFF155E8A),textAlign=TextAlign.Center);Spacer(Modifier.height(14.dp));Button(onClick={playNumber(selected)},shape=RoundedCornerShape(18.dp)){Text("🔊 Listen",fontSize=18.sp)}}};Spacer(Modifier.height(10.dp));Row(Modifier.fillMaxWidth(),horizontalArrangement=Arrangement.spacedBy(8.dp)){Button(onClick={if(selected>1)selected--},modifier=Modifier.weight(1f)){Text("Previous")};Button(onClick={if(selected<100)selected++},modifier=Modifier.weight(1f)){Text("Next")}}}}

    private fun numberName(n:Int):String{val ones=arrayOf("zero","one","two","three","four","five","six","seven","eight","nine","ten","eleven","twelve","thirteen","fourteen","fifteen","sixteen","seventeen","eighteen","nineteen");val tens=arrayOf("","","twenty","thirty","forty","fifty","sixty","seventy","eighty","ninety");return when{n<20->ones[n];n%10==0->tens[n/10];else->"${tens[n/10]} ${ones[n%10]}"}}
}
