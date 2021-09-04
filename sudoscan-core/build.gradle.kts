import Libs.ChocoSolver.implementChocoSolver
import Libs.Guava.apiGuava
import Libs.JavaCv.apiJavaCv
import Libs.JavaCv.apiOpenCv

plugins {
    id("sudoscan.kotlin-kotest")
    id("java-library")
}

description = "Sudoscan Core"

dependencies {
    implementChocoSolver()
    apiGuava()
    apiJavaCv()
    apiOpenCv()
}
