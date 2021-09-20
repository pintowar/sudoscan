import Libs.AwtColorFactory.implementAwtColorFactory
import Libs.JavaCv.apiFfmpeg
import Libs.JavaCv.apiJavaCv
import Libs.Micronaut.implementMicronautPicocli

plugins {
    id("sudoscan.kotlin-javacpp")
    id("io.micronaut.application")
    id("application")
    id("com.github.johnrengelman.shadow")
}

description = "Sudoscan Desktop App"

micronaut {
    testRuntime("kotest")
    processing {
        incremental(true)
        annotations("com.github.pintowar.sudoscan.viewer.*")
    }
}

tasks.nativeImage {
    args("--verbose")
    imageName.set("sudoscan-deskapp-app")
}

val hasDjl = project.hasProperty("djl")
dependencies {
    implementation(projects.sudoscanSolverChoco)
    implementation(if(hasDjl) projects.sudoscanRecognizerDjl else projects.sudoscanRecognizerDl4j)
    apiJavaCv()
    apiFfmpeg()

    implementMicronautPicocli()
    implementAwtColorFactory()
}

application {
    // Define the main class for the application.
    mainClass.set("com.github.pintowar.sudoscan.viewer.SudoscanApplicationKt")
}

tasks.shadowJar {
    archiveBaseName.set("sudoscan-desktop-app")
    mergeServiceFiles()
}
