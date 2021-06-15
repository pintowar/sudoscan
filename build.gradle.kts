plugins {
    id("sudoscan.kotlin-base")
    id("net.researchgate.release")
}

allprojects {
    group = "com.github.pintowar"
    val vPicocli by extra("4.5.1")
    val vNd4j by extra("1.0.0-beta7")
    val vJavaCv by extra("1.5.3")
    val vChocoSolver by extra("4.10.6")
//    javacppPlatform = "linux-x86_64,macosx-x86_64,windows-x86_64"

}
tasks {
    register<JacocoReport>("codeCoverageReport") {
        group = "verification"
        description = "Run tests and merge all jacoco reports"

        // If a subproject applies the 'jacoco' plugin, add the result it to the report
        subprojects {
            val subproject = this
            subproject.plugins.withType<JacocoPlugin>().configureEach {
                subproject.tasks.matching { it.extensions.findByType<JacocoTaskExtension>() != null }.configureEach {
                    val testTask = this
                    sourceSets(subproject.sourceSets.main.get())
                    executionData(testTask)
                }

                subproject.tasks.matching { it.extensions.findByType<JacocoTaskExtension>() != null }.forEach {
                    rootProject.tasks["codeCoverageReport"].dependsOn(it)
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
                from(fileTree(mapOf("dir" to "${project(":sudoscan-deskapp").buildDir}/libs/")))
                into("$rootDir/build/")
            }

            logger.quiet("JAR generated at $rootDir/build/. It combines the server and client projects.")
        }
    }
}

release {
    git {
        signTag = true
    }
}

tasks.afterReleaseBuild {
    dependsOn("publish")
}
