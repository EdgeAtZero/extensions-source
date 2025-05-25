buildscript {
    extra["group"] = project.name.split("-").let { "eu.kanade.tachiyomi.extension.${it[1]}.${it[2]}" }
    extra["version.code"] = 1
    extra["version.name"] = "1.4.${extra["version.code"]}"
}
plugins {
    id("android-app")
    alias(buildSrc.plugins.kotlin.serialization)
}
android {
    packaging {
        resources {
            excludes += "tc/**"
        }
    }
}
dependencies {
    implementation(project(":lib-json"))
    implementation(libs.transformChinese)

    compileOnly(libs.kotlin.stdlib)
    compileOnly(libs.kotlin.reflect)
    compileOnly(libs.kotlinx.coroutines.core)
    compileOnly(libs.kotlinx.coroutines.android)
    compileOnly(libs.kotlinx.serialization.core)
    compileOnly(libs.kotlinx.serialization.json)
    compileOnly(libs.injekt)
    compileOnly(libs.jsoup)
    compileOnly(libs.okhttp)
    compileOnly(libs.quickjs)
    compileOnly(libs.rxjava)
    compileOnly(libs.tachiyomi)
}
