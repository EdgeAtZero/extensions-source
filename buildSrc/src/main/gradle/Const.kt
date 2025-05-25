import org.gradle.api.JavaVersion
import org.jetbrains.kotlin.gradle.dsl.JvmTarget

object Const {
    const val ANDROID_COMPILE_SDK = 35
    const val ANDROID_TARGET_SDK = 35
    const val ANDROID_MIN_SDK = 21

    val GIT_COMMIT by lazy { exec("git", "rev-list", "HEAD", "--first-parent", "--count").toInt() }
    val GIT_SHA by lazy { exec("git", "rev-parse", "--short", "HEAD") }
    val GIT_TAG by lazy { exec("git", "describe", "--tags", "--abbrev=0") }

    val JAVA_VERSION = JavaVersion.VERSION_1_8
    val JVM_TARGET = JvmTarget.JVM_1_8
}
