plugins {
    id("sudoscan.kotlin-base")
    id("io.kotest")
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
            name = "Sonatype"
            val releasesRepoUrl = uri("https://s01.oss.sonatype.org/service/local/staging/deploy/maven2/")
            val snapshotsRepoUrl = uri("https://s01.oss.sonatype.org/content/repositories/snapshots/")
            setUrl(if (version.toString().endsWith("SNAPSHOT")) snapshotsRepoUrl else releasesRepoUrl)
            credentials {
                username = project.findProperty("ossrh.user")?.toString() ?: System.getenv("SONATYPE_USER")
                password = project.findProperty("ossrh.pass")?.toString() ?: System.getenv("SONATYPE_PASS")
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
