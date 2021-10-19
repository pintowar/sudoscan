package com.github.pintowar.sudoscan.api.engine

import com.github.benmanes.caffeine.cache.Caffeine
import com.github.pintowar.sudoscan.api.Puzzle
import com.github.pintowar.sudoscan.api.cv.Extractor.cropImage
import com.github.pintowar.sudoscan.api.cv.Extractor.extractAllDigits
import com.github.pintowar.sudoscan.api.cv.Extractor.preProcessGrayImage
import com.github.pintowar.sudoscan.api.cv.Extractor.splitSquares
import com.github.pintowar.sudoscan.api.cv.Plotter.changePerspectiveToOriginalSize
import com.github.pintowar.sudoscan.api.cv.Plotter.combineSolutionToOriginal
import com.github.pintowar.sudoscan.api.cv.Plotter.plotSolution
import com.github.pintowar.sudoscan.api.cv.area
import com.github.pintowar.sudoscan.api.cv.bytesToMat
import com.github.pintowar.sudoscan.api.cv.matToBytes
import com.github.pintowar.sudoscan.api.spi.Recognizer
import com.github.pintowar.sudoscan.api.spi.Solver
import mu.KLogging
import org.bytedeco.opencv.opencv_core.Mat
import java.awt.Color
import java.time.Duration

/**
 * Main class responsible for the complete solution of the puzzle. This class is responsible for glue all components
 * (CV, Recognizer and Solver) to read an image, identify a Sudoku puzzle, recognize the visible  numbers, solve the
 * puzzle and plot back the solution to the original image.
 *
 * @property recognizer recognizer implementation to be used on the pipe.
 * @property solver solver implementation to be used on the pipe.
 */
class SudokuEngine(private val recognizer: Recognizer, private val solver: Solver) : KLogging() {

    /**
     * Cache to maintain a solution already solved. This cache expires in 5 minutes.
     * After the expiration the puzzle will be solver again.
     */
    private val cache = Caffeine
        .newBuilder()
        .expireAfterWrite(Duration.ofMinutes(5))
        .build<String, Puzzle>()

    /**
     * Returns puzzle solution if key (puzzle encoded description) is found in cache.
     * Otherwise, uses solver to solve puzzle and puts the solution on cache associated with puzzle key.
     *
     * @param puzzle puzzle to be solved
     * @return puzzle solution
     */
    private fun solveWithCache(puzzle: Puzzle) = cache.get(puzzle.describe()) {
        logger.debug { "Digital Sudoku:\n ${puzzle.describe(false)}" }
        val solution = solver.solve(puzzle)
        logger.debug { "Solution:\n ${solution.describe(false)}" }
        solution
    }

    /**
     * Description of Recognizer and Solver component names.
     */
    fun components() = "${recognizer.name} / ${solver.name}"

    /**
     * This function uses a byte array representing the input and output solution.
     * It's a wrap of the [solve] function (the function of the entire pipe).
     *
     * @param image byte array of the input image.
     * @param solutionColor the color of solution digits to be plotted on solution.
     * @param recognizedColor the color of recognized digits to be plotted on solution.
     * @param ext the extension (jpg, png, etc...) of the output image.
     * @return final solution as byte array.
     */
    fun solveAndCombineSolution(
        image: ByteArray,
        solutionColor: Color = Color.GREEN,
        recognizedColor: Color = Color.RED,
        ext: String = ".jpg"
    ): ByteArray {
        val mat = image.bytesToMat()
        val sol = solveAndCombineSolution(mat, solutionColor, recognizedColor)
        return sol.matToBytes(ext)
    }

    /**
     * This function uses a Mat (from OpenCV) representing the input and output solution.
     * It's a wrap of the [solveAndCombineSolution] function (the function of the entire pipe).
     *
     * @param image Mat of the input image.
     * @param solutionColor the color of solution digits to be plotted on solution.
     * @param recognizedColor the color of recognized digits to be plotted on solution.
     * @return final solution as Mat.
     */
    fun solveAndCombineSolution(
        image: Mat,
        solutionColor: Color = Color.GREEN,
        recognizedColor: Color = Color.RED
    ): Mat {
        val sol = solve(image, solutionColor, recognizedColor)
        return if (sol != null) {
            combineSolutionToOriginal(image, sol)
        } else image
    }

    /**
     * The main function responsible to glue all components (CV, Recognizer and Solver) to read an image,
     * identify a Sudoku puzzle, recognize the visible  numbers, solve the puzzle and plot back the solution
     * to the original image.
     *
     * This function throws no Exception, however in case of any failure it will return the original input image.
     *
     * @param image Mat of the input image.
     * @param solutionColor the color of solution digits to be plotted on solution.
     * @param recognizedColor the color of recognized digits to be plotted on solution.
     * @return final solution as Mat.
     */
    private fun solve(image: Mat, solutionColor: Color = Color.GREEN, recognizedColor: Color = Color.RED) = try {
        val squareSize = 28

        val cropped = cropImage(image)
        val processedCrop = preProcessGrayImage(cropped.img, false)

        val squares = splitSquares(processedCrop)
        val cells = extractAllDigits(processedCrop, squares, squareSize)
        val digits = recognizer.reliablePredict(cells)
        val puzzle = Puzzle.Unsolved(digits)
        if (puzzle.isValid()) {
            when (val solution = solveWithCache(puzzle)) {
                is Puzzle.Unsolved -> null
                is Puzzle.Solved -> {
                    val result = plotSolution(cropped, solution, solutionColor, recognizedColor)
                    changePerspectiveToOriginalSize(cropped, result, image.area())
                }
                else -> null
            }
        } else null
    } catch (e: Exception) {
        logger.trace(e) { "Problem found during solution!" }
        null
    }
}