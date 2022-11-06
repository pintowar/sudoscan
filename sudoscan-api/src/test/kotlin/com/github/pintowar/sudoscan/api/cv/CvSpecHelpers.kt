package com.github.pintowar.sudoscan.api.cv

import com.github.pintowar.sudoscan.api.ImageMatrix
import java.io.File

object CvSpecHelpers {
    fun cvRead(path: String, gray: Boolean = false): ImageMatrix {
        val cl = Thread.currentThread().contextClassLoader
        val filename = File(cl.getResource(path)!!.toURI()).absolutePath
        return ImageMatrix.fromFile(filename, gray)
    }

    val sudoku = cvRead("imgs/sudoku01.jpg")
    val preProcessedSudoku = cvRead("imgs/pre-processed-sudoku01.jpg", true)
    val croppedSudoku = cvRead("imgs/cropped-sudoku-image01.jpg", true)
    val frontalSudoku = cvRead("imgs/frontal-processed-sudoku01.jpg", true)
    val dirtyEight = cvRead("imgs/dirty-eight.jpg", true)

    val sudokuSolution = cvRead("imgs/sudoku01-sol.jpg")
    val sudokuPerspectiveSolution = cvRead("imgs/sudoku01-perspective-sol.jpg")
    val sudokuFinalSolution = cvRead("imgs/sudoku01-final-sol.jpg")
}