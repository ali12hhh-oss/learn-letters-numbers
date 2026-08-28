package com.learnlettersnumbers.app

import android.content.Intent
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.core.animateFloatAsState
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
import androidx.compose.ui.platform.LocalContext
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
    val galleryLauncher=rememberLauncherForActivityResult(ActivityResultContracts.OpenDocument()){uri: Uri? ->
        if(uri!=null){
            try{context.contentResolver.takePersistableUriPermission(uri,Intent.FLAG_GRANT_READ_URI_PERMISSION)}catch(_:SecurityException){}
            avatar=uri.toString();ChildProfileRepository.saveAvatar(avatar)
        }
    }
    LaunchedEffect(Unit){speak("أهلاً بك في تطبيق تعلم الحروف والأرقام!")}
    CompositionLocalProvider(androidx.compose.ui.platform.LocalLayoutDirection provides LayoutDirection.Rtl){
        Box(Modifier.fillMaxSize().background(Brush.verticalGradient(listOf(Color(0xFF7ED6F7),Color(0xFFB9E8C7),Color(0xFFFFE6A8))))){
            Column(Modifier.fillMaxSize().statusBarsPadding().navigationBarsPadding().padding(horizontal=12.dp,vertical=8.dp),horizontalAlignment=Alignment.CenterHorizontally){
                Row(Modifier.fillMaxWidth(),Arrangement.SpaceBetween,Alignment.Top){ProfileCard(childName,avatar){showProfile=true};Icon3D("⚙","الإعدادات",Color(0xFF6843C6),onSettings)}
                Spacer(Modifier.height(10.dp));TitleBanner();Spacer(Modifier.height(10.dp))
                Row(Modifier.fillMaxWidth(),Arrangement.spacedBy(9.dp),Alignment.CenterVertically){
                    LangCard(Modifier.weight(1f),"العربية","تعلّم الحروف والأرقام والقراءة والقواعد",Color(0xFFF39A12),"عربي",onArabic)
                    LangCard(Modifier.weight(1f),"الإنجليزية","تعلّم الحروف والأرقام والكتابة",Color(0xFF1979D4),"A B C",onEnglish)
                }
                Spacer(Modifier.height(9.dp))
                Text("اختر ما تريد أن تتعلمه",Modifier.background(Color(0xFF285E7F).copy(alpha=.92f),RoundedCornerShape(18.dp)).padding(horizontal=16.dp,vertical=6.dp),color=Color.White,fontSize=15.sp,fontWeight=FontWeight.Black)
                Spacer(Modifier.height(6.dp))
                Row(Modifier.fillMaxWidth(),Arrangement.spacedBy(6.dp)){
                    Feature("🏆","المراحل",Color(0xFF6D35C9),Modifier.weight(1f),onStages);Feature("🎮","الألعاب",Color(0xFF49AA31),Modifier.weight(1f),onGames);Feature("📖","القصص",Color(0xFFD83B72),Modifier.weight(1f),onStories);Feature("📝","الاختبارات",Color(0xFFE98B16),Modifier.weight(1f),onTests)
                }
                Spacer(Modifier.height(6.dp))
                Row(Modifier.fillMaxWidth(),Arrangement.spacedBy(6.dp)){
                    Feature("🎁","المكافآت",Color(0xFF197BD7),Modifier.weight(1f),onRewards);Feature("📊","تقدمي",Color(0xFF00AFAF),Modifier.weight(1f),onProgress);Feature("⭐","نجومي",Color(0xFF8A55C7),Modifier.weight(1f),onRewards);Feature("🌱","واصل التعلم",Color(0xFF3C9A62),Modifier.weight(1f),onProgress)
                }
                Spacer(Modifier.weight(1f))
                Text("أحسنت! ⭐ استمر هكذا لتصبح الأفضل",Modifier.fillMaxWidth(.86f).background(Color(0xFFFFF0B8),RoundedCornerShape(20.dp)).padding(8.dp),color=Color(0xFFB53A2C),fontSize=14.sp,fontWeight=FontWeight.Black,textAlign=TextAlign.Center)
            }
        }
    }
    var draft by remember(childName){mutableStateOf(childName)}
    if(showProfile){
        ProfileEditorDialog(name=draft,avatar=avatar,onNameChange={draft=it},onBoy={avatar="boy";ChildProfileRepository.saveAvatar("boy")},onGirl={avatar="girl";ChildProfileRepository.saveAvatar("girl")},onGallery={galleryLauncher.launch(arrayOf("image/*"))},onSave={val n=draft.trim();ChildProfileRepository.saveName(n);ProgressRepository(context).setChildName(n);childName=n;ChildProfileRepository.markPromptSeen();showProfile=false},onSkip={ChildProfileRepository.markPromptSeen();showProfile=false},canDismiss=childName.isNotBlank())
    }
}

@Composable private fun ProfileEditorDialog(name:String,avatar:String,onNameChange:(String)->Unit,onBoy:()->Unit,onGirl:()->Unit,onGallery:()->Unit,onSave:()->Unit,onSkip:()->Unit,canDismiss:Boolean){
    AlertDialog(onDismissRequest={if(canDismiss)onSkip()},title={Text("ملف الطفل",fontWeight=FontWeight.Black)},text={
        Column(horizontalAlignment=Alignment.CenterHorizontally,modifier=Modifier.fillMaxWidth()){
            Text("اختر شخصية لطفلك أو صورة من الاستوديو",color=Color(0xFF35566F),fontSize=13.sp,fontWeight=FontWeight.Bold,textAlign=TextAlign.Center)
            Spacer(Modifier.height(10.dp));ProfileAvatarPreview(avatar);Spacer(Modifier.height(12.dp))
            Row(Modifier.fillMaxWidth(),horizontalArrangement=Arrangement.spacedBy(8.dp)){
                AvatarChoice("ولد","boy",avatar,Color(0xFF2B82D7),onBoy,Modifier.weight(1f));AvatarChoice("بنت","girl",avatar,Color(0xFFE65A9A),onGirl,Modifier.weight(1f))
            }
            Spacer(Modifier.height(8.dp));OutlinedButton(onClick=onGallery,modifier=Modifier.fillMaxWidth(),shape=RoundedCornerShape(16.dp)){Text("اختيار صورة من الاستوديو 📷",fontWeight=FontWeight.Bold)}
            Spacer(Modifier.height(10.dp));OutlinedTextField(value=name,onValueChange=onNameChange,label={Text("اسم الطفل")},singleLine=true,modifier=Modifier.fillMaxWidth())
        }
    },confirmButton={Button(onClick=onSave,shape=RoundedCornerShape(14.dp)){Text("حفظ الملف")}},dismissButton={TextButton(onClick=onSkip){Text("تخطي")}},properties=DialogProperties(dismissOnBackPress=canDismiss,dismissOnClickOutside=canDismiss))
}

@Composable private fun AvatarChoice(title:String,kind:String,current:String,color:Color,onClick:()->Unit,modifier:Modifier){
    val selected=current==kind
    Surface(modifier=modifier.clickable(onClick=onClick),shape=RoundedCornerShape(18.dp),color=if(selected)color.copy(alpha=.18f) else Color.Transparent,border=if(selected)androidx.compose.foundation.BorderStroke(2.dp,color) else null){
        Column(Modifier.padding(6.dp),horizontalAlignment=Alignment.CenterHorizontally){ProfileEmoji(kind,Modifier.size(58.dp));Text(title,fontSize=11.sp,fontWeight=FontWeight.Black,color=color)}
    }
}

@Composable private fun ProfileAvatarPreview(avatar:String){Surface(Modifier.size(104.dp),CircleShape,color=Color.White,shadowElevation=10.dp){Box(Modifier.fillMaxSize(),contentAlignment=Alignment.Center){ProfileEmoji(if(avatar=="girl")"girl" else "boy",Modifier.size(82.dp))}}}

@Composable private fun ProfileEmoji(kind:String,modifier:Modifier){Box(modifier.clip(CircleShape),contentAlignment=Alignment.Center){Text(if(kind=="girl")"👧🏻" else "👦🏻",fontSize=48.sp)}}

@Composable private fun TitleBanner(){Surface(Modifier.fillMaxWidth(.86f),RoundedCornerShape(28.dp),color=Color.White.copy(alpha=.94f),shadowElevation=10.dp){Column(Modifier.padding(horizontal=12.dp,vertical=9.dp),horizontalAlignment=Alignment.CenterHorizontally){Text("تعلم الحروف والأرقام",fontSize=25.sp,fontWeight=FontWeight.Black,color=Color(0xFFEE8B00),textAlign=TextAlign.Center);Text("تعلّم • العب • اكتب • أنجز ⭐",fontSize=13.sp,fontWeight=FontWeight.Bold,color=Color(0xFF315271))}}}

@Composable private fun LangCard(modifier:Modifier,title:String,subtitle:String,color:Color,icon:String,onClick:()->Unit){var pressed by remember{mutableStateOf(false)};val scale by animateFloatAsState(if(pressed).96f else 1f,label="language-card");Card(modifier=modifier.scale(scale).clickable{pressed=true;onClick();pressed=false},shape=RoundedCornerShape(28.dp),colors=CardDefaults.cardColors(containerColor=Color.White.copy(alpha=.95f)),elevation=CardDefaults.cardElevation(defaultElevation=11.dp)){Column(Modifier.padding(9.dp),horizontalAlignment=Alignment.CenterHorizontally){Surface(Modifier.size(62.dp),CircleShape,color=color,shadowElevation=7.dp){Box(Modifier.fillMaxSize(),contentAlignment=Alignment.Center){Text(icon,fontSize=24.sp,fontWeight=FontWeight.Black,color=Color.White)}};Spacer(Modifier.height(4.dp));Text(title,fontSize=21.sp,fontWeight=FontWeight.Black,color=Color(0xFF214C72),textAlign=TextAlign.Center);Text(subtitle,fontSize=10.sp,fontWeight=FontWeight.Bold,color=Color(0xFF35566F),textAlign=TextAlign.Center,minLines=2,maxLines=2);Spacer(Modifier.height(5.dp));var p by remember{mutableStateOf(false)};val z by animateFloatAsState(if(p).93f else 1f,label="entry");Button(onClick={p=true;onClick();p=false},modifier=Modifier.fillMaxWidth(.76f).scale(z),shape=RoundedCornerShape(17.dp),colors=ButtonDefaults.buttonColors(containerColor=color),contentPadding=PaddingValues(vertical=5.dp),elevation=ButtonDefaults.buttonElevation(defaultElevation=7.dp,pressedElevation=1.dp)){Text("ابدأ التعلم",color=Color.White,fontSize=12.sp,fontWeight=FontWeight.Black)}}}}

@Composable private fun Feature(icon:String,title:String,color:Color,modifier:Modifier,onClick:()->Unit){var pressed by remember{mutableStateOf(false)};val scale by animateFloatAsState(if(pressed).92f else 1f,label="feature");Surface(modifier.scale(scale).clickable{pressed=true;onClick();pressed=false},RoundedCornerShape(18.dp),color=color,shadowElevation=8.dp){Column(Modifier.padding(vertical=7.dp,horizontal=2.dp),horizontalAlignment=Alignment.CenterHorizontally){Text(icon,fontSize=22.sp);Text(title,fontSize=9.sp,fontWeight=FontWeight.Black,color=Color.White,textAlign=TextAlign.Center,maxLines=1)}}}

@Composable private fun Icon3D(icon:String,title:String,color:Color,onClick:()->Unit){var pressed by remember{mutableStateOf(false)};val scale by animateFloatAsState(if(pressed).9f else 1f,label="icon");Column(horizontalAlignment=Alignment.CenterHorizontally){Surface(Modifier.size(56.dp).scale(scale).clickable{pressed=true;onClick();pressed=false},CircleShape,color=color,shadowElevation=10.dp){Box(Modifier.fillMaxSize(),contentAlignment=Alignment.Center){Text(icon,fontSize=29.sp)}};Text(title,color=Color(0xFF234B63),fontSize=9.sp,fontWeight=FontWeight.Black)}}

@Composable private fun ProfileCard(childName:String,avatar:String,onClick:()->Unit){Surface(Modifier.widthIn(max=215.dp).clickable(onClick=onClick),RoundedCornerShape(23.dp),color=Color(0xFFFFF0D2).copy(alpha=.97f),shadowElevation=9.dp){Row(Modifier.padding(7.dp),verticalAlignment=Alignment.CenterVertically){ProfileEmoji(if(avatar=="girl")"girl" else "boy",Modifier.size(52.dp));Spacer(Modifier.width(8.dp));Column(horizontalAlignment=Alignment.End){Text(if(childName.isBlank())"مرحباً بك 🌟" else "مرحباً $childName",fontSize=15.sp,fontWeight=FontWeight.Black,color=Color(0xFF59331E));Text("اضغط لتعديل الملف ✎",fontSize=8.sp,color=Color(0xFF7D5737))}}}}
