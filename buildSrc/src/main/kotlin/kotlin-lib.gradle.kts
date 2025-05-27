plugins {
    id("org.jetbrains.kotlin.jvm")
}
group = project.extra["group"] as String
version = project.extra["version.name"] as String
kotlin {
    compilerOptions {
        jvmTarget = Const.Java.TARGET
    }
}
