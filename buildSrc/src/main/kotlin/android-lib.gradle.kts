plugins {
    id("com.android.library")
    id("org.jetbrains.kotlin.android")
}
group = project.extra["group"] as String
version = project.extra["version.name"] as String
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
