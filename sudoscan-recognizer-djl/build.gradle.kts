plugins {
    id("sudoscan.kotlin-publish")
    id("java-library")
}

description = "Sudoscan Recognizer DJL"

dependencies {
    api(projects.sudoscanApi)
    implementation(libs.djl.zoo)
    runtimeOnly(libs.djl.tensorflow)

    testImplementation(libs.opencv)
}