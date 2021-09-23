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
    implementation("org.jetbrains.kotlin:kotlin-gradle-plugin:1.5.31")
    implementation("org.jetbrains.kotlin:kotlin-allopen:1.5.31")
    implementation("org.jetbrains.dokka:dokka-gradle-plugin:1.5.30")
    implementation("io.micronaut.gradle:micronaut-gradle-plugin:2.0.4")
    implementation("org.bytedeco.gradle-javacpp-platform:org.bytedeco.gradle-javacpp-platform.gradle.plugin:1.5.6")
    implementation("gradle.plugin.com.github.jengelman.gradle.plugins:shadow:7.0.0")
    implementation("com.github.node-gradle:gradle-node-plugin:3.1.1")
    implementation("org.sonarsource.scanner.gradle:sonarqube-gradle-plugin:3.3")
    implementation("io.kotest:kotest-gradle-plugin:0.3.8")
    implementation("net.researchgate:gradle-release:2.8.1")
}