plugins {
    id("sudoscan.kotlin-publish")
    id("java-library")
    id("me.champeau.jmh")
}

description = "Sudoscan Solver OjAlgo"

dependencies {
    api(projects.sudoscanApi)
    implementation(libs.solver.ojalgo)
}