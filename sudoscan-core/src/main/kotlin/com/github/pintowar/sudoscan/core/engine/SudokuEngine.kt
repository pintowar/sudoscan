package com.github.pintowar.sudoscan.core.engine

import com.github.pintowar.sudoscan.core.Extractor.cropImage
import com.github.pintowar.sudoscan.core.Extractor.extractAllDigits
import com.github.pintowar.sudoscan.core.Extractor.preProcessGrayImage
import com.github.pintowar.sudoscan.core.Extractor.splitSquares
import com.github.pintowar.sudoscan.core.Plotter.changePerspectiveToOriginalSize
import com.github.pintowar.sudoscan.core.Plotter.plotSolution
import com.github.pintowar.sudoscan.core.spi.Recognizer
import com.github.pintowar.sudoscan.core.spi.Solver
import com.google.common.cache.CacheBuilder
import com.google.common.cache.CacheLoader.from
import mu.KLogging
import org.bytedeco.opencv.opencv_core.Mat
import java.awt.Color
import java.time.Duration

class SudokuEngine(private val recognizer: Recognizer, private val solver: Solver) {

    companion object : KLogging()

    private val cache = CacheBuilder
        .newBuilder()
        .expireAfterWrite(Duration.ofMinutes(1))
        .build(from { it: List<Int>? ->
            if (it != null) solve(it) else emptyList()
        })

    fun solve(img: Mat, color: Color = Color.GREEN) = try {
        val squareSize = 28

        val cropped = cropImage(img)
        val processedCrop = preProcessGrayImage(cropped.img, true)

        val squares = splitSquares(processedCrop)
        val cells = extractAllDigits(processedCrop, squares, squareSize)
        val digits = recognizer.predict(cells)
        if (digits.sum() > 0) {
            val solution = cache.get(digits)
            val result = plotSolution(cropped, solution, color)
            if (solution.isNotEmpty()) changePerspectiveToOriginalSize(cropped.dst, cropped.src, result, img)
            else null
        } else null
    } catch (e: Exception) {
        logger.trace(e) { "Problem found during solution!" }
        null
    }

    fun solve(digits: List<Int>): List<Int> {
        fun printableSol(prob: List<Int>) = prob.chunked(9).joinToString("\n") {
            it.joinToString("|").replace("0", " ")
        }

        val solution = solver.solve(digits, false)
        logger.debug { "Digital Sudoku:\n" + printableSol(digits) }
        logger.debug { "Solution:\n" + printableSol(solution) }
        return solution
    }

}
