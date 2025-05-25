plugins {
    id("maven-publish")
    id("signing")
}

afterEvaluate {
    for (publication in publishing.publications.filterIsInstance<MavenPublication>()) {
        publication.artifactId = project.extra["module"] as String
        publication.groupId = project.extra["group"] as String
        publication.version = project.extra["version.name"] as String
        publication.pom {
            name = publication.artifactId
            description = project.extra["desc"] as String
            url = project.extra["repo"] as String
            developers {
                developer {
                    id = "EdgeAtZero"
                    email = "edgeatzero@gmail.com"
                }
            }
        }
    }
}

signing {
    sign(publishing.publications)
}
