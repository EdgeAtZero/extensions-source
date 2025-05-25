plugins {
    id("org.jetbrains.kotlin.jvm")
    application
}

kotlin {
    compilerOptions {
        jvmTarget = Const.JVM_TARGET
    }
}

application {
    applicationName = project.extra["module"] as String
    mainClass = project.extra["main"] as String
}
