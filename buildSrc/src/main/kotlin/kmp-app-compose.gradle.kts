plugins {
    id("kmp-app")
    id("org.jetbrains.compose")
    id("org.jetbrains.kotlin.plugin.compose")
}

kotlin {
    sourceSets {
        named("commonMain") {
            dependencies {
                implementation(compose.runtime)
                implementation(compose.components.resources)
            }
        }
        named("desktopMain") {
            dependencies {
                implementation(compose.desktop.currentOs)
            }
        }
    }
}

compose {
    desktop {
        application {
            buildTypes {
                release {
                    proguard {
                        isEnabled = false
                    }
                }
            }
            mainClass = project.extra["main"] as String
            nativeDistributions {
                packageName = project.extra["module"] as String
                packageVersion = project.extra["version.name"] as String
            }
        }
    }
    resources {
        packageOfResClass = project.extra["group"] as String
    }
}
