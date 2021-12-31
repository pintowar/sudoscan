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
    implementation("org.jetbrains.kotlin:kotlin-gradle-plugin:1.6.10")
    implementation("org.jetbrains.kotlin:kotlin-allopen:1.6.10")
    implementation("org.jetbrains.dokka:dokka-gradle-plugin:1.6.10")
    implementation("io.micronaut.gradle:micronaut-gradle-plugin:2.0.6")
    implementation("org.bytedeco.gradle-javacpp-platform:org.bytedeco.gradle-javacpp-platform.gradle.plugin:1.5.6")
    implementation("gradle.plugin.com.github.johnrengelman:shadow:7.1.2")
    implementation("org.sonarsource.scanner.gradle:sonarqube-gradle-plugin:3.3")
    implementation("io.kotest:kotest-gradle-plugin:0.3.9")
    implementation("org.jlleitschuh.gradle:ktlint-gradle:10.2.1")
    implementation("net.researchgate:gradle-release:2.8.1")
}