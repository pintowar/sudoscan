package io.github.pintowar.sudoscan.api.cv

import io.github.pintowar.sudoscan.api.ImageMatrix

object CvSpecHelpers {
    fun cvRead(path: String): io.github.pintowar.sudoscan.api.ImageMatrix {
        val cl = Thread.currentThread().contextClassLoader
        val bytes = cl.getResource(path).openStream().readAllBytes()
        return io.github.pintowar.sudoscan.api.ImageMatrix.fromBytes(bytes)
    }

    val sudoku = cvRead("imgs/sudoku01.jpg")
    val preProcessedSudoku = cvRead("imgs/pre-processed-sudoku01.jpg").toGrayScale()
    val croppedSudoku = cvRead("imgs/cropped-sudoku-image01.jpg").toGrayScale()
    val frontalSudoku = cvRead("imgs/frontal-processed-sudoku01.jpg").toGrayScale()
    val dirtyEight = cvRead("imgs/dirty-eight.jpg").toGrayScale()

    val sudokuSolution = cvRead("imgs/sudoku01-sol.jpg").colored()
    val sudokuPerspectiveSolution = cvRead("imgs/sudoku01-perspective-sol.jpg").colored()
    val sudokuFinalSolution = cvRead("imgs/sudoku01-final-sol.jpg")
}