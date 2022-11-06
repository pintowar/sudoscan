package com.github.pintowar.sudoscan.api.cv

import com.github.pintowar.sudoscan.api.ImageMatrix

object CvSpecHelpers {
    fun cvRead(path: String): ImageMatrix {
        val cl = Thread.currentThread().contextClassLoader
        val bytes = cl.getResource(path).openStream().readAllBytes()
        return ImageMatrix.fromBytes(bytes)
    }

    val sudoku = cvRead("imgs/sudoku01.jpg")
    val preProcessedSudoku = cvRead("imgs/pre-processed-sudoku01.jpg")
    val croppedSudoku = cvRead("imgs/cropped-sudoku-image01.jpg")
    val frontalSudoku = cvRead("imgs/frontal-processed-sudoku01.jpg")
    val dirtyEight = cvRead("imgs/dirty-eight.jpg")

    val sudokuSolution = cvRead("imgs/sudoku01-sol.jpg")
    val sudokuPerspectiveSolution = cvRead("imgs/sudoku01-perspective-sol.jpg")
    val sudokuFinalSolution = cvRead("imgs/sudoku01-final-sol.jpg")
}