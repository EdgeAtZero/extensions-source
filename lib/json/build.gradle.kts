buildscript {
    extra["group"] = project.name.split("-").let { "${rootProject.group}.lib.${it[1]}" }
    extra["version.name"] = project.version.toString()
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
