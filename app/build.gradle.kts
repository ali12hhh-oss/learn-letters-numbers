plugins { id("com.android.application"); id("org.jetbrains.kotlin.android"); id("org.jetbrains.kotlin.plugin.compose") }

import java.net.HttpURLConnection
import java.net.URI

android {
    namespace = "com.learnlettersnumbers.app"
    compileSdk = 36
    defaultConfig { applicationId="com.learnlettersnumbers.app"; minSdk=24; targetSdk=36; versionCode=1; versionName = "1.0.0"
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
    buildFeatures {
        compose = true
        buildConfig = true
    }

    // Release signing is enabled only when the keystore and credentials are
    // supplied through environment variables / local properties. Private
    // signing material is intentionally never stored in Git.
    val releaseKeystorePath = providers.environmentVariable("RELEASE_KEYSTORE_PATH").orNull
    val releaseStorePassword = providers.environmentVariable("RELEASE_STORE_PASSWORD").orNull
    val releaseKeyAlias = providers.environmentVariable("RELEASE_KEY_ALIAS").orNull
    val releaseKeyPassword = providers.environmentVariable("RELEASE_KEY_PASSWORD").orNull

    if (!releaseKeystorePath.isNullOrBlank() && !releaseStorePassword.isNullOrBlank() &&
        !releaseKeyAlias.isNullOrBlank() && !releaseKeyPassword.isNullOrBlank()) {
        signingConfigs.create("release") {
            storeFile = file(releaseKeystorePath)
            storePassword = releaseStorePassword
            keyAlias = releaseKeyAlias
            keyPassword = releaseKeyPassword
        }
        buildTypes.getByName("release") {
            signingConfig = signingConfigs.getByName("release")
        }
    }
}

// The phonics_english project publishes the A-Z phonics media separately from
// its code. Download the CC0 media during the Android build so the APK keeps
// the sounds locally and remains offline at runtime. Existing files are reused.
val phonicsGeneratedResDir = layout.buildDirectory.dir("generated/phonics/res/raw")
val downloadEnglishPhonics by tasks.registering {
    outputs.dir(phonicsGeneratedResDir)
    doLast {
        val outDir = phonicsGeneratedResDir.get().asFile
        outDir.mkdirs()
        val alphabet = ('a'..'z').toList()
        alphabet.forEach { letter ->
            val output = outDir.resolve("phonics_$letter.ogg")
            if (output.exists() && output.length() > 100) return@forEach

            val url = URI("https://raw.githubusercontent.com/Neuromancer56/phonics_english/main/sounds/phonics_$letter.ogg").toURL()
            val connection = url.openConnection() as HttpURLConnection
            connection.connectTimeout = 30_000
            connection.readTimeout = 60_000
            connection.instanceFollowRedirects = true
            connection.requestMethod = "GET"
            connection.setRequestProperty("User-Agent", "learn-letters-numbers-build")
            try {
                check(connection.responseCode in 200..299) {
                    "Could not download phonics_$letter.ogg: HTTP ${connection.responseCode}"
                }
                connection.inputStream.use { input -> output.outputStream().use { input.copyTo(it) } }
                check(output.length() > 100) { "Downloaded phonics_$letter.ogg is empty" }
            } finally {
                connection.disconnect()
            }
        }
    }
}

android.sourceSets["main"].res.srcDir(phonicsGeneratedResDir)
tasks.named("preBuild").configure { dependsOn(downloadEnglishPhonics) }

dependencies { implementation(platform("androidx.compose:compose-bom:2024.10.01")); implementation("androidx.activity:activity-compose:1.9.3"); implementation("androidx.compose.ui:ui"); implementation("androidx.compose.ui:ui-tooling-preview"); implementation("androidx.compose.material3:material3")
    implementation("androidx.compose.material:material-icons-core"); implementation("androidx.compose.foundation:foundation"); implementation("androidx.compose.animation:animation"); implementation("com.google.android.gms:play-services-ads:25.4.0"); debugImplementation("androidx.compose.ui:ui-tooling") }
