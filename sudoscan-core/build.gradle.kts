plugins {
    id("sudoscan.kotlin-javacpp")
    id("java-library")
}

description = "Sudoscan Core"

val vNd4j: String by extra
val vJavaCv: String by extra
val vChocoSolver: String by extra

dependencies {

    implementation("org.choco-solver:choco-solver:$vChocoSolver") {
        exclude(group = "org.knowm.xchart")
        exclude(group = "org.jgrapht")
        exclude(group = "com.github.cp-profiler")
        exclude(group = "dk.brics.automaton")
    }

    implementation("org.deeplearning4j:deeplearning4j-modelimport:$vNd4j")
    implementation("org.nd4j:nd4j-native-platform:$vNd4j") {
        exclude(group = "org.bytedeco", module = "mkl-platform")
    }

    api("org.bytedeco:javacv-platform:$vJavaCv") {
        listOf("artoolkitplus", "flandmark", "flycapture", "leptonica", "libdc1394",
         "libfreenect", "libfreenect2", "librealsense", "librealsense2",
         "tesseract", "videoinput", "ffmeg").forEach {
            exclude(group = "org.bytedeco", module = "${it}-platform")
            exclude(group = "org.bytedeco", module = it)
        }
    }

}
