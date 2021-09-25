import Libs.Caffeine.implementCaffeine
import Libs.JavaCv.implementOpenCv

plugins {
    id("sudoscan.kotlin-kotest")
    id("java-library")
}

description = "Sudoscan Core"

dependencies {
    implementCaffeine()
    implementOpenCv()
}