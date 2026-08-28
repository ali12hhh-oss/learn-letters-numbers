package com.learnlettersnumbers.app

enum class GameLevel(val multiplier: Int, val label: String) {
    EASY(1, "سهل"),
    MEDIUM(2, "متوسط"),
    HARD(3, "متقدم")
}

enum class GameKind(val title: String) {
    MATCH_LETTER("طابق الحرف"),
    CATCH_NUMBER("صيد الأرقام"),
    MEMORY("ذاكرة الحروف"),
    ORDER_NUMBERS("رتب الأرقام"),
    MISSING_LETTER("الكلمة المفقودة"),
    LISTEN_CHOOSE("اسمع واختر"),
    LETTER_SHAPE("شكل الحرف"),
    COUNT_OBJECTS("عد الأشياء"),
    WORD_RACE("سباق الكلمات"),
    QUICK_CHALLENGE("التحدي السريع")
}
