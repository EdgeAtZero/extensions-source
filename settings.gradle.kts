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
rootDir
    .resolve("lib")
    .listFiles { it: File -> it.isDirectory }
    .forEach { lib ->
        val name = ":lib-${lib.name}"
        include(name)
        project(name).projectDir = lib
    }
rootDir
    .resolve("src")
    .listFiles { it: File -> it.isDirectory }
    .forEach { lang ->
        lang.listFiles { it: File -> it.isDirectory }.forEach { ext ->
            val name = ":ext-${lang.name}-${ext.name}"
            include(name)
            project(name).projectDir = ext
        }
    }
