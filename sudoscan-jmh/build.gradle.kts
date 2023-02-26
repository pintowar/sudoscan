plugins {
    id("sudoscan.kotlin-base")
    id("me.champeau.jmh")
}

dependencies {
    implementation(projects.sudoscanSolverChoco)
    implementation(projects.sudoscanSolverOjalgo)
}