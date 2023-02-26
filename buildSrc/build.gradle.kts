plugins {
    `kotlin-dsl`
    `kotlin-dsl-precompiled-script-plugins`
}

repositories {
    mavenCentral()
    maven {
        url = uri("https://plugins.gradle.org/m2/")
    }
}

dependencies {
    implementation(libs.gradleplugin.kotlin)
    implementation(libs.gradleplugin.kotlin.allopen)
    implementation(libs.gradleplugin.dokka)
    implementation(libs.gradleplugin.micronaut)

    implementation(libs.gradleplugin.javacpp)
    implementation(libs.gradleplugin.shadow)
    implementation(libs.gradleplugin.sonarqube)
    implementation(libs.gradleplugin.kotest)
    implementation(libs.gradleplugin.ktlint)
    implementation(libs.gradleplugin.release)

    implementation(libs.gradleplugin.jmh)
    implementation(libs.gradleplugin.jbake)
    implementation(libs.gradleplugin.asciidoctorj.diagram)
    implementation(libs.gradleplugin.git.publish)
}