plugins {
    id("sudoscan.kotlin-javacpp")
    id("application")
    id("com.github.johnrengelman.shadow")
}

description = "Sudoscan Desktop App"

val vNd4j: String by extra
val vPicocli: String by extra

kapt {
    arguments {
        arg("project", "${project.group}/${project.name}")
    }
}

dependencies {

    implementation(project(":sudoscan-core"))

    kapt("info.picocli:picocli-codegen:$vPicocli")
    implementation("info.picocli:picocli:$vPicocli")
    implementation("org.beryx:awt-color-factory:1.0.1")

    api("org.datavec:datavec-data-image:$vNd4j") {
        exclude(group = "org.freemarker")
        exclude(group = "com.github.jai-imageio")
        exclude(group = "com.twelvemonkeys.imageio", module = "imageio-bmp")
        exclude(group = "com.twelvemonkeys.imageio", module = "imageio-psd")
        exclude(group = "com.twelvemonkeys.imageio", module = "imageio-tiff")

        listOf("artoolkitplus", "flandmark", "flycapture", "leptonica", "libdc1394",
         "libfreenect", "libfreenect2", "librealsense", "librealsense2",
         "tesseract", "videoinput", "javacv-platform").forEach {
            exclude(group = "org.bytedeco", module = it)
        }
    }

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
