import Libs.AwtColorFactory.implementAwtColorFactory
import Libs.Nd4j.apiDatavec
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

dependencies {
    implementation(projects.sudoscanNd4j)
    implementPicocli()
    implementAwtColorFactory()
    apiDatavec()
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
