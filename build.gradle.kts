plugins {
    id("sudoscan.kotlin-base")
    id("net.researchgate.release")
    id("org.sonarqube")
}

allprojects {
    group = "com.github.pintowar"
//    javacppPlatform = "linux-x86_64,macosx-x86_64,windows-x86_64"
}

tasks {
    register<JacocoReport>("codeCoverageReport") {
        group = "verification"
        description = "Run tests and merge all jacoco reports"

        val codeCoverageTask = this
        // If a subproject applies the 'jacoco' plugin, add the result it to the report
        subprojects {
            val subproject = this
            subproject.plugins.withType<JacocoPlugin>().configureEach {
                val extensions = subproject.tasks.matching { it.extensions.findByType<JacocoTaskExtension>() != null }
                extensions.forEach { codeCoverageTask.dependsOn(it) }

                extensions.configureEach {
                    val testTask = this
                    sourceSets(subproject.sourceSets.main.get())
                    executionData(testTask)
                }
            }
        }

        reports {
            xml.required.set(true)
            html.required.set(true)
            csv.required.set(true)
        }
    }

    register("assembleDesktopApp") {
        dependsOn(":sudoscan-deskapp:shadowJar")
        group = "build"
        description = "Build desktop app"
        doLast {
            copy {
                from(files("${project(":sudoscan-deskapp").buildDir}/libs/")) {
                    include("*-all.jar")
                }
                into("$rootDir/build/")
            }

            logger.quiet("JAR generated at $rootDir/build/.")
        }
    }
}

sonarqube {
    properties {
        val jacocoReportPath = "${project.buildDir.absolutePath}/reports/jacoco/codeCoverageReport"
        val sonarToken = project.findProperty("sonar.token")?.toString() ?: System.getenv("SONAR_TOKEN")
        property("sonar.sourceEncoding", "UTF-8")
        property("sonar.organization", "pintowar")
        property("sonar.projectName", "sudoscan")
        property("sonar.projectKey", "pintowar_sudoscan")
        property("sonar.projectVersion", project.version.toString())
        property("sonar.host.url", "https://sonarcloud.io")
        property("sonar.login", sonarToken)
        property("sonar.verbose", true)
        property("sonar.github.repository", "pintowar/sudoscan")
        property("sonar.coverage.jacoco.xmlReportPaths", "$jacocoReportPath/codeCoverageReport.xml")
    }
}

release {
    tagTemplate = "v\$version"

    git {
        requireBranch = "master"
    }
}

tasks.afterReleaseBuild {
    dependsOn(
        ":sudoscan-api:publish", ":sudoscan-solver-choco:publish", ":sudoscan-recognizer-dl4j:publish",
        ":sudoscan-recognizer-djl:publish", ":sudoscan-deskapp:publish"
    )
}