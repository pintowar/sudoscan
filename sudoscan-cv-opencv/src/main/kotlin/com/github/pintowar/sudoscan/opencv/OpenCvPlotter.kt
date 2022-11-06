package com.github.pintowar.sudoscan.opencv

import com.github.pintowar.sudoscan.api.Digit
import com.github.pintowar.sudoscan.api.ImageMatrix
import com.github.pintowar.sudoscan.api.Puzzle
import com.github.pintowar.sudoscan.api.cv.Area
import com.github.pintowar.sudoscan.api.cv.FrontalPerspective
import com.github.pintowar.sudoscan.api.cv.Plotter
import mu.KLogging
import org.bytedeco.opencv.global.opencv_core.CV_8UC3
import org.bytedeco.opencv.global.opencv_imgproc.FONT_HERSHEY_DUPLEX
import org.bytedeco.opencv.global.opencv_imgproc.putText
import org.bytedeco.opencv.opencv_core.Point
import org.bytedeco.opencv.opencv_core.Scalar
import java.awt.Color
import kotlin.math.ceil

object OpenCvPlotter : Plotter<ImageMatrix>, KLogging() {

    /**
     * Use a frontal image of a sudoku puzzle + a given solution to plot a solution. The generated solution is an image
     * with the number plotted only on empty cells.
     *
     * @param image the frontal image of a sudoku puzzle.
     * @param solution a given puzzle solution.
     * @param solutionColor the color of the solution digits to be plotted.
     * @param recognizedColor the color of the recognized digits to be plotted.
     */
    override fun plotSolution(
        image: FrontalPerspective<ImageMatrix>,
        solution: Puzzle,
        solutionColor: Color,
        recognizedColor: Color
    ): ImageMatrix {
        val base = image.img
        val squareImage = zeros(Area(base.width(), base.height()), CV_8UC3)

        val factor = (base as OpenCvMatrix).mat.size(0) / 9
        val fSize = base.height() / 350.0

        val font = FONT_HERSHEY_DUPLEX
        val solColor = solutionColor.let {
            Scalar((255.0 - it.blue), (255.0 - it.green), (255.0 - it.red), (255.0 - it.alpha))
        }
        val predictedColor = recognizedColor.let {
            Scalar((255.0 - it.blue), (255.0 - it.green), (255.0 - it.red), (255.0 - it.alpha))
        }

        (0 until solution.gridSize).forEach { i ->
            (0 until solution.gridSize).forEach { j ->
                val textX = ceil(factor * j + factor / 2.0 - 15).toInt()
                val textY = ceil(factor * i + factor / 2.0 + factor / 3.0).toInt()
                val color = if (solution[i, j] is Digit.Found) solColor else predictedColor
                val digit = if (solution[i, j] is Digit.Found) "Found" else "Valid"

                logger.debug { "$digit(${solution[i, j].value}) ($i, $j) : $textX | $textY" }
                putText(squareImage, "${solution[i, j].value}", Point(textX, textY), font, fSize, color)
            }
        }

        return OpenCvMatrix(squareImage)
    }

    /**
     * This function changes the perspective of the result generated by [plotSolution] (a solution based on the frontal
     * view of the puzzle) to the same angle (perspective) of the original image.
     *
     * @param frontal cropped image of the original image, containing the view of a frontal solution.
     * @param sudokuResult the image with the solution generated by [plotSolution].
     * @param original the original image area.
     */
    override fun changePerspectiveToOriginalSize(
        frontal: FrontalPerspective<ImageMatrix>,
        sudokuResult: ImageMatrix,
        originalArea: Area
    ): OpenCvMatrix {
        val m = (frontal.dst as OpenCvMatrix).mat.getPerspectiveTransform((frontal.src as OpenCvMatrix).mat)
        val img = (sudokuResult as OpenCvMatrix).mat.warpPerspective(m, originalArea)
        return OpenCvMatrix(img.bitwiseNot())
    }

    /**
     * Plot the solution on the original perspective (generated by [changePerspectiveToOriginalSize]) and merge it with
     * the original image.
     *
     * @param original the original and not pre-processed image.
     * @param solution the solution image on the original perspective.
     * @return the final solution plotted on the original image.
     */
    override fun combineSolutionToOriginal(original: ImageMatrix, solution: ImageMatrix): OpenCvMatrix {
        return OpenCvMatrix((solution as OpenCvMatrix).mat.bitwiseAnd((original as OpenCvMatrix).mat))
    }
}