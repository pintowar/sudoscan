plugins {
    id("sudoscan.kotlin-publish")
    id("java-library")
}

description = "Sudoscan Solver Choco"

dependencies {
    api(projects.sudoscanApi)
    implementation(libs.solver.choco) {
        exclude(group = "org.knowm.xchart")
        exclude(group = "org.jgrapht")
        exclude(group = "com.github.cp-profiler")
        exclude(group = "dk.brics.automaton")
    }
}