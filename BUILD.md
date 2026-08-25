# بناء المشروع

المشروع Android/Jetpack Compose واحد، وليس عدة إصدارات.

- Java: 17
- Android Gradle Plugin: 8.11.1
- Gradle: 8.13
- Kotlin: 2.2.20
- compileSdk / targetSdk: 36
- minSdk: 24
- versionName: 1.0.0
- versionCode: 1

لـ GitHub Actions يوجد workflow في `.github/workflows/android-build.yml` يبني Debug وRelease ويتحقق من وجود ملفات APK.

لا تضع `local.properties` أو مفاتيح التوقيع داخل GitHub. قبل نشر Release يجب إضافة Keystore عبر GitHub Secrets وربط signing config بالـ Secrets.
