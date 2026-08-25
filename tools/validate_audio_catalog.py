from pathlib import Path
import sys

raw = Path("app/src/main/res/raw")
expected = []
for i in range(1, 29):
    n = f"{i:02d}"
    expected += [f"ar_letter_{n}_sound.ogg", f"ar_letter_{n}_name.ogg",
                 f"ar_letter_{n}_vowel_1.ogg", f"ar_letter_{n}_vowel_2.ogg", f"ar_letter_{n}_vowel_3.ogg"]
for i in range(1, 27):
    n = f"{i:02d}"
    expected += [f"en_letter_{n}_sound.ogg", f"en_letter_{n}_name.ogg"]
for i in range(1, 101):
    expected += [f"ar_number_{i:03d}.ogg", f"en_number_{i:03d}.ogg"]
expected += [
    "welcome_ar.ogg", "welcome_en.ogg", "letters_intro_ar.ogg", "letters_intro_en.ogg",
    "numbers_intro_ar.ogg", "numbers_intro_en.ogg", "reading_intro_ar.ogg", "reading_intro_en.ogg",
    "writing_intro_ar.ogg", "writing_intro_en.ogg", "quiz_intro_ar.ogg", "quiz_intro_en.ogg",
    "stories_intro_ar.ogg", "stories_intro_en.ogg", "games_intro_ar.ogg", "games_intro_en.ogg",
    "correct_ar.ogg", "correct_en.ogg", "wrong_ar.ogg", "wrong_en.ogg", "next_ar.ogg", "next_en.ogg",
    "back_ar.ogg", "back_en.ogg"
]
expected += [f"story_{i:02d}.ogg" for i in range(1, 21)]
expected += [f"praise_{lang}_{i:02d}.ogg" for lang in ("ar", "en") for i in range(1, 11)]

missing = []
bad = []
for name in expected:
    p = raw / name
    if not p.exists():
        missing.append(name)
        continue
    data = p.read_bytes()
    if len(data) < 100:
        bad.append((name, "empty/small"))
    elif not data.startswith(b"OggS"):
        bad.append((name, "not a valid OGG container header"))

print(f"Expected core audio: {len(expected)}")
print(f"Missing: {len(missing)}")
print(f"Bad: {len(bad)}")
if missing or bad:
    print("MISSING", missing)
    print("BAD", bad)
    sys.exit(1)
print("Audio catalog OK: all required local audio files are valid OGG containers")
