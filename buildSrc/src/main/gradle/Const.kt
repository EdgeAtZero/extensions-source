import org.gradle.api.JavaVersion
import org.jetbrains.kotlin.gradle.dsl.JvmTarget

object Const {

    object Android {

        object  Sdk {

            const val COMPILE = 35
            const val TARGET = 35
            const val MIN = 21

        }

    }

    object Java {

        val VERSION = JavaVersion.VERSION_1_8
        val TARGET = JvmTarget.JVM_1_8

    }

    object Git {

        val COMMIT by lazy { exec("git", "rev-list", "HEAD", "--first-parent", "--count").toInt() }
        val SHA by lazy { exec("git", "rev-parse", "--short", "HEAD") }
        val TAG by lazy { exec("git", "describe", "--tags", "--abbrev=0") }

        fun commit(path: String): Int =
            exec("git", "rev-list", "HEAD", "--first-parent", "--count", "--", path).toInt()
    }

}
