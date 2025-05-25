plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.multiplatform")
}

group = project.extra["group"] as String
version = project.extra["version.name"] as String

kotlin {
    androidTarget("android") {
        compilerOptions {
            jvmTarget = Const.JVM_TARGET
        }
    }
    jvm("desktop")
}

android {
    namespace = project.extra["group"] as String
    compileSdk = Const.ANDROID_COMPILE_SDK
    buildTypes {
        release {
            isMinifyEnabled = false
        }
    }
    compileOptions {
        sourceCompatibility = Const.JAVA_VERSION
        targetCompatibility = Const.JAVA_VERSION
    }
    defaultConfig {
        minSdk = Const.ANDROID_MIN_SDK
        minSdk = Const.ANDROID_MIN_SDK
        targetSdk = Const.ANDROID_TARGET_SDK
        versionCode = project.extra["version.code"] as Int
        versionName = project.extra["version.name"] as String
    }

    if (project.properties["android.signing.store"] != null) {
        buildTypes["release"].signingConfig = signingConfigs.create("release") {
            storeFile = file(project.properties["android.signing.store"] as String)
            storePassword = project.properties["android.signing.store.password"] as String
            keyAlias = project.properties["android.signing.key.alias"] as String
            keyPassword = project.properties["android.signing.key.password"] as String
        }
    }
}
