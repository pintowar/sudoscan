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

    implementation(libs.javacv) {
        exclude(group = "org.bytedeco", module = "artoolkitplus")
        exclude(group = "org.bytedeco", module = "flandmark")
        exclude(group = "org.bytedeco", module = "flycapture")
        exclude(group = "org.bytedeco", module = "leptonica")
        exclude(group = "org.bytedeco", module = "libdc1394")
        exclude(group = "org.bytedeco", module = "libfreenect")
        exclude(group = "org.bytedeco", module = "libfreenect2")
        exclude(group = "org.bytedeco", module = "librealsense")
        exclude(group = "org.bytedeco", module = "librealsense2")
        exclude(group = "org.bytedeco", module = "tesseract")
        exclude(group = "org.bytedeco", module = "videoinput")
        exclude(group = "org.bytedeco", module = "openblas")
        exclude(group = "org.bytedeco", module = "ffmpeg")
        exclude(group = "org.bytedeco", module = "opencv")
    }
    implementation(libs.opencv.platform)
    implementation(libs.ffmpeg.platform)

    kapt(libs.picocli)
    implementation(libs.micronaut.picocli)
    implementation(libs.micronaut.kotlin)
    compileOnly(libs.graalvm)
    implementation(libs.awt.color.factory)
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