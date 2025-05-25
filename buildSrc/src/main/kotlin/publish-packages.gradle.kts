extensions.configure<PublishingExtension> {
    repositories {
        maven {
            name = "Packages"
            credentials {
                username = findProperty("packages.username")?.toString() ?: System.getenv("PACKAGES_USERNAME")
                password = findProperty("packages.password")?.toString() ?: System.getenv("PACKAGES_PASSWORD")
            }
            setUrl(findProperty("packages.api")?.toString() ?: System.getenv("PACKAGES_API"))
        }
    }
}
