plugins {
    id("sudoscan.kotlin-base")
    id("net.researchgate.release")
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
                from(fileTree(mapOf("dir" to "${project(":sudoscan-deskapp").buildDir}/libs/")))
                into("$rootDir/build/")
            }

            logger.quiet("JAR generated at $rootDir/build/. It combines the server and client projects.")
        }
    }
}

tasks.afterReleaseBuild {
    dependsOn(":sudoscan-core:publish", ":sudoscan-deskapp:publish")
}
