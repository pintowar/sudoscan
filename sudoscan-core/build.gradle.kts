import Libs.ChocoSolver.implementChocoSolver
import Libs.JavaCv.apiJavaCv
import Libs.Nd4j.implementNd4j

plugins {
    id("sudoscan.kotlin-javacpp")
    id("java-library")
}

description = "Sudoscan Core"

dependencies {
    implementChocoSolver()
    implementNd4j()
    apiJavaCv()
}
