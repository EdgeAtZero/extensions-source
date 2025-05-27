plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.multiplatform")
}
group = project.extra["group"] as String
version = project.extra["version.name"] as String
kotlin {
    androidTarget("android") {
        compilerOptions {
            jvmTarget = Const.Java.TARGET
        }
    }
    jvm("desktop")
}
android {
    namespace = project.extra["group"] as String
    compileSdk = Const.Android.Sdk.COMPILE
    buildTypes {
        release {
            isMinifyEnabled = false
        }
    }
    compileOptions {
        sourceCompatibility = Const.Java.VERSION
        targetCompatibility = Const.Java.VERSION
    }
    defaultConfig {
        minSdk = Const.Android.Sdk.MIN
        minSdk = Const.Android.Sdk.MIN
        targetSdk = Const.Android.Sdk.TARGET
        versionCode = project.extra["version.code"] as Int
        versionName = project.extra["version.name"] as String
    }
    if (project.properties["android.signing.store"] != null) {
        buildTypes["release"].signingConfig = signingConfigs.create("release") {
            storeFile = file(project.properties["android.signing.store"] as String)
            storePassword = project.properties["android.signing.store.password"] as String
            keyAlias = project.properties["android.signing.key.alias"] as String
            keyPassword = project.properties["android.signing.key.password"] as String
            enableV1Signing = true
            enableV2Signing = true
            enableV3Signing = true
        }
    }
}
