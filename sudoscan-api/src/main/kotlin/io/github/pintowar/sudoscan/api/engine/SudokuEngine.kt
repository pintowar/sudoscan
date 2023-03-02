package io.github.pintowar.sudoscan.api.engine

import com.github.benmanes.caffeine.cache.Caffeine
import io.github.pintowar.sudoscan.api.ImageMatrix
import io.github.pintowar.sudoscan.api.Puzzle
import io.github.pintowar.sudoscan.api.cv.Extractor.extractPuzzleCells
import io.github.pintowar.sudoscan.api.cv.Extractor.preProcessPhases
import io.github.pintowar.sudoscan.api.cv.Plotter.changePerspectiveToOriginalSize
import io.github.pintowar.sudoscan.api.cv.Plotter.combineSolutionToOriginal
import io.github.pintowar.sudoscan.api.cv.Plotter.plotSolution
import io.github.pintowar.sudoscan.api.spi.Recognizer
import io.github.pintowar.sudoscan.api.spi.Solver
import mu.KLogging
import java.awt.Color
import java.awt.image.BufferedImage
import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream
import java.time.Duration
import javax.imageio.ImageIO

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
    private fun solveWithCache(puzzle: Puzzle): Puzzle = cache.get(puzzle.describe()) {
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
     * The main function responsible to glue all components (CV, Recognizer and Solver) to read an image,
     * identify a Sudoku puzzle, recognize the visible  numbers, solve the puzzle and plot back the solution
     * to the original image.
     *
     * This function throws no Exception, however in case of any failure it will return the original input image.
     *
     * In case of debug, a [debugScale] value must be informed. This will generate a mosaic if images of different
     * phases of the solution and resize the final image with the scale provided.
     *
     * @param image ByteArray of the input image.
     * @param solutionColor the color of solution digits to be plotted on solution.
     * @param recognizedColor the color of recognized digits to be plotted on solution.
     * @param ext the extension (jpg, png, etc...) of the output image.
     * @param debugScale if any provided, it will generate a mosaic if images of different phases of the solution.
     * @return final solution as ByteArray.
     */
    fun solveAndCombineSolution(
        image: ByteArray,
        solutionColor: Color = Color.GREEN,
        recognizedColor: Color = Color.RED,
        ext: String = "jpg",
        debugScale: Double = 1.0
    ): ByteArray {
        val mat = ImageMatrix.fromBytes(image)
        val sol = solveAndCombineSolution(mat, solutionColor, recognizedColor, debugScale)
        return sol.toBytes(ext)
    }

    /**
     * The main function responsible to glue all components (CV, Recognizer and Solver) to read an image,
     * identify a Sudoku puzzle, recognize the visible  numbers, solve the puzzle and plot back the solution
     * to the original image.
     *
     * This function throws no Exception, however in case of any failure it will return the original input image.
     *
     * In case of debug, a [debugScale] value must be informed. This will generate a mosaic if images of different
     * phases of the solution and resize the final image with the scale provided.
     *
     * @param image BufferedImage of the input image.
     * @param solutionColor the color of solution digits to be plotted on solution.
     * @param recognizedColor the color of recognized digits to be plotted on solution.
     * @param ext the extension (jpg, png, etc...) of the output image.
     * @param debugScale if any provided, it will generate a mosaic if images of different phases of the solution.
     * @return final solution as BufferedImage.
     */
    fun solveAndCombineSolution(
        image: BufferedImage,
        solutionColor: Color = Color.GREEN,
        recognizedColor: Color = Color.RED,
        ext: String = "jpg",
        debugScale: Double = 1.0
    ): BufferedImage {
        val bytes = ByteArrayOutputStream().also { ImageIO.write(image, ext, it) }.toByteArray()
        val sol = solveAndCombineSolution(bytes, solutionColor, recognizedColor, ext, debugScale)
        return ByteArrayInputStream(sol).let { ImageIO.read(it) }
    }

    /**
     * The main function responsible to glue all components (CV, Recognizer and Solver) to read an image,
     * identify a Sudoku puzzle, recognize the visible  numbers, solve the puzzle and plot back the solution
     * to the original image.
     *
     * This function throws no Exception, however in case of any failure it will return the original input image.
     *
     * In case of debug, a [debugScale] value must be informed. This will generate a mosaic if images of different
     * phases of the solution and resize the final image with the scale provided.
     *
     * @param image Mat of the input image.
     * @param solutionColor the color of solution digits to be plotted on solution.
     * @param recognizedColor the color of recognized digits to be plotted on solution.
     * @param debugScale if any provided, it will generate a mosaic if images of different phases of the solution.
     * @return final solution as Mat.
     */
    private fun solveAndCombineSolution(
        image: ImageMatrix,
        solutionColor: Color = Color.GREEN,
        recognizedColor: Color = Color.RED,
        debugScale: Double = 1.0
    ): ImageMatrix {
        val isDebug = debugScale > 1.0
        val solution = solve(image, solutionColor, recognizedColor, isDebug)
        return if (!isDebug) solution.last() else {
            val rows = solution.asSequence()
                .map { it.resize(image.area()) }
                .chunked(solution.size / 2)
                .map { it.reduce { acc, mat -> acc.concat(mat) } }

            rows.reduce { acc, mat -> acc.concat(mat, false) }.resize(image.area() * debugScale)
        }
    }

    /**
     * The main function responsible to glue all components (CV, Recognizer and Solver) to read an image,
     * identify a Sudoku puzzle, recognize the visible numbers, solve the puzzle and plot back the solution
     * to the original image.
     *
     * This function returns a list of images from different phases during the solution process. Where the first image
     * is the original image and the last is the final solution (if possible, otherwise returns the original image).
     *
     * @param image Mat of the input image.
     * @param solutionColor the color of solution digits to be plotted on solution.
     * @param recognizedColor the color of recognized digits to be plotted on solution.
     * @return a list of images from different phases during the solution process.
     */
    private fun solve(
        image: ImageMatrix,
        solutionColor: Color = Color.GREEN,
        recognizedColor: Color = Color.RED,
        debug: Boolean = false
    ): List<ImageMatrix> {
        val prePhases = preProcessPhases(image)
        val cropped = prePhases.frontal
        val processedCrop = cropped.img.preProcessGrayImage(false)
        val noGrid = processedCrop.removeGrid()

        val puzzleCells = extractPuzzleCells(noGrid)
        val cleanImage = puzzleCells.toMat(debug)

        return try {
            val puzzle = puzzleCells.toUnsolvedPuzzle(recognizer)
            val finalSolution = when (val solution = solveWithCache(puzzle)) {
                is Puzzle.Unsolved -> image
                is Puzzle.Solved -> {
                    val result = plotSolution(cropped.frontalArea(), solution, solutionColor, recognizedColor)
                    val sol = changePerspectiveToOriginalSize(cropped, result, image.area())
                    combineSolutionToOriginal(image, sol)
                }
            }

            listOf(
                image, prePhases.preProcessedGrayImage, processedCrop, noGrid, cleanImage, finalSolution
            )
        } catch (e: Exception) {
            logger.trace(e) { "Problem found during solution!" }
            listOf(image, prePhases.preProcessedGrayImage, processedCrop, noGrid, cleanImage, image)
        }
    }
}