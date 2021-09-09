import Libs.Djl.implementDjl

plugins {
    id("sudoscan.kotlin-kotest")
    id("java-library")
}

description = "Sudoscan Recognizer DJL"

dependencies {
    api(projects.sudoscanApi)
    implementDjl()
}
