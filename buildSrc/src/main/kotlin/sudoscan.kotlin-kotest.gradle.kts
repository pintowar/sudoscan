plugins {
    id("sudoscan.kotlin-base")
    id("io.kotest")
    `maven-publish`
}

dependencies {
    testImplementation(Libs.Kotest.junit) {
        exclude(group = "org.jetbrains.kotlin")
    }
    testImplementation(Libs.Kotest.assertionsCore)
    testImplementation(Libs.Kotest.assertionsJson)
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