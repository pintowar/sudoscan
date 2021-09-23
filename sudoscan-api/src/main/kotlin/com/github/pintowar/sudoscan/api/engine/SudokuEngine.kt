package com.github.pintowar.sudoscan.api.engine

import com.github.benmanes.caffeine.cache.Caffeine
import com.github.pintowar.sudoscan.api.cv.Extractor.cropImage
import com.github.pintowar.sudoscan.api.cv.Extractor.extractAllDigits
import com.github.pintowar.sudoscan.api.cv.Extractor.preProcessGrayImage
import com.github.pintowar.sudoscan.api.cv.Extractor.splitSquares
import com.github.pintowar.sudoscan.api.cv.Plotter.changePerspectiveToOriginalSize
import com.github.pintowar.sudoscan.api.cv.Plotter.combineSolutionToOriginal
import com.github.pintowar.sudoscan.api.cv.Plotter.plotSolution
import com.github.pintowar.sudoscan.api.cv.bytesToMat
import com.github.pintowar.sudoscan.api.cv.matToBytes
import com.github.pintowar.sudoscan.api.spi.Recognizer
import com.github.pintowar.sudoscan.api.spi.Solver
import mu.KLogging
import org.bytedeco.opencv.opencv_core.Mat
import java.awt.Color
import java.time.Duration

class SudokuEngine(private val recognizer: Recognizer, private val solver: Solver) : KLogging() {

    private val cache = Caffeine
        .newBuilder()
        .expireAfterWrite(Duration.ofMinutes(1))
        .build { it: List<Int>? ->
            if (it != null) solvePuzzle(it) else emptyList()
        }

    fun solveAndCombineSolution(bytes: ByteArray, color: Color = Color.GREEN, type: String = ".jpg"): ByteArray {
        val mat = bytes.bytesToMat()
        val sol = solveAndCombineSolution(mat, color)
        return sol.matToBytes(type)
    }

    fun solveAndCombineSolution(img: Mat, color: Color = Color.GREEN): Mat {
        val sol = solve(img, color)
        return if (sol != null) {
            combineSolutionToOriginal(img, sol)
        } else img
    }

    private fun solve(img: Mat, color: Color = Color.GREEN) = try {
        val squareSize = 28

        val cropped = cropImage(img)
        val processedCrop = preProcessGrayImage(cropped.img, false)

        val squares = splitSquares(processedCrop)
        val cells = extractAllDigits(processedCrop, squares, squareSize)
        val digits = recognizer.predict(cells)
        if (digits.sum() > 0) {
            val solution = cache.get(digits)!!
            val result = plotSolution(cropped, solution, color)
            if (solution.isNotEmpty()) changePerspectiveToOriginalSize(cropped.dst, cropped.src, result, img)
            else null
        } else null
    } catch (e: Exception) {
        logger.trace(e) { "Problem found during solution!" }
        null
    }

    private fun solvePuzzle(digits: List<Int>): List<Int> {
        fun printableSol(prob: List<Int>) = prob.chunked(9).joinToString("\n") {
            it.joinToString("|").replace("0", " ")
        }

        val solution = solver.solve(digits, false)
        logger.debug { "Digital Sudoku:\n" + printableSol(digits) }
        logger.debug { "Solution:\n" + printableSol(solution) }
        return solution
    }

}
