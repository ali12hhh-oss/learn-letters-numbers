from pathlib import Path

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

# Material 3 imports / experimental APIs.
edit('EnglishLettersScreen.kt', lambda s: s.replace(
    'import androidx.compose.material3.Text\n',
    'import androidx.compose.material3.MaterialTheme\nimport androidx.compose.material3.Text\n'))

for name in ['GamesScreen.kt', 'SettingsScreen.kt', 'StagesScreen.kt', 'WritingTutorialScreen.kt', 'WritingStrokeLessonScreen.kt']:
    p = BASE / name
    s = p.read_text(encoding='utf-8')
    if not s.startswith('@file:OptIn'):
        s = '@file:OptIn(androidx.compose.material3.ExperimentalMaterial3Api::class)\n' + s
    p.write_text(s, encoding='utf-8')

# GamesScreen: Button's first positional parameter is onClick, so pass modifier by name.
edit('GamesScreen.kt', lambda s: s.replace(
    'Button(\n                    Modifier.fillMaxWidth().padding(vertical = 4.dp),',
    'Button(\n                    modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),'))

# HomeScreen: OpenDocument expects an array of MIME filters.
edit('HomeScreen.kt', lambda s: s.replace(
    'imagePicker.launch("image/*")',
    'imagePicker.launch(arrayOf("image/*"))'))

# MainActivity: match EnglishNumbers callback and make ModeButton accept its caller-provided modifier.
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
    s = s.replace(
        'ModeButton("1–9", "الآحاد", mode == "ones", Color(0xFF4C8BF5))',
        'ModeButton("1–9", "الآحاد", mode == "ones", Color(0xFF4C8BF5), Modifier.weight(1f))')
    s = s.replace(
        'ModeButton("10–100", "العشرات", mode == "tens", Color(0xFFFF8A4C))',
        'ModeButton("10–100", "العشرات", mode == "tens", Color(0xFFFF8A4C), Modifier.weight(1f))')
    s = s.replace(
        'fun ModeButton(en: String, ar: String, selected: Boolean, color: Color, onClick: () -> Unit) {',
        'fun ModeButton(en: String, ar: String, selected: Boolean, color: Color, modifier: Modifier, onClick: () -> Unit) {')
    s = s.replace(
        'Button(onClick = onClick, modifier = Modifier.weight(1f).height(64.dp)',
        'Button(onClick = onClick, modifier = modifier.height(64.dp)')
    return s
edit('MainActivity.kt', main_fix)

# NumbersScreen: remove invalid weight from standalone OperationCard.
edit('NumbersScreen.kt', lambda s: s.replace(
    'Modifier.fillMaxWidth().weight(1f).shadow(12.dp',
    'Modifier.fillMaxWidth().shadow(12.dp'))

# LettersScreen: avoid package-level LetterForm collision with ReadingScreen.
def letters_fix(s):
    s = s.replace('enum class LetterForm { INITIAL, MEDIAL, FINAL }', 'enum class ArabicLetterForm { INITIAL, MEDIAL, FINAL }')
    s = s.replace('mutableStateOf(LetterForm.INITIAL)', 'mutableStateOf(ArabicLetterForm.INITIAL)')
    s = s.replace('form == LetterForm.INITIAL', 'form == ArabicLetterForm.INITIAL')
    s = s.replace('form == LetterForm.MEDIAL', 'form == ArabicLetterForm.MEDIAL')
    s = s.replace('form == LetterForm.FINAL', 'form == ArabicLetterForm.FINAL')
    s = s.replace('form = LetterForm.INITIAL', 'form = ArabicLetterForm.INITIAL')
    s = s.replace('form = LetterForm.MEDIAL', 'form = ArabicLetterForm.MEDIAL')
    s = s.replace('form = LetterForm.FINAL', 'form = ArabicLetterForm.FINAL')
    s = s.replace('when(form) {\n                        LetterForm.INITIAL', 'when(form) {\n                        ArabicLetterForm.INITIAL')
    s = s.replace('LetterForm.MEDIAL ->', 'ArabicLetterForm.MEDIAL ->')
    s = s.replace('LetterForm.FINAL ->', 'ArabicLetterForm.FINAL ->')
    return s
edit('LettersScreen.kt', letters_fix)

# ReadingScreen: imports, duplicate enum, pointer consumption, explicit list typing.
def reading_fix(s):
    s = s.replace('import androidx.compose.material3.Text\n', 'import androidx.compose.material3.MaterialTheme\nimport androidx.compose.material3.Text\n')
    s = s.replace('import androidx.compose.ui.input.pointer.consume\n', 'import androidx.compose.ui.input.pointer.consumePositionChange\n')
    s = s.replace('private enum class LetterForm { INITIAL, MEDIAL, FINAL }', 'private enum class ReadingLetterForm { INITIAL, MEDIAL, FINAL }')
    s = s.replace('LetterForm.', 'ReadingLetterForm.')
    s = s.replace('change.consume();', 'change.consumePositionChange();')
    old = '''val names = word.map { ch -> readingArabicLetters.indexOf(ch).takeIf { it >= 0 }?.plus(1) }\n                    if (names.all { it != null }) audio.playSequence(names.map { "ar_letter_%02d_sound".format(it!!) })'''
    new = '''val ids: List<Int> = word.mapNotNull { ch ->\n                        readingArabicLetters.indexOf(ch).takeIf { it >= 0 }?.plus(1)\n                    }\n                    if (ids.size == word.length) {\n                        audio.playSequence(ids.map { id -> "ar_letter_%02d_sound".format(id) })\n                    }'''
    s = s.replace(old, new)
    old2 = '''val ids = word.map { arabicLetters.indexOf(it) + 1 }.filter { it > 0 }\n                                    audio.playSequence(ids.map { "ar_letter_%02d_sound".format(it) })'''
    new2 = '''val ids: List<Int> = word.mapNotNull { ch ->\n                                        arabicLetters.indexOf(ch).takeIf { it >= 0 }?.plus(1)\n                                    }\n                                    if (ids.size == word.length) {\n                                        audio.playSequence(ids.map { id -> "ar_letter_%02d_sound".format(id) })\n                                    }'''
    s = s.replace(old2, new2)
    return s
edit('ReadingScreen.kt', reading_fix)

# SettingsScreen: Card onClick overload with named modifier.
edit('SettingsScreen.kt', lambda s: s.replace(
    'Card(Modifier.fillMaxWidth(), shape = RoundedCornerShape(24.dp), elevation = CardDefaults.cardElevation(6.dp), onClick = onClick)',
    'Card(modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(24.dp), elevation = CardDefaults.cardElevation(6.dp), onClick = onClick)'))

# TestsScreen: put weight in caller RowScope, not inside helper.
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

# WritingStrokeLessonScreen: replace prohibited vararg Offset and fix positional Button modifiers.
def stroke_fix(s):
    # Only rewrite the helper signature; the lesson declarations are all stroke(...) calls.
    s = s.replace('private fun stroke(vararg p: Offset) = Stroke(p.toList())', 'private fun stroke(p: List<Offset>) = Stroke(p)')
    # Target all stroke(...) calls in the two lesson declarations.
    import re
    def repl(m):
        return 'stroke(listOf(' + m.group(1) + '))'
    s = re.sub(r'stroke\((Offset\([^\n]+?\)(?:,\s*Offset\([^\n]+?\))+?)\)', repl, s)
    s = s.replace('OutlinedButton(\n                    Modifier.weight(1f),', 'OutlinedButton(\n                    modifier = Modifier.weight(1f),')
    s = s.replace('Button(\n                    Modifier.weight(1f),', 'Button(\n                    modifier = Modifier.weight(1f),')
    s = s.replace('Modifier.fillMaxWidth().weight(1f).background(Color(0xFFF3F7FF))', 'Modifier.fillMaxWidth().height(220.dp).background(Color(0xFFF3F7FF))')
    return s
edit('WritingStrokeLessonScreen.kt', stroke_fix)

# WritingTutorialScreen: positional modifier arguments.
def tutorial_fix(s):
    s = s.replace('Button(\n                    Modifier.weight(1f),', 'Button(\n                    modifier = Modifier.weight(1f),')
    s = s.replace('OutlinedButton(\n                    Modifier.weight(1f),', 'OutlinedButton(\n                    modifier = Modifier.weight(1f),')
    return s
edit('WritingTutorialScreen.kt', tutorial_fix)

print('compile repair script completed')
