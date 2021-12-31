import Libs.AwtColorFactory.implementAwtColorFactory
import Libs.JavaCv.implementFfmpeg
import Libs.JavaCv.implementJavaCv
import Libs.JavaCv.implementOpenCv
import Libs.Micronaut.implementMicronautPicocli

plugins {
    id("sudoscan.kotlin-app")
    id("io.micronaut.application")
    id("io.micronaut.graalvm")
    id("com.github.johnrengelman.shadow")
}

description = "Sudoscan CLI App"

micronaut {
    testRuntime("kotest")
    processing {
        incremental(true)
        annotations("com.github.pintowar.sudoscan.cli.*")
    }
}

val hasDjl = project.hasProperty("djl")
val hasOjalgo = project.hasProperty("ojalgo")
dependencies {
    implementation(if (hasOjalgo) projects.sudoscanSolverOjalgo else projects.sudoscanSolverChoco)
    implementation(if (hasDjl) projects.sudoscanRecognizerDjl else projects.sudoscanRecognizerDl4j)
    implementJavaCv()
    implementOpenCv()
    implementFfmpeg()

    implementMicronautPicocli()
    implementAwtColorFactory()
}

application {
    // Define the main class for the application.
    mainClass.set("com.github.pintowar.sudoscan.cli.ApplicationKt")
}

tasks {
    val platform = project.properties["javacppPlatform"] ?: "multi"
    val baseName = "${project.name}-app-$platform"

    graalvmNative {
        binaries {
            named("main") {
                buildArgs("--verbose", "-Djava.awt.headless=false")
                imageName.set(baseName)
            }
        }
    }

    shadowJar {
        archiveFileName.set("$baseName-all.${archiveExtension.get()}")
        mergeServiceFiles()
    }
}