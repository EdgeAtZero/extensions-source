buildscript {
    extra["group"] = project.name.split("-").let { "${rootProject.group}.lib.${it[1]}" }
    extra["version.name"] = project.version.toString()
}
plugins {
    id("extension-lib")
}
