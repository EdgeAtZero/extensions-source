buildscript {
    extra["group"] = project.name.split("-").let { "eu.kanade.tachiyomi.lib.${it[1]}" }
}
plugins {
    id("android-lib")
}
dependencies {
    compileOnly(libs.kotlin.stdlib)
    compileOnly(libs.kotlin.reflect)
    compileOnly(libs.kotlinx.serialization.core)
    compileOnly(libs.kotlinx.serialization.json)
}
