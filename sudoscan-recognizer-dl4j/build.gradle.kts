plugins {
    id("sudoscan.kotlin-publish")
    id("java-library")
}

description = "Sudoscan Recognizer Dl4j"

dependencies {
    api(projects.sudoscanApi)
    implementation(libs.recognizer.dl4j.nd4j) {
        exclude(group = "org.bytedeco", module = "mkl-platform")
    }
    implementation(libs.recognizer.dl4j.deeplearning4j) {
        exclude(group = "org.bytedeco", module = "hdf5-platform")
    }
    implementation(libs.recognizer.dl4j.hdf5)
    testImplementation(libs.opencv.platform)
}