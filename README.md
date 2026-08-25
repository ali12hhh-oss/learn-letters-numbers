# تعلم الحروف والأرقام

مشروع Android/Jetpack Compose واحد موحد، أوفلاين في ميزاته التعليمية الأساسية.

## النسخة الموحدة
- versionName: `1.0.0`
- versionCode: `1`

## البيئة
- Android Gradle Plugin: `8.11.1`
- Kotlin: `2.2.20`
- Gradle: `8.13`
- compileSdk: `36`
- targetSdk: `36`
- Java: `17`

## GitHub Actions
يوجد البناء في `.github/workflows/android-build.yml` ويقوم بـ:
1. فحص إعداد Gradle.
2. Lint للإصدار Release.
3. بناء Debug APK.
4. بناء Release APK.
5. التحقق من وجود الملفات.
6. رفع APKs كـ Artifacts.

## التشغيل أوفلاين
لا توجد صلاحية INTERNET في Manifest. التخزين الأساسي للطفل والتقدم والإعدادات محلي. الصوت داخل التطبيق يعتمد على ملفات OGG محلية في `app/src/main/res/raw` ويُشغّل عبر `LocalAudioManager` فقط، ولا يوجد TTS كبديل. عند مغادرة الشاشة يتم إيقاف المشغل المحلي فوراً.

## الصوت البشري المرخص
مصدر التسجيلات البشرية المرشح محفوظ في `HUMAN_AUDIO_SOURCE.md`. لا يتم دمجها قبل التحقق من الملف والترخيص.
