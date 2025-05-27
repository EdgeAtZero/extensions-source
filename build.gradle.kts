@file:Suppress("UnstableApiUsage")

buildscript {
    extra["group"] = "eu.kanade.tachiyomi"
}
group = extra["group"] as String
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
