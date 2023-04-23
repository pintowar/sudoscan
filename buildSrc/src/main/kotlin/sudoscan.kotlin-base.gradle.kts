import org.jetbrains.kotlin.gradle.dsl.JvmTarget
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile
import org.jlleitschuh.gradle.ktlint.reporter.ReporterType
import org.jlleitschuh.gradle.ktlint.KtlintExtension

plugins {
    kotlin("jvm")
    id("org.jetbrains.dokka")
    id("jacoco")
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
    testImplementation(libs.kotest.assertions.core)
    testImplementation(libs.kotest.assertions.json)
    testImplementation(libs.mockk)
}

java {
    toolchain {
        languageVersion.set(JavaLanguageVersion.of(11))
    }
}

configure<KtlintExtension> {
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
        compilerOptions {
            jvmTarget.set(JvmTarget.JVM_11)
            freeCompilerArgs.set(listOf("-Xjsr305=strict"))
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
