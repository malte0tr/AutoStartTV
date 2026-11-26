plugins {
    id("com.android.application")
}

android {
    namespace = "de.autostarttv"
    compileSdk = 35

    defaultConfig {
        applicationId = "de.autostarttv"
        minSdk = 21
        targetSdk = 35
        versionCode = 2
        versionName = "1.1"
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
