import Libs.Djl.implementDjl
import Libs.JavaCv.testImplementOpenCv

plugins {
    id("sudoscan.kotlin-kotest")
    id("java-library")
}

description = "Sudoscan Recognizer DJL"

dependencies {
    api(projects.sudoscanApi)
    implementDjl()
    testImplementOpenCv()
}