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
    implementation("org.jetbrains.kotlin:kotlin-gradle-plugin:1.5.10")
    implementation("org.jetbrains.dokka:dokka-gradle-plugin:1.4.32")
    implementation("org.bytedeco.gradle-javacpp-platform:org.bytedeco.gradle-javacpp-platform.gradle.plugin:1.5.4")
    implementation("gradle.plugin.com.github.jengelman.gradle.plugins:shadow:7.0.0")
    implementation("io.kotest:kotest-gradle-plugin:0.3.8")
    implementation("net.researchgate:gradle-release:2.8.1")
}