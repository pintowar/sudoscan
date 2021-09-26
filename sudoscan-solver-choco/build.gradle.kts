import Libs.ChocoSolver.implementChocoSolver

plugins {
    id("sudoscan.kotlin-publish")
    id("java-library")
}

description = "Sudoscan Solver Choco"

dependencies {
    api(projects.sudoscanApi)
    implementChocoSolver()
}