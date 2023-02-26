plugins {
    id("sudoscan.kotlin-publish")
    id("java-library")
}

description = "Sudoscan OpenCV"

dependencies {
    api(projects.sudoscanApi)
    implementation(libs.opencv.platform)
    testImplementation(testFixtures(projects.sudoscanApi))
}