plugins {
    id("sudoscan.kotlin-publish")
    id("java-library")
}

description = "Sudoscan Recognizer DJL"

dependencies {
    api(projects.sudoscanApi)
    implementation(libs.recognizer.djl.zoo)
    runtimeOnly(libs.recognizer.djl.native)
    testImplementation(projects.sudoscanCvOpencv)
}