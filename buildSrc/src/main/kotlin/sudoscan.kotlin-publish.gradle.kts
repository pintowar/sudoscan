plugins {
    id("sudoscan.kotlin-base")
    `maven-publish`
}

publishing {
    repositories {
        maven {
            name = "GitHubPackages"
            setUrl("https://maven.pkg.github.com/pintowar/sudoscan")
            credentials {
                username = project.findProperty("gpr.user")?.toString() ?: System.getenv("GITHUB_ACTOR")
                password = project.findProperty("gpr.pass")?.toString() ?: System.getenv("GITHUB_TOKEN")
            }
        }
        maven {
            name = "SudoscanLibs"
            val type = if("${project.version}".contains("SNAPSHOT")) "snapshot" else "release"
            setUrl("https://pintowar.jfrog.io/artifactory/sudoscan-libs-$type")
            credentials {
                username = project.findProperty("jfrog.user")?.toString() ?: System.getenv("JFROG_USER")
                password = project.findProperty("jfrog.pass")?.toString() ?: System.getenv("JFROG_PASS")
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
