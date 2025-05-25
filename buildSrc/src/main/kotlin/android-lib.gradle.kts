plugins {
    id("com.android.library")
    id("org.jetbrains.kotlin.android")
}
kotlin {
    compilerOptions {
        jvmTarget = Const.JVM_TARGET
    }
}
android {
    namespace = project.extra["group"] as String
    compileSdk = Const.ANDROID_COMPILE_SDK
    compileOptions {
        sourceCompatibility = Const.JAVA_VERSION
        targetCompatibility = Const.JAVA_VERSION
    }
    defaultConfig {
        minSdk = Const.ANDROID_MIN_SDK
    }
}
