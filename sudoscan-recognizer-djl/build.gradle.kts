import Libs.Djl.implementDjl
import Libs.JavaCv.testImplementOpenCv

plugins {
    id("sudoscan.kotlin-publish")
    id("java-library")
}

description = "Sudoscan Recognizer DJL"

dependencies {
    api(projects.sudoscanApi)
    implementDjl()
    testImplementOpenCv()
}