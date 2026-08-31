package com.learnlettersnumbers.app

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.material3.Text
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
internal fun OperationButton(text: String, selected: Boolean, modifier: Modifier, color: Color, onClick: () -> Unit) {
    val scale by animateFloatAsState(if (selected) 1.04f else 1f, spring(), label = text)
    Box(modifier.height(56.dp).scale(scale).shadow(if (selected) 9.dp else 4.dp, RoundedCornerShape(18.dp)).background(if (selected) color else MaterialTheme.colorScheme.surface, RoundedCornerShape(18.dp)).border(2.dp, color, RoundedCornerShape(18.dp)).clickable(onClick=onClick), contentAlignment=Alignment.Center) {
        Text(text, fontSize=20.sp, fontWeight=FontWeight.Black, color=if(selected) Color.White else color)
    }
}

@Composable
internal fun Number3DButton(text: String, color: Color, modifier: Modifier, onClick: () -> Unit) {
    var pressed by remember { mutableStateOf(false) }
    val scale by animateFloatAsState(if (pressed) .94f else 1f, spring(dampingRatio=.55f, stiffness=650f), label=text)
    Box(modifier.height(52.dp).scale(scale).shadow(8.dp,RoundedCornerShape(18.dp)).background(Brush.verticalGradient(listOf(color.copy(.92f),color.copy(.68f))),RoundedCornerShape(18.dp)).border(2.dp,Color.White.copy(.7f),RoundedCornerShape(18.dp)).clickable{pressed=true;onClick();pressed=false}.padding(horizontal=16.dp,vertical=6.dp),contentAlignment=Alignment.Center) {
        Text(text,fontSize=18.sp,fontWeight=FontWeight.Black,color=Color.White)
    }
}

internal fun arabicDigits(n:Int):String=n.toString().map{c->if(c in '0'..'9')('٠'.code+(c-'0')).toChar() else c}.joinToString("")
internal fun numberWords(n:Int):String{val ones=listOf("صفر","واحد","اثنان","ثلاثة","أربعة","خمسة","ستة","سبعة","ثمانية","تسعة");val teens=listOf("عشرة","أحد عشر","اثنا عشر","ثلاثة عشر","أربعة عشر","خمسة عشر","ستة عشر","سبعة عشر","ثمانية عشر","تسعة عشر");val tens=listOf("","","عشرون","ثلاثون","أربعون","خمسون","ستون","سبعون","ثمانون","تسعون");return when{n<10->ones[n];n<20->teens[n-10];n==100->"مئة";n%10==0->tens[n/10];else->"${ones[n%10]} و${tens[n/10]}"}}
internal fun numberColor(n:Int):Color=when(n%6){0->Color(0xFF7E57C2);1->Color(0xFF039BE5);2->Color(0xFF43A047);3->Color(0xFFFF8F00);4->Color(0xFFEC407A);else->Color(0xFF00897B)}
