buildscript {
    extra["group"] = project.name.split("-").let { "${rootProject.group}.extension.${it[1]}.${it[2]}" }
    extra["version.code"] = Const.Git.commit(project.projectDir.absolutePath)
    extra["version.name"] = "1.4.${extra["version.code"] as Int}"
    extra["extension.name"] = "Copy20"
    extra["extension.class"] = ".Copy20"
    extra["extension.nsfw"] = true
}
plugins {
    id("extension-app")
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
    implementation(project(":lib-t2s"))
}
