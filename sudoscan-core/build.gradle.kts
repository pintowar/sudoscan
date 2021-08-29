import Libs.JavaCv.apiOpenCv
import Libs.ChocoSolver.implementChocoSolver
import Libs.Guava.apiGuava

plugins {
    id("sudoscan.kotlin-kotest")
    id("java-library")
}

description = "Sudoscan Core"

dependencies {
    implementChocoSolver()
    apiGuava()
    apiOpenCv()
}
