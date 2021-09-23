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
    implementation(platform(Libs.Kotlin.bom))
    implementation(Libs.Kotlin.reflect)
    implementation(Libs.Kotlin.jdk8)
    implementation(Libs.Kotlin.logging)

    runtimeOnly(Libs.LogBack.logback)
    testImplementation(Libs.Mockk.mockk)
}

java {
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
//    filter {
//        exclude("**/style-violations.kt")
//    }
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

    test {
        useJUnitPlatform()
        finalizedBy(tasks.jacocoTestReport) // report is always generated after tests run
    }

    jacocoTestReport {
        dependsOn(tasks.test) // tests are required to run before generating the report
    }
}
