import org.jetbrains.kotlin.gradle.tasks.KotlinCompile
import org.jlleitschuh.gradle.ktlint.reporter.ReporterType

plugins {
    kotlin("jvm")
    id("org.jetbrains.dokka")
    id("jacoco")
    id("idea")
    id("org.jlleitschuh.gradle.ktlint")
}

repositories {
    mavenLocal()
    mavenCentral()
}

dependencies {
    implementation(platform(libs.kotlin.bom))
    implementation(libs.kotlin.reflect)
    implementation(libs.kotlin.jdk8)
    implementation(libs.kotlin.logging)

    runtimeOnly(libs.logback.classic)

    testImplementation(libs.kotest.junit) {
        exclude(group = "org.jetbrains.kotlin")
    }
    testImplementation(libs.kotest.junit)
    testImplementation(libs.kotest.assertions.core)
    testImplementation(libs.kotest.assertions.json)
    testImplementation(libs.mockk)
}

java {
    toolchain {
        languageVersion.set(JavaLanguageVersion.of(11))
    }
    sourceCompatibility = JavaVersion.VERSION_11
    targetCompatibility = JavaVersion.VERSION_11
}

ktlint {
    verbose.set(true)
    outputToConsole.set(true)
    coloredOutput.set(true)
    reporters {
        reporter(ReporterType.CHECKSTYLE)
        reporter(ReporterType.JSON)
        reporter(ReporterType.HTML)
    }
}

tasks {
    register<Jar>("sourcesJar") {
        archiveClassifier.set("sources")
        archiveExtension.set("jar")
        from(sourceSets["main"].allSource)
    }

    register<Jar>("javadocJar") {
        dependsOn(dokkaJavadoc)
        archiveClassifier.set("javadoc")
        archiveExtension.set("jar")
        from("$buildDir/dokka/javadoc")
    }

    withType<KotlinCompile> {
        kotlinOptions {
            jvmTarget = "11"
            freeCompilerArgs = listOf("-Xjsr305=strict")
        }
    }

    // Do not generate reports for individual projects
    jacocoTestReport {
        enabled = false
    }
}

testing {
    suites {
        val test by getting(JvmTestSuite::class) {
            useJUnitJupiter()
        }
    }
}
