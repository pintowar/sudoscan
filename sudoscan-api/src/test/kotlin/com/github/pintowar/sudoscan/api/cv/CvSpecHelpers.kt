package com.github.pintowar.sudoscan.api.cv

import org.bytedeco.opencv.global.opencv_imgcodecs
import org.bytedeco.opencv.opencv_core.Mat
import java.io.File

object CvSpecHelpers {
    fun cvRead(path: String, gray: Boolean = false): Mat {
        val flag = if (gray) opencv_imgcodecs.IMREAD_GRAYSCALE else opencv_imgcodecs.IMREAD_COLOR
        val cl = Thread.currentThread().contextClassLoader
        val filename = File(cl.getResource(path)!!.toURI()).absolutePath
        return opencv_imgcodecs.imread(filename, flag)
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