package com.learnlettersnumbers.app

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
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

private data class Story(val title: String, val illustration: String, val text: String, val lesson: String = "")

private val arabicStories = listOf(
    Story("ليلى ونجمة المعرفة", "🌟", "في صباح جميل، جلست ليلى قرب نافذة غرفتها ومعها كتابها الملون. كانت تريد أن تتعلم حرفاً جديداً. قالت لنفسها: سأبدأ بحرف واحد فقط، ثم أتعلم شيئاً آخر. فتحت الكتاب ووجدت حرف ب، ثم بحثت عن كلمات تبدأ به. وجدت باباً وبطة وبالوناً. فرحت كثيراً، وكتبت الكلمات في دفترها. عندما أخطأت في كتابة إحدى الكلمات، لم تغضب، بل مسحتها وحاولت من جديد. وبعد وقت قصير شعرت أنها أصبحت أفضل. قالت أمها: التعلم لا يحتاج إلى السرعة، بل يحتاج إلى المحاولة والصبر. أغلقت ليلى الكتاب وهي سعيدة، ووعدت نفسها أن تتعلم كل يوم شيئاً صغيراً."),
    Story("سامر وحديقة الأرقام", "🌳", "ذهب سامر مع والده إلى الحديقة. رأى ثلاث زهرات حمراء وزهرتين صفراوين، فبدأ يعدها بصوت واضح. قال: واحد، اثنان، ثلاثة، أربعة، خمسة. ثم رأى أربع فراشات تطير فوق الزهور. حاول أن يجمع عدد الزهور والفراشات في ذهنه. ساعده والده وقال: إذا كان لدينا خمسة أشياء وأضفنا أربعة، كم يصبح العدد؟ فكر سامر قليلاً ثم قال: تسعة! ابتسم والده وقال: أحسنت، أنت تستخدم الرياضيات وأنت تلعب. في طريق العودة بدأ سامر يبحث عن الأرقام في السيارات واللافتات، وأصبح كل شيء حوله لعبة تعليمية ممتعة."),
    Story("القطة التي تعلمت الانتظار", "🐱", "كانت هناك قطة صغيرة اسمها لوزة تحب اللعب بسرعة. كلما رأت كرة قفزت إليها، وكلما رأت لعبة جديدة تركت لعبتها القديمة. في أحد الأيام حاولت تركيب لعبة تحتاج إلى خطوات مرتبة، لكنها لم تنجح. جلست قليلاً، ثم أخذت نفساً عميقاً وبدأت خطوة خطوة. اكتشفت أن القطعة الأولى يجب أن تأتي قبل الثانية. بعد دقائق اكتملت اللعبة. شعرت لوزة بالفخر لأنها لم تستسلم. قالت لها صديقتها العصفورة: أحياناً يكون أفضل طريق إلى النجاح هو أن نتوقف قليلاً ونفكر. ومن ذلك اليوم أصبحت لوزة أكثر هدوءاً وصبراً في اللعب والتعلم."),
    Story("رحلة التفاحة الحمراء", "🍎", "تدحرجت تفاحة حمراء من سلة المطبخ وخرجت إلى الحديقة. لم تكن تعرف أين ستصل. قابلت أولاً أرنباً صغيراً، فسألته عن الطريق إلى شجرة التفاح. قال الأرنب: اتبعي الطريق الذي تشرق عليه الشمس صباحاً. تابعت التفاحة رحلتها حتى قابلت عصفوراً، فأخبرها أن الأشجار كثيرة وأن عليها ألا تخاف من الطريق الطويل. في النهاية وصلت إلى شجرة كبيرة مليئة بالتفاح. أدركت أن الرحلة لم تكن مجرد انتقال من مكان إلى آخر، بل كانت فرصة للتعرف على أصدقاء جدد وتعلم أشياء لم تكن تعرفها. عادت إلى السلة مساءً وهي تحمل قصة جميلة."),
    Story("يوم بلا شاشة", "☀️", "استيقظ كريم في يوم مشمس وقرر أن يقضي ساعات الصباح بعيداً عن الأجهزة. خرج إلى الحديقة، ركل الكرة، وسقى النباتات، ثم جلس يرسم شجرة وبيتاً صغيراً. بعد ذلك ساعد أخته في ترتيب ألعابها. اكتشف أن الوقت يمر بسرعة عندما يكون مشغولاً بأشياء مفيدة وممتعة. عند الظهر قرأ قصة قصيرة، ثم نام قليلاً ليستريح. في المساء عاد إلى ألعابه الإلكترونية لفترة محدودة، لكنه شعر أن يومه كان أجمل لأنه جمع بين الراحة والحركة والقراءة واللعب. تعلم كريم أن الراحة ليست دائماً أمام الشاشة، وأن العقل والجسم يحتاجان إلى أنواع مختلفة من النشاط."),
    Story("فريق الأصدقاء", "🤝", "كان أربعة أصدقاء يريدون بناء برج من المكعبات. أراد كل واحد منهم أن يكون القائد، فبدأوا يختلفون. سألهم معلمهم: ماذا سيحدث إذا تعاونتم؟ اقترح أحدهم أن يختاروا شخصاً للترتيب، وآخر للبناء، وثالثاً للعد، ورابعاً للمراجعة. بدأ العمل من جديد. عندما سقط جزء من البرج، لم يلوم أحد صديقه، بل أعادوا البناء معاً. بعد دقائق أصبح البرج أطول وأجمل. فهم الأصدقاء أن العمل الجماعي يجعل المهمة أسهل، وأن الخطأ ليس سبباً للغضب بل فرصة للتعلم والتحسين."),
    Story("المظلة الزرقاء", "☔", "خرجت مريم من المدرسة وكانت تحمل مظلة زرقاء. بدأت الأمطار تنزل فجأة، فرأت طفلاً صغيراً يقف بلا مظلة. اقتربت منه وشاركته مظلتها حتى وصل إلى والده. شكرها الطفل كثيراً. في اليوم التالي وجدت مريم على مكتبها بطاقة صغيرة كتب عليها: شكراً لأنك جعلت يومي أفضل. احتفظت بالبطاقة داخل دفترها. تعلمت أن العمل الطيب قد يكون بسيطاً جداً، مثل مشاركة مظلة أو كلمة جميلة أو مساعدة شخص يحتاج إلينا. ومنذ ذلك اليوم أصبحت تبحث عن فرص صغيرة لفعل الخير كل يوم."),
    Story("سر الصندوق الخشبي", "📦", "وجد يوسف صندوقاً خشبياً قديماً في غرفة جده. كان الصندوق مغلقاً، وفوقه ورقة كتب عليها: لا تبحث عن الكنز قبل أن تبحث عن المعرفة. بدأ يوسف يقرأ التعليمات واحدة تلو الأخرى. كان عليه أن يحل مسألة أرقام، ثم يقرأ كلمة، ثم يجد حرفاً مخفياً في الورقة. بعد عدة محاولات وصل إلى المفتاح. فتح الصندوق فوجد مجموعة كتب ورسومات وأقلاماً ملونة. ضحك جده وقال: هذا هو الكنز الحقيقي. جلس يوسف يقرأ أحد الكتب، وفهم أن المعرفة يمكن أن تقودنا إلى أشياء أجمل من الذهب."),
    Story("رحلة إلى القمر", "🚀", "تخيلت نور أنها سافرت إلى القمر في مركبة صغيرة. جلست بجانب النافذة ورأت الأرض مثل كرة زرقاء جميلة. كانت تحمل معها دفتر ملاحظات، فبدأت تكتب الأرقام التي تراها على شاشة المركبة. سألها الحاسوب: كم خطوة تحتاجين للوصول إلى المحطة التالية؟ استخدمت نور الحساب ثم ضغطت الزر الصحيح. في الطريق سمعت صوتاً يقول: لا تخافي من السؤال، فكل سؤال بداية لاكتشاف جديد. وصلت إلى المحطة ورأت نجومًا كثيرة. عندما استيقظت من حلمها، فتحت دفترها وقالت: ربما لا أستطيع السفر إلى القمر اليوم، لكنني أستطيع أن أتعلم شيئاً يقربني من حلمي."),
    Story("النجاح يبدأ بمحاولة", "🏆", "كان آدم يتدرب على كتابة الحروف. في البداية كانت بعض الحروف كبيرة جداً وأخرى صغيرة جداً. حاول مرة، ثم مرة ثانية، لكنه لم يكن راضياً. شعر بالإحباط وأراد أن يتوقف. قالت له معلمته: لا تقارن محاولتك الأولى بالنتيجة الأخيرة، قارنها بمحاولتك السابقة فقط. أعاد آدم التدريب ببطء، وبدأ يلاحظ التحسن. بعد عدة أيام أصبح يكتب الحروف بشكل أوضح. لم يحصل على الجائزة لأنه كان مثالياً، بل لأنه استمر في المحاولة. فهم آدم أن النجاح ليس لحظة واحدة، وإنما مجموعة محاولات صغيرة تتراكم حتى نصبح أفضل.")
)

private val englishStories = listOf(
    Story("A Morning at School", "🏫", "Mia: Good morning, Sam! How are you today?\nSam: I am great, thank you. How about you?\nMia: I am happy because we have a new reading lesson.\nSam: That sounds fun. What are you reading?\nMia: I am reading a story about a little bird.\nSam: Can I read with you?\nMia: Of course! Let's sit together.\nThey sat near the window and read slowly. When Sam did not understand a word, Mia explained it. Their teacher smiled and said, 'Learning together makes everyone stronger.' The two friends continued reading and helped each other until the lesson was finished.", "Useful phrases: Good morning. How are you? Thank you. Of course. Let's learn together."),
    Story("The Friendly Shop", "🛒", "Ben: Hello! Can I help you?\nLina: Yes, please. I want two apples and one banana.\nBen: Here you are. Would you like anything else?\nLina: Yes. How much is the orange?\nBen: It is one dollar.\nLina: Great. I will take one orange, please.\nBen: Here you are. Have a nice day!\nLina: Thank you. You too!\nOn the way home, Lina repeated the new sentences. She learned that simple English can help her ask for things politely. She practiced the words again with her brother and felt more confident. The next time she visited the shop, she greeted the shopkeeper in English without being afraid.", "Useful phrases: Can I help you? I want... How much is it? Please. Thank you. Have a nice day."),
    Story("My New Friend", "🧒", "Tom: Hi! My name is Tom. What is your name?\nAli: Hi, I am Ali. Nice to meet you.\nTom: Nice to meet you too. What do you like?\nAli: I like football and drawing. What about you?\nTom: I like reading and riding my bike.\nAli: That is great. Would you like to play after school?\nTom: Yes, I would.\nThey discovered that they had different hobbies, but they enjoyed spending time together. They decided to teach each other one new word every day. Tom taught Ali a word about books, and Ali taught Tom a word about football. Soon they had a small notebook full of new English words.", "Useful phrases: Nice to meet you. What do you like? What about you? Would you like...?"),
    Story("A Rainy Day", "🌧️", "Emma: Look outside. It is raining!\nNoah: Yes, it is. We cannot play outside today.\nEmma: That is okay. We can read a book.\nNoah: Good idea. Which book should we choose?\nEmma: How about the animal book?\nNoah: I like animals. Let's read it together.\nThey sat on the sofa and read about lions, elephants, and birds. Every few minutes they stopped and practiced a new English sentence. The rain continued outside, but the children were happy. They learned that a quiet day can also be a good day for learning, reading, and resting.", "Useful phrases: Look outside. It is raining. That is okay. Good idea. Let's..."),
    Story("At the Playground", "🛝", "Sara: Do you want to play on the slide?\nOmar: Yes! But let's wait for our turn.\nSara: Okay. We can play with the ball while we wait.\nOmar: Great idea.\nAfter a few minutes, it was their turn. They played safely and helped a smaller child climb the steps. The teacher said, 'Please be careful and take turns.' Sara and Omar repeated the sentence together. At the end of the afternoon, they thanked their teacher and went home. They learned that simple English words can help us be polite, safe, and friendly.", "Useful phrases: Do you want to...? Let's wait. My turn. Your turn. Be careful."),
    Story("The Healthy Breakfast", "🍳", "Mom: Good morning! Are you ready for breakfast?\nLeo: Yes, I am. What do we have?\nMom: We have eggs, bread, milk, and fruit.\nLeo: I would like some fruit, please.\nMom: Which fruit do you want?\nLeo: I want an apple and a banana.\nMom: Here you are.\nLeo: Thank you!\nLeo ate his breakfast slowly and drank some water. His mother explained that healthy food gives the body energy for learning and playing. Before leaving for school, Leo practiced the sentences again. He learned that English can be part of everyday routines, not only a school subject.", "Useful phrases: What do we have? I would like... Which one? Here you are."),
    Story("The Lost Pencil", "✏️", "Nora: Excuse me, did you see my blue pencil?\nJack: I saw a pencil near the window. Is this yours?\nNora: Yes! Thank you so much.\nJack: You are welcome.\nNora: I was worried because I need it for my writing lesson.\nJack: No problem. We can look for things together.\nNora smiled and returned to her desk. She learned that asking politely is helpful when something is lost. Jack learned that a small act of kindness can make someone feel better. Before the lesson ended, they practiced the words 'Excuse me', 'Thank you', and 'You are welcome' together.", "Useful phrases: Excuse me. Did you see...? Is this yours? Thank you so much. You are welcome."),
    Story("The Little Robot", "🤖", "Maya: Look! I made a little robot.\nEvan: Wow! What can it do?\nMaya: It can move forward and turn left.\nEvan: Can it say hello?\nMaya: Yes. Hello, Evan!\nEvan: That is amazing. Can we teach it a new word?\nMaya: Sure. Let's teach it 'Good job!'\nThey repeated the phrase several times while programming the robot. When it finally said the words correctly, both children laughed. They discovered that learning English and learning technology could happen together. They also learned that mistakes are normal when we are building something new.", "Useful phrases: What can it do? Can it...? Let's teach it... Good job!"),
    Story("A Visit to the Library", "📚", "David: Hello. Where can I find books about animals?\nLibrarian: They are on the second shelf.\nDavid: Thank you. Can I borrow this book?\nLibrarian: Yes. Please bring it back next week.\nDavid: Okay. Thank you very much.\nDavid found a quiet place and began reading. He wrote three new English words in his notebook: animal, forest, and river. Before leaving, he asked the librarian about another book. He felt proud because he could use English to ask questions and understand simple instructions. He decided to visit the library every week.", "Useful phrases: Where can I find...? Can I...? Please. Thank you very much. Bring it back."),
    Story("The Kind Team", "🤝", "Teacher: Today we will work in teams.\nAva: What should we do first?\nTeacher: First, choose a leader. Then share the jobs.\nAva: I can draw the picture.\nMax: I can write the words.\nLily: I can check the numbers.\nTeacher: Excellent. Remember to help each other.\nThe children worked together. When one answer was wrong, nobody laughed. They said, 'Let's try again.' At the end, their poster was colorful and clear. The teacher congratulated them because they used English, shared ideas, and treated each other kindly. They learned that teamwork is not only about finishing a task; it is also about listening, helping, and encouraging others.", "Useful phrases: What should we do first? I can... Let's try again. Help each other. Excellent!"),
)

@Composable
fun StoriesScreen(audio: LocalAudioManager, onBack: () -> Unit) {
    var language by remember { mutableStateOf(0) }
    var index by remember { mutableStateOf(0) }
    val stories = if (language == 0) arabicStories else englishStories
    val story = stories[index]
    LaunchedEffect(language, index) {
        if (language == 0) audio.playRequired("stories_intro_ar") else audio.playRequired("stories_intro_en")
    }
    Column(Modifier.fillMaxSize().background(Brush.verticalGradient(listOf(MaterialTheme.colorScheme.background, MaterialTheme.colorScheme.surfaceVariant))).padding(14.dp)) {
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
            Button(onClick = onBack, shape = RoundedCornerShape(18.dp)) { Text("رجوع") }
            Column(horizontalAlignment = Alignment.End) {
                Text(if (language == 0) "القصص العربية" else "English Stories", fontSize = 28.sp, fontWeight = FontWeight.ExtraBold, color = Color(0xFF2357A6))
                Text(if (language == 0) "تعلم ومرح وراحة" else "Learn, relax and practice English", fontSize = 14.sp)
            }
        }
        Row(Modifier.fillMaxWidth().padding(vertical = 10.dp), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            Button(onClick = { language = 0; index = 0 }, modifier = Modifier.weight(1f), shape = RoundedCornerShape(20.dp), colors = ButtonDefaults.buttonColors(containerColor = if (language == 0) Color(0xFF4C8BF5) else MaterialTheme.colorScheme.surface, contentColor = if (language == 0) Color.White else Color(0xFF4C8BF5))) { Text("العربي") }
            Button(onClick = { language = 1; index = 0 }, modifier = Modifier.weight(1f), shape = RoundedCornerShape(20.dp), colors = ButtonDefaults.buttonColors(containerColor = if (language == 1) Color(0xFF6BCB77) else MaterialTheme.colorScheme.surface, contentColor = if (language == 1) Color.White else Color(0xFF3A9A49))) { Text("English") }
        }
        Card(Modifier.fillMaxWidth().weight(1f), shape = RoundedCornerShape(30.dp), colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface), elevation = CardDefaults.cardElevation(9.dp)) {
            LazyColumn(contentPadding = PaddingValues(bottom = 12.dp)) {
                item {
                    Box(Modifier.fillMaxWidth().height(155.dp).background(Brush.horizontalGradient(listOf(Color(0xFFBDEBFF), Color(0xFFFFE4A8))))) {
                        Text(story.illustration, fontSize = 88.sp, modifier = Modifier.align(Alignment.Center))
                    }
                    Text(story.title, Modifier.fillMaxWidth().padding(14.dp, 14.dp, 14.dp, 6.dp), fontSize = 27.sp, fontWeight = FontWeight.ExtraBold, color = Color(0xFF2357A6), textAlign = TextAlign.Center)
                    Text("قصة ${index + 1} من ${stories.size}", Modifier.fillMaxWidth(), textAlign = TextAlign.Center, color = Color.Gray)
                    Button(onClick = { audio.playRequired("story_%02d".format(if (language == 0) index + 1 else index + 11)) }, Modifier.fillMaxWidth().padding(14.dp).height(56.dp), shape = RoundedCornerShape(20.dp)) { Text("🔊 استمع إلى القصة", fontSize = 18.sp) }
                    Text(story.text, Modifier.fillMaxWidth().padding(horizontal = 18.dp), fontSize = 19.sp, lineHeight = 31.sp, textAlign = if (language == 0) TextAlign.Right else TextAlign.Left)
                    if (story.lesson.isNotBlank()) {
                        Spacer(Modifier.height(14.dp)); Card(Modifier.padding(14.dp), colors = CardDefaults.cardColors(containerColor = Color(0xFFEAF7EA))) { Text(story.lesson, Modifier.padding(14.dp), fontSize = 16.sp, lineHeight = 25.sp) }
                    }
                }
            }
        }
        Row(Modifier.fillMaxWidth().padding(top = 10.dp), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            OutlinedButton(onClick = { if (index > 0) index-- }, modifier = Modifier.weight(1f), shape = RoundedCornerShape(18.dp)) { Text("السابق") }
            Button(onClick = { index = (index + 1) % stories.size }, modifier = Modifier.weight(1f), shape = RoundedCornerShape(18.dp)) { Text("القصة التالية ➜") }
        }
    }
}
