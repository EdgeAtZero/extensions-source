extensions.configure<PublishingExtension> {
    repositories {
        maven {
            name = "MavenCentral"
            credentials {
                username = findProperty("maven.username")?.toString() ?: System.getenv("MAVEN_USERNAME")
                password = findProperty("maven.password")?.toString() ?: System.getenv("MAVEN_PASSWORD")
            }
            setUrl(findProperty("maven.api")?.toString() ?: System.getenv("MAVEN_API"))
        }
    }
}
