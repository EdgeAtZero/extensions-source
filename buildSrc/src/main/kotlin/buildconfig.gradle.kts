plugins {
    id("com.github.gmazzo.buildconfig")
}
buildConfig {
    className = "BuildConfig"
    packageName = project.extra["group"] as String
    buildConfigField("BUILD_TIME", System.currentTimeMillis())
    buildConfigField("VERSION_CODE", project.extra["version.code"] as Int)
    buildConfigField("VERSION_NAME", project.extra["version.name"] as String)
}
