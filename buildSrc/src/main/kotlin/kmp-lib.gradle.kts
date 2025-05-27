plugins {
    id("com.android.library")
    id("org.jetbrains.kotlin.multiplatform")
}
group = project.extra["group"] as String
version = project.extra["version.name"] as String
kotlin {
    androidTarget("android") {
        publishLibraryVariants("release")
        compilerOptions {
            jvmTarget = Const.Java.TARGET
        }
    }
    jvm("desktop")
}
android {
    namespace = project.extra["group"] as String
    compileSdk = Const.Android.Sdk.COMPILE
    compileOptions {
        sourceCompatibility = Const.Java.VERSION
        targetCompatibility = Const.Java.VERSION
    }
    defaultConfig {
        minSdk = Const.Android.Sdk.MIN
    }
}
