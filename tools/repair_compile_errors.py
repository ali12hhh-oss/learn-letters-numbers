from pathlib import Path

p = Path('app/src/main/java/com/learnlettersnumbers/app/NumbersScreen.kt')
s = p.read_text(encoding='utf-8')
old = '@Composable private fun PracticeCard(example:OperationExample,answer:Int?,answered:Boolean,onSpeak:()->Unit,onAnswer:(Int)->Unit,onNext:()->Unit)'
new = '@Composable private fun ColumnScope.PracticeCard(example:OperationExample,answer:Int?,answered:Boolean,onSpeak:()->Unit,onAnswer:(Int)->Unit,onNext:()->Unit)'
if old in s:
    s = s.replace(old, new, 1)
    p.write_text(s, encoding='utf-8')
