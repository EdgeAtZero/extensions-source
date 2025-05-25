@file:Suppress("UnstableApiUsage")

buildscript {
    extra["group"] = "me.edgeatzero.ext"
}
allprojects {
    repositories {
        google {
            mavenContent {
                includeGroupAndSubgroups("androidx")
                includeGroupAndSubgroups("com.android")
                includeGroupAndSubgroups("com.google")
            }
        }
        mavenCentral()
        maven("https://jitpack.io")
    }
}
