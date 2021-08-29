import Libs.Djl.implementDjl

plugins {
    id("sudoscan.kotlin-kotest")
    id("java-library")
}

description = "Sudoscan DJL"

dependencies {
    api(projects.sudoscanCore)
    implementDjl()
}
