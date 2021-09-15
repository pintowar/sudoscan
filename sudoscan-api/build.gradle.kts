import Libs.Caffeine.implementCaffeine
import Libs.JavaCv.apiOpenCv

plugins {
    id("sudoscan.kotlin-kotest")
    id("java-library")
}

description = "Sudoscan Core"

dependencies {
    implementCaffeine()
    apiOpenCv()
}
