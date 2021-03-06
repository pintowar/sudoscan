plugins {
    id("sudoscan.kotlin-base")
    kotlin("kapt")
    id("org.bytedeco.gradle-javacpp-platform")
    id("io.kotest")
    `maven-publish`
}

val kotestVersion by extra("4.6.0")

dependencies {
    testImplementation("io.kotest:kotest-runner-junit5-jvm:$kotestVersion") {
        exclude(group = "org.jetbrains.kotlin")
    }
    testImplementation("io.kotest:kotest-assertions-core-jvm:$kotestVersion")
    testImplementation("io.kotest:kotest-assertions-json-jvm:$kotestVersion")
}

publishing {
    repositories {
        maven {
            name = "GitHubPackages"
            setUrl("https://maven.pkg.github.com/pintowar/sudoscan")
            credentials {
                username = project.findProperty("gpr.user")?.toString() ?: System.getenv("USERNAME")
                password = project.findProperty("gpr.key")?.toString() ?: System.getenv("TOKEN")
            }
        }
    }
    publications {
        register<MavenPublication>("gpr") {
            from(components["java"])
            artifact(tasks["sourcesJar"])
            artifact(tasks["javadocJar"])
        }
    }
}