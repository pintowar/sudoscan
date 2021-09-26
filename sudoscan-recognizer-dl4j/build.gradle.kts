import Libs.Dl4j.implementDl4j
import Libs.JavaCv.testImplementOpenCv

plugins {
    id("sudoscan.kotlin-publish")
    id("java-library")
}

description = "Sudoscan Recognizer Dl4j"

dependencies {
    api(projects.sudoscanApi)
    implementDl4j()
    testImplementOpenCv()
}