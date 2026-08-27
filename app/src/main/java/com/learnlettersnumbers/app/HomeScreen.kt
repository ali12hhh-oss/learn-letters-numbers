package com.learnlettersnumbers.app
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.DialogProperties

@Composable
fun HomeSection(onArabic:()->Unit,onEnglish:()->Unit,onProgress:()->Unit,onRewards:()->Unit,onTests:()->Unit,onStories:()->Unit,onGames:()->Unit,onStages:()->Unit,onSettings:()->Unit,speak:(String)->Unit){
 val context=LocalContext.current
 var childName by remember{mutableStateOf(ChildProfileRepository.loadName())}
 var avatar by remember{mutableStateOf(ChildProfileRepository.loadAvatar())}
 var showProfile by remember{mutableStateOf(!ChildProfileRepository.promptSeen())}
 LaunchedEffect(Unit){speak("أهلاً بك في تطبيق تعلم الحروف والأرقام!")}
 CompositionLocalProvider(androidx.compose.ui.platform.LocalLayoutDirection provides LayoutDirection.Rtl){
  BoxWithConstraints(Modifier.fillMaxSize().background(Brush.verticalGradient(listOf(Color(0xFF087ED4),Color(0xFF4CCAF1),Color(0xFF79CB72))))){
   val w=maxWidth
   val h=maxHeight
   val gap=(w*.016f).coerceAtLeast(5.dp)
   val side=(w*.03f).coerceAtLeast(8.dp)
   Image(painter = painterResource(R.drawable.home_background),contentDescription = null,modifier = Modifier.fillMaxSize(),contentScale = ContentScale.Crop)
   Column(Modifier.fillMaxSize().statusBarsPadding().navigationBarsPadding().padding(horizontal=side,vertical=7.dp),horizontalAlignment=Alignment.CenterHorizontally){
    Row(Modifier.fillMaxWidth(),Arrangement.SpaceBetween,Alignment.Top){ProfileCard(childName,avatar){showProfile=true};Icon3D("⚙","الإعدادات",Color(0xFF7136D1),onSettings)}
    Spacer(Modifier.height((h*.012f).coerceAtMost(9.dp)))
    TitleBanner()
    Spacer(Modifier.height((h*.012f).coerceAtMost(9.dp)))
    Row(Modifier.fillMaxWidth(),Arrangement.Center,Alignment.CenterVertically){LangCard(Modifier.weight(1f),R.drawable.arabic_card,"العربية","حروف • أرقام • نطق\nألعاب • قصص • اختبارات",Color(0xFFF39A12),onArabic);Spacer(Modifier.width(gap));LangCard(Modifier.weight(1f),R.drawable.english_card,"English","Letters • Numbers • Pronunciation\nGames • Stories • Tests",Color(0xFF1979D4),onEnglish)}
    Spacer(Modifier.height((h*.01f).coerceAtMost(7.dp)))
    Text("اختر ما تريد أن تتعلمه",Modifier.background(Color(0xFF185B8E).copy(.86f),RoundedCornerShape(17.dp)).padding(horizontal=14.dp,vertical=5.dp),color=Color.White,fontSize=15.sp,fontWeight=FontWeight.Black)
    Spacer(Modifier.height(5.dp))
    Row(Modifier.fillMaxWidth(),Arrangement.spacedBy(5.dp)){Feature("🏆","المراحل",Color(0xFF6D35C9),Modifier.weight(1f),onStages);Feature("🎮","ألعاب",Color(0xFF49AA31),Modifier.weight(1f),onGames);Feature("📖","قصص",Color(0xFFD83B72),Modifier.weight(1f),onStories);Feature("📝","اختبارات",Color(0xFFE98B16),Modifier.weight(1f),onTests);Feature("🎁","مكافآت",Color(0xFF197BD7),Modifier.weight(1f),onRewards);Feature("📊","تقدمي",Color(0xFF00AFAF),Modifier.weight(1f),onProgress)}
    Spacer(Modifier.weight(1f))
    Row(Modifier.fillMaxWidth(),Arrangement.spacedBy(7.dp)){Feature("⭐","نجومك 250",Color(0xFF5D43C7),Modifier.weight(1f),onRewards);Feature("🌱","واصل التعلم",Color(0xFF5D43C7),Modifier.weight(1.25f),onProgress)}
    Spacer(Modifier.height(6.dp))
    Row(Modifier.fillMaxWidth(.78f),Arrangement.spacedBy(3.dp)){Feature("🔊","الصوت",Color(0xFF5541A6),Modifier.weight(1f),onSettings);Feature("🎵","الموسيقى",Color(0xFF5541A6),Modifier.weight(1f),onSettings);Feature("🛡","الخصوصية",Color(0xFF5541A6),Modifier.weight(1f),onSettings);Feature("ⓘ","عن التطبيق",Color(0xFF5541A6),Modifier.weight(1f),onSettings);Feature("♥","القيم",Color(0xFF5541A6),Modifier.weight(1f),onSettings)}
    Spacer(Modifier.height(4.dp))
    Text("أحسنت! ⭐ استمر هكذا لتصبح الأفضل",Modifier.fillMaxWidth(.82f).background(Color(0xFFFFE49A),RoundedCornerShape(20.dp)).padding(8.dp),color=Color(0xFFB53A2C),fontSize=14.sp,fontWeight=FontWeight.Black,textAlign=TextAlign.Center)
   }
  }
 }
 var draft by remember(childName){mutableStateOf(childName)}
 if(showProfile)AlertDialog(onDismissRequest={if(childName.isNotBlank())showProfile=false},title={Text("مرحباً بك 🌟")},text={OutlinedTextField(draft,{draft=it},label={Text("اسم الطفل")},singleLine=true,modifier=Modifier.fillMaxWidth())},confirmButton={Button({val n=draft.trim();ChildProfileRepository.saveName(n);ProgressRepository(context).setChildName(n);childName=n;ChildProfileRepository.markPromptSeen();showProfile=false}){Text("حفظ")}},dismissButton={TextButton({ChildProfileRepository.markPromptSeen();showProfile=false}){Text("تخطي")}},properties=DialogProperties(dismissOnBackPress=true,dismissOnClickOutside=childName.isNotBlank()))
}

@Composable private fun TitleBanner(){Surface(Modifier.fillMaxWidth(.84f),RoundedCornerShape(26.dp),Color.White.copy(.9f),shadowElevation=9.dp){Column(modifier=Modifier.padding(8.dp),horizontalAlignment=Alignment.CenterHorizontally){Text("تعلم الحروف والأرقام",fontSize=26.sp,fontWeight=FontWeight.Black,color=Color(0xFFEE8B00),textAlign=TextAlign.Center);Text("تعلم • العب • اكتب • أنجز ⭐",fontSize=13.sp,fontWeight=FontWeight.Bold,color=Color(0xFF315271))}}}
@Composable private fun LangCard(modifier:Modifier,image:Int,title:String,subtitle:String,color:Color,onClick:()->Unit){var p by remember{mutableStateOf(false)};val s by animateFloatAsState(if(p).965f else 1f,label="lang");Card(modifier.scale(s).clickable{p=true;onClick();p=false},RoundedCornerShape(27.dp),colors=CardDefaults.cardColors(Color.White.copy(.94f)),elevation=CardDefaults.cardElevation(10.dp)){Column(modifier=Modifier.padding(5.dp),horizontalAlignment=Alignment.CenterHorizontally){Image(painter = painterResource(image),contentDescription = title,modifier=Modifier.fillMaxWidth().height(88.dp).clip(RoundedCornerShape(21.dp)),contentScale=ContentScale.Crop);Text(title,fontSize=21.sp,fontWeight=FontWeight.Black,color=Color(0xFF214C72));Text(subtitle,fontSize=8.sp,fontWeight=FontWeight.Bold,color=Color(0xFF35566F),textAlign=TextAlign.Center,maxLines=2);var q by remember{mutableStateOf(false)};val z by animateFloatAsState(if(q).94f else 1f,label="entry");Button(onClick={q=true;onClick();q=false},modifier=Modifier.fillMaxWidth(.74f).scale(z),shape=RoundedCornerShape(17.dp),colors=ButtonDefaults.buttonColors(containerColor=color),contentPadding=PaddingValues(vertical=3.dp),elevation=ButtonDefaults.buttonElevation(defaultElevation=6.dp,pressedElevation=1.dp)){Text("الدخول",color=Color.White,fontSize=12.sp,fontWeight=FontWeight.Black)}}}}
@Composable private fun Feature(icon:String,title:String,color:Color,modifier:Modifier,onClick:()->Unit){var p by remember{mutableStateOf(false)};val s by animateFloatAsState(if(p).92f else 1f,label="feature");Surface(modifier.scale(s).clickable{p=true;onClick();p=false},RoundedCornerShape(17.dp),color,shadowElevation=8.dp){Column(modifier=Modifier.padding(vertical=5.dp),horizontalAlignment=Alignment.CenterHorizontally){Text(icon,fontSize=22.sp);Text(title,fontSize=9.sp,fontWeight=FontWeight.Black,color=Color.White,textAlign=TextAlign.Center)}}}
@Composable private fun Icon3D(icon:String,title:String,color:Color,onClick:()->Unit){var p by remember{mutableStateOf(false)};val s by animateFloatAsState(if(p).9f else 1f,label="icon");Column(horizontalAlignment=Alignment.CenterHorizontally){Surface(Modifier.size(56.dp).scale(s).clickable{p=true;onClick();p=false},CircleShape,color,shadowElevation=10.dp){Box(Alignment.Center){Text(icon,fontSize=29.sp)}};Text(title,color=Color.White,fontSize=9.sp,fontWeight=FontWeight.Black)}}
@Composable private fun ProfileCard(childName:String,avatar:String,onClick:()->Unit){Surface(Modifier.widthIn(max=215.dp).clickable(onClick=onClick),RoundedCornerShape(23.dp),Color(0xFFFFF0D2).copy(.96f),shadowElevation=9.dp){Row(Modifier.padding(7.dp),verticalAlignment=Alignment.CenterVertically){val res=when(avatar){"boy"->R.drawable.student_boy_avatar;"girl"->R.drawable.student_girl_avatar;else->R.drawable.child_avatar};Image(painter = painterResource(res),contentDescription = "صورة الطفل",modifier=Modifier.size(44.dp),contentScale=ContentScale.Crop);Spacer(Modifier.width(6.dp));Column(horizontalAlignment=Alignment.End){Text(if(childName.isBlank())"مرحباً بك 🌟" else "مرحباً $childName",fontSize=15.sp,fontWeight=FontWeight.Black,color=Color(0xFF59331E));Text("اضغط لتعديل الملف ✎",fontSize=8.sp,color=Color(0xFF7D5737))}}}}
