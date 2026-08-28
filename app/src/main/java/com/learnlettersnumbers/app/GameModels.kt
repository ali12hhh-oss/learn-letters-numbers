package com.learnlettersnumbers.app

enum class GameLevel(val multiplier: Int, val label: String) {
    EASY(1, "سهل"),
    MEDIUM(2, "متوسط"),
    HARD(3, "متقدم")
}

enum class GameKind(val title: String) {
    LETTER("طابق الحرف"),
    NUMBER("صيد الأرقام"),
    MEMORY("ذاكرة الحروف"),
    ORDER("رتب الأرقام"),
    WORD("الكلمة المفقودة"),
    LISTEN("اسمع واختر"),
    SHAPE("شكل الحرف"),
    COUNT("عد الأشياء"),
    BUILD("سباق الكلمات"),
    MIXED("التحدي السريع")
}
