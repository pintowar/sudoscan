plugins {
    id("sudoscan.kotlin-publish")
    id("java-library")
    id("java-test-fixtures")
}

description = "Sudoscan Core"

dependencies {
    implementation(libs.caffeine)
    testImplementation(projects.sudoscanCvOpencv)
}