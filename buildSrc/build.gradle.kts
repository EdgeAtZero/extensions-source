plugins {
    `kotlin-dsl`
}
sourceSets {
    main {
        kotlin.srcDir("src/main/gradle")
    }
}
dependencies {
    implementation(gradleApi())
    implementation(libs.android.gradle)
    implementation(libs.buildconfig.gradle)
    implementation(libs.compose.compiler)
    implementation(libs.compose.gradle)
    implementation(libs.kotlin.gradle)
    implementation(files(libs.javaClass.protectionDomain.codeSource.location))
}
repositories {
    google()
    mavenCentral()
    gradlePluginPortal()
}
