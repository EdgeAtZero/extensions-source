plugins {
    id("android-lib")
}
dependencies {
    compileOnly(kotlin("stdlib"))
    compileOnly(kotlin("reflect"))
    compileOnly("org.jetbrains.kotlinx:kotlinx-coroutines-android:1.10.2")
    compileOnly("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.10.2")
    compileOnly("org.jetbrains.kotlinx:kotlinx-serialization-core:1.8.1")
    compileOnly("org.jetbrains.kotlinx:kotlinx-serialization-json:1.8.1")
    compileOnly("org.jetbrains.kotlinx:kotlinx-serialization-protobuf:1.8.1")
    compileOnly("com.github.null2264.injekt:injekt-core:4135455a2a")
    compileOnly("org.jsoup:jsoup:1.15.1")
    compileOnly("com.squareup.okhttp3:okhttp:5.0.0-alpha.11")
    compileOnly("app.cash.quickjs:quickjs-android:0.9.2")
    compileOnly("io.reactivex:rxjava:1.3.8")
    compileOnly("com.github.keiyoushi:extensions-lib:v1.4.2.1")
}
