import Libs.Nd4j.implementNd4j

plugins {
    id("sudoscan.kotlin-kotest")
    id("java-library")
}

description = "Sudoscan Nd4j"

dependencies {
    api(projects.sudoscanCore)
    implementNd4j()
}
