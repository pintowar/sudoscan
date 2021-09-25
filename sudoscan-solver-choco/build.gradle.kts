import Libs.ChocoSolver.implementChocoSolver

plugins {
    id("sudoscan.kotlin-kotest")
    id("java-library")
}

description = "Sudoscan Solver Choco"

dependencies {
    api(projects.sudoscanApi)
    implementChocoSolver()
}