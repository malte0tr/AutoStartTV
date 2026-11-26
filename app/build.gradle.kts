import java.time.LocalDate
import java.time.format.DateTimeFormatter

plugins {
    id("com.android.application")
}

// Version = versionMajor.versionMinor (Versionsnummer.Zwischennummer).
val versionMajor = 1
val versionMinor = 5

android {
    namespace = "de.autostarttv"
    compileSdk = 35

    defaultConfig {
        applicationId = "de.autostarttv"
        minSdk = 21
        targetSdk = 35
        versionCode = 6
        versionName = "$versionMajor.$versionMinor"
    }

    buildTypes {
        release {
            // Shrinks and optimizes the code: smaller APK, faster start.
            isMinifyEnabled = true
            isShrinkResources = true
            proguardFiles(getDefaultProguardFile("proguard-android-optimize.txt"))
            // Signed with the debug key so the APK can be installed directly.
            signingConfig = signingConfigs.getByName("debug")
        }
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
}

// APK file name: AutoStartTV_<YYYYMMDD><Versionsnummer>-<Zwischennummer>.apk,
// e.g. AutoStartTV_202611261-3.apk (built on 26 Nov 2026, version 1.3).
val buildDate: String = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyyMMdd"))
android.applicationVariants.all {
    val suffix = if (buildType.name == "release") "" else "-${buildType.name}"
    outputs.all {
        (this as com.android.build.gradle.internal.api.BaseVariantOutputImpl).outputFileName =
            "AutoStartTV_$buildDate$versionMajor-$versionMinor$suffix.apk"
    }
}
