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


def dedupe_import(s, line):
    return s.replace(line + line, line)

# Remove accidental duplicate imports created by previous repair passes.
for name in ['EnglishLettersScreen.kt', 'ReadingScreen.kt']:
    edit(name, lambda s, line='import androidx.compose.material3.MaterialTheme\n': dedupe_import(s, line))

# English letters: keep exactly one MaterialTheme import.
edit('EnglishLettersScreen.kt', lambda s: s.replace(
    'import androidx.compose.material3.MaterialTheme\nimport androidx.compose.material3.MaterialTheme\n',
    'import androidx.compose.material3.MaterialTheme\n'))

# Material 3 opt-in where required.
for name in ['GamesScreen.kt', 'SettingsScreen.kt', 'StagesScreen.kt', 'WritingTutorialScreen.kt', 'WritingStrokeLessonScreen.kt']:
    p = BASE / name
    s = p.read_text(encoding='utf-8')
    if not s.startswith('@file:OptIn'):
        s = '@file:OptIn(androidx.compose.material3.ExperimentalMaterial3Api::class)\n' + s
        p.write_text(s, encoding='utf-8')

# GamesScreen: Button modifier must be named.
edit('GamesScreen.kt', lambda s: s.replace(
    'Button(\n                    Modifier.fillMaxWidth().padding(vertical = 4.dp),',
    'Button(\n                    modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),'))

# HomeScreen: OpenDocument takes MIME filters as an array.
edit('HomeScreen.kt', lambda s: s.replace(
    'imagePicker.launch("image/*")',
    'imagePicker.launch(arrayOf("image/*"))'))

# MainActivity: match EnglishNumbers callback and ModeButton signature.
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
    ]:
        s = s.replace(old, new)
    return s
edit('MainActivity.kt', main_fix)

# NumbersScreen: remove weight from standalone operation card.
edit('NumbersScreen.kt', lambda s: s.replace(
    'Modifier.fillMaxWidth().weight(1f).shadow(12.dp',
    'Modifier.fillMaxWidth().shadow(12.dp'))

# LettersScreen: use an isolated enum name and repair any accidental doubled prefix.
def letters_fix(s):
    s = s.replace('enum class LetterForm { INITIAL, MEDIAL, FINAL }', 'enum class ArabicLetterForm { INITIAL, MEDIAL, FINAL }')
    s = s.replace('ArabicArabicLetterForm', 'ArabicLetterForm')
    for old, new in [
        ('LetterForm.INITIAL', 'ArabicLetterForm.INITIAL'),
        ('LetterForm.MEDIAL', 'ArabicLetterForm.MEDIAL'),
        ('LetterForm.FINAL', 'ArabicLetterForm.FINAL'),
    ]:
        s = s.replace(old, new)
    return s
edit('LettersScreen.kt', letters_fix)

# ReadingScreen: use a private ReadingLetterForm consistently, with no doubled prefix.
def reading_fix(s):
    s = dedupe_import(s, 'import androidx.compose.material3.MaterialTheme\n')
    s = s.replace('import androidx.compose.ui.input.pointer.consume\n', 'import androidx.compose.ui.input.pointer.consumePositionChange\n')
    s = s.replace('private enum class LetterForm { INITIAL, MEDIAL, FINAL }', 'private enum class ReadingLetterForm { INITIAL, MEDIAL, FINAL }')
    s = s.replace('ReadingReadingLetterForm', 'ReadingLetterForm')
    s = s.replace('ReadingLetterForm.', 'ReadingLetterForm.')
    s = s.replace('LetterForm.', 'ReadingLetterForm.')
    s = s.replace('change.consume();', 'change.consumePositionChange()')
    s = s.replace('private fun displayTarget(mode: ReadingMode, index: Int, form: LetterForm): String', 'private fun displayTarget(mode: ReadingMode, index: Int, form: ReadingLetterForm): String')
    # Explicitly type state so Kotlin 2.2 can infer remember/mutableStateOf.
    s = s.replace('var form by remember { mutableStateOf(ReadingLetterForm.INITIAL) }', 'var form: ReadingLetterForm by remember { mutableStateOf(ReadingLetterForm.INITIAL) }')
    s = s.replace('val ids: List<Int> = word.mapNotNull { ch ->', 'val ids: List<Int> = word.mapNotNull { ch ->')
    return s
edit('ReadingScreen.kt', reading_fix)

# SettingsScreen: Card onClick overload with named modifier.
edit('SettingsScreen.kt', lambda s: s.replace(
    'Card(Modifier.fillMaxWidth(), shape = RoundedCornerShape(24.dp), elevation = CardDefaults.cardElevation(6.dp), onClick = onClick)',
    'Card(modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(24.dp), elevation = CardDefaults.cardElevation(6.dp), onClick = onClick)'))

# TestsScreen: weight belongs in the parent RowScope.
def tests_fix(s):
    replacements = {
        'TestModeButton("العربي", language == TestMode.ARABIC, Color(0xFF4C8BF5))': 'TestModeButton("العربي", language == TestMode.ARABIC, Color(0xFF4C8BF5), Modifier.weight(1f))',
        'TestModeButton("English", language == TestMode.ENGLISH, Color(0xFF6BCB77))': 'TestModeButton("English", language == TestMode.ENGLISH, Color(0xFF6BCB77), Modifier.weight(1f))',
        'TestModeButton(if (language == TestMode.ARABIC) "الحروف" else "Letters", kind == TestKind.LETTERS, Color(0xFF9B72E8))': 'TestModeButton(if (language == TestMode.ARABIC) "الحروف" else "Letters", kind == TestKind.LETTERS, Color(0xFF9B72E8), Modifier.weight(1f))',
        'TestModeButton(if (language == TestMode.ARABIC) "الأرقام" else "Numbers", kind == TestKind.NUMBERS, Color(0xFFFF8A4C))': 'TestModeButton(if (language == TestMode.ARABIC) "الأرقام" else "Numbers", kind == TestKind.NUMBERS, Color(0xFFFF8A4C), Modifier.weight(1f))',
        'private fun TestModeButton(text: String, selected: Boolean, color: Color, onClick: () -> Unit)': 'private fun TestModeButton(text: String, selected: Boolean, color: Color, modifier: Modifier, onClick: () -> Unit)',
        'modifier = Modifier.weight(1f).height(58.dp)': 'modifier = modifier.height(58.dp)'
    }
    for old, new in replacements.items():
        s = s.replace(old, new)
    return s
edit('TestsScreen.kt', tests_fix)

# WritingStrokeLessonScreen: use List<Offset> instead of prohibited vararg.
def stroke_fix(s):
    s = s.replace('private fun stroke(vararg p: Offset) = Stroke(p.toList())', 'private fun stroke(p: List<Offset>) = Stroke(p)')
    # Single point calls.
    s = re.sub(r'stroke\((Offset\([^()]*\))\)', r'stroke(listOf(\1))', s)
    # Multiple point calls, once, avoiding already-wrapped listOf calls.
    s = re.sub(r'stroke\(((?:Offset\([^()]*\),\s*)+Offset\([^()]*\))\)', r'stroke(listOf(\1))', s)
    s = s.replace('OutlinedButton(Modifier.weight(1f),', 'OutlinedButton(modifier = Modifier.weight(1f),')
    s = s.replace('Button(Modifier.weight(1f),', 'Button(modifier = Modifier.weight(1f),')
    s = s.replace('Modifier.fillMaxWidth().weight(1f).background(Color(0xFFF3F7FF))', 'Modifier.fillMaxWidth().height(220.dp).background(Color(0xFFF3F7FF))')
    return s
edit('WritingStrokeLessonScreen.kt', stroke_fix)

# WritingTutorialScreen: positional modifier arguments.
edit('WritingTutorialScreen.kt', lambda s: s.replace('Button(Modifier.weight(1f),', 'Button(modifier = Modifier.weight(1f),').replace('OutlinedButton(Modifier.weight(1f),', 'OutlinedButton(modifier = Modifier.weight(1f),'))

print('compile repair script completed')
