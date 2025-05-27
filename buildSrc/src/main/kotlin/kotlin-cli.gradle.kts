plugins {
    id("org.jetbrains.kotlin.jvm")
    application
}
group = project.extra["group"] as String
version = project.extra["version.name"] as String
kotlin {
    compilerOptions {
        jvmTarget = Const.Java.TARGET
    }
}
application {
    applicationName = project.extra["module"] as String
    mainClass = project.extra["main"] as String
}
