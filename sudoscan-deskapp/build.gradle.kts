import Libs.AwtColorFactory.implementAwtColorFactory
import Libs.JavaCv.apiFfmpeg
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
    api(if(hasDjl) projects.sudoscanDjl else projects.sudoscanNd4j)
    apiFfmpeg()

    implementPicocli()
    implementAwtColorFactory()
}

application {
    // Define the main class for the application.
    mainClass.set("com.github.pintowar.sudoscan.viewer.SudoscanApplicationKt")
}

tasks.shadowJar {
//    mainClassName.set("com.github.pintowar.sudoscan.viewer.SudoscanApplicationKt")
    archiveBaseName.set("sudoscan-desktop-app")
    mergeServiceFiles()
}
