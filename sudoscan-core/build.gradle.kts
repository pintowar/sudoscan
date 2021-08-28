import Libs.ChocoSolver.implementChocoSolver

plugins {
    id("sudoscan.kotlin-kotest")
    id("java-library")
}

description = "Sudoscan Core"

dependencies {
    implementChocoSolver()
}
