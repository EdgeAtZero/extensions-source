import kotlin.io.path.listDirectoryEntries
import kotlin.io.path.name

rootProject.name = "extensions-source"
dependencyResolutionManagement {
    versionCatalogs {
        register("buildSrc") {
            from(files("buildSrc/libs.versions.toml"))
        }
        register("libs") {
            from(files("libs.versions.toml"))
        }
    }
}
rootDir.toPath()
    .resolve("lib")
    .listDirectoryEntries()
    .forEach { lib ->
        val name = ":lib-${lib.name}"
        include(name)
        project(name).projectDir = lib.toFile()
    }
rootDir.toPath()
    .resolve("src")
    .listDirectoryEntries()
    .forEach { lang ->
        lang.listDirectoryEntries().forEach { ext ->
            val name = ":ext-${lang.name}-${ext.name}"
            include(name)
            project(name).projectDir = ext.toFile()
        }
    }
