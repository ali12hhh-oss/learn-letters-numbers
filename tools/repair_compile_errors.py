from pathlib import Path
import re

BASE = Path('app/src/main/java/com/learnlettersnumbers/app')


def edit(name, fn):
    p = BASE / name
    s = p.read_text(encoding='utf-8')
    s2 = fn(s)
    if s2 != s:
        p.write_text(s2, encoding='utf-8')
        print('updated', p)
    else:
        print('unchanged', p)


def normalize_text(name, replacements):
    def fn(s):
        for old, new in replacements:
            s = s.replace(old, new)
        return s
    edit(name, fn)

normalize_text('EnglishLettersScreen.kt', [
    ('import androidx.compose.material3.MaterialTheme\nimport androidx.compose.material3.MaterialTheme\n',
     'import androidx.compose.material3.MaterialTheme\n'),
])
normalize_text('LettersScreen.kt', [('ArabicArabicLetterForm', 'ArabicLetterForm')])
normalize_text('ReadingScreen.kt', [
    ('ReadingReadingLetterForm', 'ReadingLetterForm'),
    ('import androidx.compose.material3.MaterialTheme\nimport androidx.compose.material3.MaterialTheme\n',
     'import androidx.compose.material3.MaterialTheme\n'),
])

edit('EnglishLettersScreen.kt', lambda s: s if 'import androidx.compose.material3.MaterialTheme\n' in s else s.replace(
    'import androidx.compose.material3.Text\n',
    'import androidx.compose.material3.MaterialTheme\nimport androidx.compose.material3.Text\n'))

for name in ['GamesScreen.kt', 'SettingsScreen.kt', 'StagesScreen.kt', 'WritingTutorialScreen.kt', 'WritingStrokeLessonScreen.kt']:
    p = BASE / name
    s = p.read_text(encoding='utf-8')
    if not s.startswith('@file:OptIn'):
        s = '@file:OptIn(androidx.compose.material3.ExperimentalMaterial3Api::class)\n' + s
    p.write_text(s, encoding='utf-8')

edit('GamesScreen.kt', lambda s: s.replace(
    'Button(\n                    Modifier.fillMaxWidth().padding(vertical = 4.dp),',
    'Button(\n                    modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),'))
edit('HomeScreen.kt', lambda s: s.replace(
    'imagePicker.launch("image/*")', 'imagePicker.launch(arrayOf("image/*"))'))

def main_fix(s):
    s = s.replace(
        'fun EnglishNumbers(onBack: () -> Unit, speak: (String) -> Unit, repo: ProgressRepository) {',
        'fun EnglishNumbers(onBack: () -> Unit, speak: (String) -> Unit, playNumber: (Int) -> Unit, repo: ProgressRepository) {')
    s = s.replace(
        'Button(onClick = { speak(numberName(selected)) }, shape = RoundedCornerShape(18.dp))',
        'Button(onClick = { playNumber(selected) }, shape = RoundedCornerShape(18.dp))')
    s = s.replace(
        'repo.recordNumberSeen(n); repo.recordLesson("English numbers", n.toString()); speak(numberName(n))',
        'repo.recordNumberSeen(n); repo.recordLesson("English numbers", n.toString()); playNumber(n)')
    for old, new in [
        ('ModeButton("1–9", "الآحاد", mode == "ones", Color(0xFF4C8BF5))', 'ModeButton("1–9", "الآحاد", mode == "ones", Color(0xFF4C8BF5), Modifier.weight(1f))'),
        ('ModeButton("10–100", "العشرات", mode == "tens", Color(0xFFFF8A4C))', 'ModeButton("10–100", "العشرات", mode == "tens", Color(0xFFFF8A4C), Modifier.weight(1f))'),
        ('ModeButton("Letters", "الحروف", mode == "letters", Color(0xFF4C8BF5))', 'ModeButton("Letters", "الحروف", mode == "letters", Color(0xFF4C8BF5), Modifier.weight(1f))'),
        ('ModeButton("Numbers", "الأرقام", mode == "numbers", Color(0xFFFF8A4C))', 'ModeButton("Numbers", "الأرقام", mode == "numbers", Color(0xFFFF8A4C), Modifier.weight(1f))'),
        ('fun ModeButton(en: String, ar: String, selected: Boolean, color: Color, onClick: () -> Unit) {', 'fun ModeButton(en: String, ar: String, selected: Boolean, color: Color, modifier: Modifier, onClick: () -> Unit) {'),
        ('Button(onClick = onClick, modifier = Modifier.weight(1f).height(64.dp)', 'Button(onClick = onClick, modifier = modifier.height(64.dp)')
    ]: s = s.replace(old, new)
    return s
edit('MainActivity.kt', main_fix)
edit('NumbersScreen.kt', lambda s: s.replace(
    'Modifier.fillMaxWidth().weight(1f).shadow(12.dp',
    'Modifier.fillMaxWidth().shadow(12.dp'))

def letters_fix(s):
    s = s.replace('enum class LetterForm { INITIAL, MEDIAL, FINAL }', 'enum class ArabicLetterForm { INITIAL, MEDIAL, FINAL }')
    s = s.replace('ArabicArabicLetterForm', 'ArabicLetterForm')
    s = s.replace('mutableStateOf(ArabicLetterForm.INITIAL)', 'mutableStateOf<ArabicLetterForm>(ArabicLetterForm.INITIAL)')
    for old, new in [
        ('mutableStateOf(LetterForm.INITIAL)', 'mutableStateOf<ArabicLetterForm>(ArabicLetterForm.INITIAL)'),
        ('form == LetterForm.INITIAL', 'form == ArabicLetterForm.INITIAL'),
        ('form == LetterForm.MEDIAL', 'form == ArabicLetterForm.MEDIAL'),
        ('form == LetterForm.FINAL', 'form == ArabicLetterForm.FINAL'),
        ('form = LetterForm.INITIAL', 'form = ArabicLetterForm.INITIAL'),
        ('form = LetterForm.MEDIAL', 'form = ArabicLetterForm.MEDIAL'),
        ('form = LetterForm.FINAL', 'form = ArabicLetterForm.FINAL'),
        ('LetterForm.INITIAL ->', 'ArabicLetterForm.INITIAL ->'),
        ('LetterForm.MEDIAL ->', 'ArabicLetterForm.MEDIAL ->'),
        ('LetterForm.FINAL ->', 'ArabicLetterForm.FINAL ->'),
    ]: s = s.replace(old, new)
    return s
edit('LettersScreen.kt', letters_fix)

def reading_fix(s):
    s = s.replace('ReadingReadingLetterForm', 'ReadingLetterForm')
    s = s.replace('private enum class LetterForm { INITIAL, MEDIAL, FINAL }', 'private enum class ReadingLetterForm { INITIAL, MEDIAL, FINAL }')
    s = s.replace('var form by remember { mutableStateOf(ReadingLetterForm.INITIAL) }', 'var form by remember { mutableStateOf<ReadingLetterForm>(ReadingLetterForm.INITIAL) }')
    s = s.replace('import androidx.compose.material3.Text\n', 'import androidx.compose.material3.MaterialTheme\nimport androidx.compose.material3.Text\n' if 'import androidx.compose.material3.MaterialTheme\n' not in s else 'import androidx.compose.material3.Text\n')
    s = s.replace('import androidx.compose.ui.input.pointer.consume\n', 'import androidx.compose.ui.input.pointer.consumePositionChange\n')
    s = s.replace('change.consume();', 'change.consumePositionChange();')
    s = s.replace('audio.playSequence(ids.map { id -> "ar_letter_%02d_sound".format(id) })', 'val clips: List<String> = ids.map { id: Int -> "ar_letter_%02d_sound".format(id) }\n                        audio.playSequence(clips)')
    s = s.replace('private fun displayTarget(mode: ReadingMode, index: Int, form: LetterForm): String', 'private fun displayTarget(mode: ReadingMode, index: Int, form: ReadingLetterForm): String')
    return s
edit('ReadingScreen.kt', reading_fix)
edit('SettingsScreen.kt', lambda s: s.replace(
    'Card(Modifier.fillMaxWidth(), shape = RoundedCornerShape(24.dp), elevation = CardDefaults.cardElevation(6.dp), onClick = onClick)',
    'Card(modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(24.dp), elevation = CardDefaults.cardElevation(6.dp), onClick = onClick)'))

def tests_fix(s):
    replacements = {
        'TestModeButton("العربي", language == TestMode.ARABIC, Color(0xFF4C8BF5))': 'TestModeButton("العربي", language == TestMode.ARABIC, Color(0xFF4C8BF5), Modifier.weight(1f))',
        'TestModeButton("English", language == TestMode.ENGLISH, Color(0xFF6BCB77))': 'TestModeButton("English", language == TestMode.ENGLISH, Color(0xFF6BCB77), Modifier.weight(1f))',
        'TestModeButton(if (language == TestMode.ARABIC) "الحروف" else "Letters", kind == TestKind.LETTERS, Color(0xFF9B72E8))': 'TestModeButton(if (language == TestMode.ARABIC) "الحروف" else "Letters", kind == TestKind.LETTERS, Color(0xFF9B72E8), Modifier.weight(1f))',
        'TestModeButton(if (language == TestMode.ARABIC) "الأرقام" else "Numbers", kind == TestKind.NUMBERS, Color(0xFFFF8A4C))': 'TestModeButton(if (language == TestMode.ARABIC) "الأرقام" else "Numbers", kind == TestKind.NUMBERS, Color(0xFFFF8A4C), Modifier.weight(1f))',
        'private fun TestModeButton(text: String, selected: Boolean, color: Color, onClick: () -> Unit)': 'private fun TestModeButton(text: String, selected: Boolean, color: Color, modifier: Modifier, onClick: () -> Unit)',
        'modifier = Modifier.weight(1f).height(58.dp)': 'modifier = modifier.height(58.dp)'}
    for old, new in replacements.items(): s = s.replace(old, new)
    return s
edit('TestsScreen.kt', tests_fix)

def stroke_fix(s):
    s = s.replace('private fun stroke(vararg p: Offset) = Stroke(p.toList())', 'private fun stroke(p: List<Offset>) = Stroke(p)')
    s = re.sub(r'stroke\((Offset\([^()]*\))\)', r'stroke(listOf(\1))', s)
    s = re.sub(r'stroke\(((?:Offset\([^()]*\),\s*)+Offset\([^()]*\))\)', r'stroke(listOf(\1))', s)
    s = s.replace('OutlinedButton(Modifier.weight(1f),', 'OutlinedButton(modifier = Modifier.weight(1f),')
    s = s.replace('Button(Modifier.weight(1f),', 'Button(modifier = Modifier.weight(1f),')
    s = s.replace('Modifier.fillMaxWidth().weight(1f).background(Color(0xFFF3F7FF))', 'Modifier.fillMaxWidth().height(220.dp).background(Color(0xFFF3F7FF))')
    return s
edit('WritingStrokeLessonScreen.kt', stroke_fix)
edit('WritingTutorialScreen.kt', lambda s: s.replace('Button(Modifier.weight(1f),', 'Button(modifier = Modifier.weight(1f),').replace('OutlinedButton(Modifier.weight(1f),', 'OutlinedButton(modifier = Modifier.weight(1f),'))

print('compile repair script completed')
