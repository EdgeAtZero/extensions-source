buildscript {
    extra["group"] = project.name.split("-").let { "eu.kanade.tachiyomi.extension.${it[1]}.${it[2]}" }
    extra["version.code"] = 1
    extra["version.name"] = "1.4.${extra["version.code"]}"
}
plugins {
    id("android-app")
    alias(buildSrc.plugins.kotlin.serialization)
}
android {
    applicationVariants.all {
        val variant = this
        variant.outputs.forEach { output ->
            val packageTask = variant.packageApplicationProvider.get()
            packageTask.doLast {
                val apkFile = output.outputFile
                val unzipDir = File(layout.buildDirectory.asFile.get(), "tmp/unzipped/${variant.name}")
                copy {
                    from(zipTree(apkFile))
                    into(unzipDir)
                }
                apkFile.delete()
                val tcDir = File(unzipDir, "tc")
                val assetsDir = File(unzipDir, "assets").apply { mkdirs() }
                tcDir.resolve("t2s.txt").copyTo(assetsDir.resolve("t2s.txt"), overwrite = true)
                tcDir.deleteRecursively()
                ant.withGroovyBuilder {
                    "zip"(
                        "destfile" to apkFile.absolutePath,
                        "basedir" to unzipDir
                    )
                }
                unzipDir.deleteRecursively()
            }
        }
    }
}
dependencies {
    implementation(project(":lib-json"))
    implementation(libs.transformChinese)

    compileOnly(libs.kotlin.stdlib)
    compileOnly(libs.kotlin.reflect)
    compileOnly(libs.kotlinx.coroutines.core)
    compileOnly(libs.kotlinx.coroutines.android)
    compileOnly(libs.kotlinx.serialization.core)
    compileOnly(libs.kotlinx.serialization.json)
    compileOnly(libs.injekt)
    compileOnly(libs.jsoup)
    compileOnly(libs.okhttp)
    compileOnly(libs.quickjs)
    compileOnly(libs.rxjava)
    compileOnly(libs.tachiyomi)
}
