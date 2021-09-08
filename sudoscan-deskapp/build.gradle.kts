import Libs.AwtColorFactory.implementAwtColorFactory
import Libs.JavaCv.apiFfmpeg
import Libs.JavaCv.apiJavaCv
import Libs.Picocli.implementPicocli

plugins {
    id("sudoscan.kotlin-javacpp")
    id("application")
    id("com.github.johnrengelman.shadow")
}

description = "Sudoscan Desktop App"

kapt {
    arguments {
        arg("project", "${project.group}/${project.name}")
    }
}

val hasDjl = project.hasProperty("djl")
dependencies {
    api(projects.sudoscanCore)
    api(if(hasDjl) projects.sudoscanRecognizerDjl else projects.sudoscanRecognizerNd4j)
    apiJavaCv()
    apiFfmpeg()

    implementPicocli()
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
