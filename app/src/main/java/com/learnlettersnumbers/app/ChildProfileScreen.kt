package com.learnlettersnumbers.app

import android.content.Context
import android.content.Intent
import android.graphics.BitmapFactory
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.DialogProperties

@Composable
fun ChildProfileScreen(repo: ProgressRepository, onBack: () -> Unit, onEdit: () -> Unit) {
    var snapshot by remember { mutableStateOf(repo.load()) }
    val name = ChildProfileRepository.loadName().ifBlank { snapshot.childName }
    val avatar = ChildProfileRepository.loadAvatar()
    LaunchedEffect(Unit) { snapshot = repo.load() }
    CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Rtl) {
        Column(Modifier.fillMaxSize().background(Brush.verticalGradient(listOf(Color(0xFFBCEBFF), Color(0xFFFFE8B8)))).padding(14.dp)) {
            Row(Modifier.fillMaxWidth(), verticalAlignment=Alignment.CenterVertically) {
                Button(onClick=onBack, shape=RoundedCornerShape(18.dp)) { Text("رجوع") }
                Spacer(Modifier.weight(1f)); Text("ملف الطفل",fontSize=28.sp,fontWeight=FontWeight.Black,color=Color(0xFF245B8A))
            }
            Spacer(Modifier.height(12.dp))
            Card(Modifier.fillMaxWidth(),shape=RoundedCornerShape(28.dp),colors=CardDefaults.cardColors(containerColor=Color.White.copy(.95f)),elevation=CardDefaults.cardElevation(9.dp)) {
                Column(Modifier.fillMaxWidth().padding(18.dp),horizontalAlignment=Alignment.CenterHorizontally) {
                    StoredAvatar(avatar,Modifier.size(120.dp)); Spacer(Modifier.height(8.dp))
                    Text(if(name.isBlank())"الطفل" else name,fontSize=27.sp,fontWeight=FontWeight.Black,color=Color(0xFF315271))
                    Text("⭐ ${snapshot.stars} نجمة",fontSize=18.sp,fontWeight=FontWeight.Bold,color=Color(0xFF9A6500))
                    Spacer(Modifier.height(10.dp)); Button(onClick=onEdit,shape=RoundedCornerShape(16.dp)){Text("تعديل الاسم أو الصورة ✎")}
                }
            }
            Spacer(Modifier.height(14.dp)); Text("🛍️ مقتنياتي",fontSize=24.sp,fontWeight=FontWeight.Black,color=Color(0xFF245B8A)); Spacer(Modifier.height(7.dp))
            val owned = repo.ownedRewards().toList().sorted()
            if(owned.isEmpty() && snapshot.earnedTitles.isEmpty()) {
                Card(Modifier.fillMaxWidth(),shape=RoundedCornerShape(20.dp)){Text("لا توجد مقتنيات بعد. اجمع النجوم واشترِ مكافآتك من المتجر ⭐",Modifier.fillMaxWidth().padding(18.dp),textAlign=TextAlign.Center,fontWeight=FontWeight.Bold)}
            } else {
                LazyVerticalGrid(columns=GridCells.Fixed(2),modifier=Modifier.fillMaxWidth().weight(1f),verticalArrangement=Arrangement.spacedBy(9.dp),horizontalArrangement=Arrangement.spacedBy(9.dp),contentPadding=PaddingValues(bottom=10.dp)) {
                    items(owned){id->OwnedItemCard(id)}
                    items(snapshot.earnedTitles){title->Card(Modifier.fillMaxWidth(),shape=RoundedCornerShape(18.dp),colors=CardDefaults.cardColors(containerColor=Color(0xFFFFF0B8)),elevation=CardDefaults.cardElevation(5.dp)){Column(Modifier.fillMaxWidth().padding(10.dp),horizontalAlignment=Alignment.CenterHorizontally){Text("🏅",fontSize=30.sp);Text(title,fontWeight=FontWeight.Black,textAlign=TextAlign.Center);Text("لقب مملوك ✓",fontSize=12.sp,color=Color(0xFF6B4A00))}}}
                }
            }
        }
    }
}

@Composable private fun OwnedItemCard(id:String){
    val emoji=when(id){"star_badge"->"⭐";"rainbow","rainbow2"->"🌈";"rocket","rocket2"->"🚀";"trophy"->"🏆";"medal"->"🏅";"crown"->"👑";"sun"->"☀️";"heart"->"❤️";"balloon"->"🎈";"book"->"📚";"pencil"->"✏️";"sparkles"->"✨";"gift"->"🎁";"butterfly"->"🦋";else->"🎁"}
    val title=when(id){"star_badge"->"وسام النجمة";"rainbow"->"قوس قزح";"rocket"->"صاروخ التعلم";"trophy"->"كأس بطل التعلم";"medal"->"ميدالية التفوق";"crown"->"تاج صغير";"sun"->"شمس مشرقة";"heart"->"قلب الفرح";"balloon"->"بالون المرح";"rocket2"->"مركبة فضائية";"book"->"كتاب المعرفة";"pencil"->"قلم المبدع";"sparkles"->"نجوم لامعة";"gift"->"هدية مفاجأة";"butterfly"->"فراشة جميلة";"rainbow2"->"ألوان الفرح";else->id}
    Card(Modifier.fillMaxWidth(),shape=RoundedCornerShape(18.dp),colors=CardDefaults.cardColors(containerColor=Color(0xFFE4F5E5)),elevation=CardDefaults.cardElevation(5.dp)){Column(Modifier.fillMaxWidth().padding(10.dp),horizontalAlignment=Alignment.CenterHorizontally){Text(emoji,fontSize=32.sp);Text(title,fontWeight=FontWeight.Black,textAlign=TextAlign.Center);Text("مملوك ✓",fontSize=12.sp,color=Color(0xFF2E7D32))}}
}

@Composable
fun ProfileEditorDialogV2(onSaved:()->Unit,onCancel:()->Unit){
    val context=LocalContext.current
    var name by remember{mutableStateOf(ChildProfileRepository.loadName())}
    var avatar by remember{mutableStateOf(ChildProfileRepository.loadAvatar())}
    val launcher=rememberLauncherForActivityResult(ActivityResultContracts.OpenDocument()){uri:Uri?->if(uri!=null){try{context.contentResolver.takePersistableUriPermission(uri,Intent.FLAG_GRANT_READ_URI_PERMISSION)}catch(_:Exception){};avatar=uri.toString();ChildProfileRepository.saveAvatar(avatar)}}
    AlertDialog(onDismissRequest=onCancel,title={Text("تعديل ملف الطفل",fontWeight=FontWeight.Black)},text={Column(horizontalAlignment=Alignment.CenterHorizontally,modifier=Modifier.fillMaxWidth()){
        StoredAvatar(avatar,Modifier.size(100.dp));Spacer(Modifier.height(10.dp));Row(Modifier.fillMaxWidth(),horizontalArrangement=Arrangement.spacedBy(8.dp)){Button(onClick={avatar="boy";ChildProfileRepository.saveAvatar(avatar)},Modifier.weight(1f)){Text("👦 ولد")};Button(onClick={avatar="girl";ChildProfileRepository.saveAvatar(avatar)},Modifier.weight(1f)){Text("👧 بنت")}};Spacer(Modifier.height(8.dp));OutlinedButton(onClick={launcher.launch(arrayOf("image/*"))},modifier=Modifier.fillMaxWidth()){Text("📷 اختيار صورة")};Spacer(Modifier.height(8.dp));OutlinedTextField(value=name,onValueChange={name=it},singleLine=true,label={Text("اسم الطفل")},modifier=Modifier.fillMaxWidth())
    }},confirmButton={Button(onClick={ChildProfileRepository.saveName(name.trim());ProgressRepository(context).setChildName(name.trim());onSaved()}){Text("حفظ")}},dismissButton={TextButton(onClick=onCancel){Text("إلغاء")}},properties=DialogProperties(dismissOnBackPress=true,dismissOnClickOutside=false))
}

@Composable private fun StoredAvatar(value:String,modifier:Modifier){
    val context=LocalContext.current
    val bitmap=remember(value){loadBitmap(context,value)}
    Surface(modifier.clip(CircleShape),shape=CircleShape,color=Color.White,shadowElevation=9.dp){Box(Modifier.fillMaxSize(),contentAlignment=Alignment.Center){if(bitmap!=null)Image(bitmap.asImageBitmap(),contentDescription="صورة الطفل",modifier=Modifier.fillMaxSize(),contentScale=ContentScale.Crop)else Text(if(value=="girl")"👧🏻" else "👦🏻",fontSize=54.sp)}}
}
private fun loadBitmap(context:Context,value:String)=if(value.startsWith("content://"))try{context.contentResolver.openInputStream(Uri.parse(value)).use{BitmapFactory.decodeStream(it)}}catch(_:Exception){null}else null
