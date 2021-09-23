package com.github.pintowar.sudoscan.api.cv

import mu.KLogging
import org.bytedeco.opencv.global.opencv_core.CV_8UC3
import org.bytedeco.opencv.global.opencv_imgproc.FONT_HERSHEY_DUPLEX
import org.bytedeco.opencv.global.opencv_imgproc.putText
import org.bytedeco.opencv.opencv_core.Mat
import org.bytedeco.opencv.opencv_core.Point
import org.bytedeco.opencv.opencv_core.Scalar
import java.awt.Color
import kotlin.math.ceil

internal object Plotter : KLogging() {

    /**
     * Use a frontal image of a sudoku puzzle + a given solution to plot a solution. The generated solution is an image
     * with the number plotted only on empty cells.
     *
     * @param image the frontal image of a sudoku puzzle.
     * @param solution a given solution.
     * @param color the color of the solution to be plotted.
     */
    fun plotSolution(image: FrontalPerspective, solution: List<Int>, color: Color = Color.GREEN): Mat {
        val base = image.img
        val squareImage = zeros(Area(base.arrayWidth(), base.arrayHeight()), CV_8UC3)

        val factor = base.size(0) / 9
        val fSize = base.arrayHeight() / 350.0

        var x = 0
        var y = -1

        val font = FONT_HERSHEY_DUPLEX
        val textColor = Scalar((255.0 - color.blue), (255.0 - color.green), (255.0 - color.red), 0.0)

        solution.forEach {
            if (x % 9 == 0) {
                x = 0
                y += 1
            }
            val textX = ceil(factor * x + factor / 2.0 - 15).toInt()
            val textY = ceil(factor * y + factor / 2.0 + factor / 3.0).toInt()

            if (it != 0) {
                logger.debug { "$x, $y : $textX | $textY" }
                putText(squareImage, "$it", Point(textX, textY), font, fSize, textColor)
            }
            x += 1
        }

        return squareImage
    }

    /**
     * This function changes the perspective of the result generated by [plotSolution] (a solution based on the frontal
     * view of the puzzle) to the same angle (perspective) of the original image.
     *
     * @param frontal cropped image of the original image, containing the view of a frontal solution.
     * @param sudokuResult the image with the solution generated by [plotSolution].
     * @param original the original image area.
     */
    fun changePerspectiveToOriginalSize(frontal: FrontalPerspective, sudokuResult: Mat, originalArea: Area): Mat {
        val m = frontal.dst.getPerspectiveTransform(frontal.src)
        val img = sudokuResult.warpPerspective(m, originalArea)
        return img.bitwiseNot()
    }

    /**
     * Plot the solution on the original perspective (generated by [changePerspectiveToOriginalSize]) and merge it with
     * the original image.
     *
     * @param original the original and not pre-processed image.
     * @param solution the solution image on the original perspective.
     * @return the final solution plotted on the original image.
     */
    fun combineSolutionToOriginal(original: Mat, solution: Mat): Mat {
        return solution.bitwiseAnd(original)
    }

}