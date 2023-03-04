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

val catalogs = extensions.getByType<VersionCatalogsExtension>()
val libs = catalogs.named("libs")

dependencies {
    implementation(platform(libs.findLibrary("kotlin.bom").get()))
    implementation(libs.findLibrary("kotlin.reflect").get())
    implementation(libs.findLibrary("kotlin.jdk8").get())
    implementation(libs.findLibrary("kotlin.logging").get())

    runtimeOnly(libs.findLibrary("logback.classic").get())

    testImplementation(libs.findLibrary("kotest.junit").get()) {
        exclude(group = "org.jetbrains.kotlin")
    }
    testImplementation(libs.findLibrary("kotest.junit").get())
    testImplementation(libs.findLibrary("kotest.assertions.core").get())
    testImplementation(libs.findLibrary("kotest.assertions.json").get())
    testImplementation(libs.findLibrary("mockk").get())
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

    test {
        useJUnitPlatform()
        finalizedBy(tasks.jacocoTestReport) // report is always generated after tests run
    }

    jacocoTestReport {
        dependsOn(tasks.test) // tests are required to run before generating the report
    }
}
