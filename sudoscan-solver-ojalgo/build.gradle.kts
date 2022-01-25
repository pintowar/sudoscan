plugins {
    id("sudoscan.kotlin-publish")
    id("java-library")
}

description = "Sudoscan Solver OjAlgo"

dependencies {
    api(projects.sudoscanApi)
    implementation(libs.ojalgo)
}