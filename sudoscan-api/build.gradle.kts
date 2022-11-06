plugins {
    id("sudoscan.kotlin-publish")
    id("java-library")
}

description = "Sudoscan Core"

dependencies {
    implementation(libs.caffeine)
    testImplementation(projects.sudoscanCvOpencv)
}