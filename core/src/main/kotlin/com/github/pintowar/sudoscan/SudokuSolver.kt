package com.github.pintowar.sudoscan

import com.github.pintowar.sudoscan.core.Parser.cropImage
import com.github.pintowar.sudoscan.core.Parser.extractAllDigits
import com.github.pintowar.sudoscan.core.Parser.preProcessGrayImage
import com.github.pintowar.sudoscan.core.Parser.splitSquares
import com.github.pintowar.sudoscan.core.Plotter.changePerspectiveToOriginalSize
import com.github.pintowar.sudoscan.core.Plotter.plotResultOnOriginalSource
import com.github.pintowar.sudoscan.core.Plotter.plotSolution
import com.github.pintowar.sudoscan.core.Recognizer
import com.github.pintowar.sudoscan.core.Solver
import mu.KLogging
import org.bytedeco.opencv.opencv_core.Mat
import org.nd4j.shade.guava.cache.CacheBuilder
import org.nd4j.shade.guava.cache.CacheLoader.from
import java.awt.Color
import java.awt.image.BufferedImage
import java.time.Duration
import com.github.pintowar.sudoscan.core.OpenCvWrapper as cv2

class SudokuSolver {

    companion object : KLogging()

    val recognizer = Recognizer()

    val cache = CacheBuilder
            .newBuilder()
            .expireAfterWrite(Duration.ofMinutes(5))
            .build(from { it: List<Int>? ->
                if (it != null) solve(it) else emptyList()
            })

    fun solveAndPasteSolution(img: BufferedImage, color: Color = Color.green): BufferedImage {
        val mat = cv2.toMat(img)
        val sol = solveAndPasteSolution(mat, color)
        return cv2.toImage(sol)
    }

    fun solveAndPasteSolution(img: Mat, color: Color = Color.green) = try {
        val squareSize = 28

        val cropped = cropImage(img)
        val processedCrop = preProcessGrayImage(cropped.img, true)

        val squares = splitSquares(processedCrop)
        val cells = extractAllDigits(processedCrop, squares, squareSize)
        val digits = recognizer.predict(cells)
        val solution = solve(digits)
        if (solution.isNotEmpty()) plotResultOnOriginalSource(img, cropped, solution, color)
        else img
    } catch (e: Exception) {
        logger.trace("Problem found during solution!", e)
        img
    }

    fun solve(img: Mat, color: Color = Color.green) = try {
        val squareSize = 28

        val cropped = cropImage(img)
        val processedCrop = preProcessGrayImage(cropped.img, true)

        val squares = splitSquares(processedCrop)
        val cells = extractAllDigits(processedCrop, squares, squareSize)
        val digits = recognizer.predict(cells)
        val solution = solve(digits)
        val result = plotSolution(cropped, solution, color)
        if (solution.isNotEmpty()) changePerspectiveToOriginalSize(cropped.dst, cropped.src, result, img)
        else null
    } catch (e: Exception) {
        logger.trace("Problem found during solution!", e)
        null
    }

    fun solve(digits: List<Int>): List<Int> {
        logger.debug { "Digital Sudoku: \n" + digits.chunked(9).joinToString("\n") { it.joinToString(" | ") } }
        val solution = Solver.solve(digits, false)
        logger.debug { "Solution: \n" + solution.chunked(9).joinToString("\n") { it.joinToString(" | ") } }
        return solution
    }

}
