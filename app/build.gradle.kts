import java.net.HttpURLConnection
import java.net.URI

plugins { id("com.android.application"); id("org.jetbrains.kotlin.android"); id("org.jetbrains.kotlin.plugin.compose") }

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
        buildTypes.getByName("release") { signingConfig = signingConfigs.getByName("release") }
    }
}

// Download the A-Z phonics recordings into Android's normal raw resource
// directory before any resource/source-set mapping happens. This keeps the
// final APK fully offline at runtime and avoids the previous generated-resource
// directory not being packaged.
val phonicsResDir = layout.projectDirectory.dir("src/main/res/raw").asFile
val downloadEnglishPhonics by tasks.registering {
    doLast {
        phonicsResDir.mkdirs()
        ('a'..'z').forEach { letter ->
            val output = phonicsResDir.resolve("phonics_$letter.ogg")
            if (output.exists() && output.length() > 100) return@forEach
            val url = URI("https://raw.githubusercontent.com/Neuromancer56/phonics_english/main/sounds/phonics_$letter.ogg").toURL()
            val connection = url.openConnection() as HttpURLConnection
            connection.connectTimeout = 30_000
            connection.readTimeout = 60_000
            connection.instanceFollowRedirects = true
            connection.requestMethod = "GET"
            connection.setRequestProperty("User-Agent", "learn-letters-numbers-build")
            try {
                check(connection.responseCode in 200..299) { "Could not download phonics_$letter.ogg: HTTP ${connection.responseCode}" }
                connection.inputStream.use { input -> output.outputStream().use { input.copyTo(it) } }
                check(output.length() > 100) { "Downloaded phonics_$letter.ogg is empty" }
            } finally { connection.disconnect() }
        }
    }
}

// Android Gradle Plugin reads the raw resource directory during these tasks,
// so make the dependency explicit for every variant that maps or merges it.
tasks.configureEach {
    if ((name.startsWith("map") && name.endsWith("SourceSetPaths")) ||
        (name.startsWith("merge") && name.endsWith("Resources"))) {
        dependsOn(downloadEnglishPhonics)
    }
}

dependencies { implementation(platform("androidx.compose:compose-bom:2024.10.01")); implementation("androidx.activity:activity-compose:1.9.3"); implementation("androidx.compose.ui:ui"); implementation("androidx.compose.ui:ui-tooling-preview"); implementation("androidx.compose.material3:material3")
    implementation("androidx.compose.material:material-icons-core"); implementation("androidx.compose.foundation:foundation"); implementation("androidx.compose.animation:animation"); implementation("com.google.android.gms:play-services-ads:25.4.0"); debugImplementation("androidx.compose.ui:ui-tooling") }
