plugins {
    id("sudoscan.kotlin-publish")
    id("java-library")
}

description = "Sudoscan Recognizer Dl4j"

dependencies {
    api(projects.sudoscanApi)
    implementation(libs.nd4j) {
        exclude(group = "org.bytedeco", module = "mkl-platform")
    }
    implementation(libs.dl4j) {
        exclude(group = "org.bytedeco", module = "hdf5-platform")
    }
    implementation(libs.hdf5)

    testImplementation(libs.opencv)
}