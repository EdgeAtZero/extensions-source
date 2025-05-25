plugins {
    id("kmp-lib")
    id("org.jetbrains.compose")
    id("org.jetbrains.kotlin.plugin.compose")
}
kotlin {
    sourceSets {
        named("commonMain") {
            dependencies {
                implementation(compose.runtime)
            }
        }
    }
}
compose {
    resources {
        packageOfResClass = project.extra["group"] as String
    }
}
