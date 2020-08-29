package com.github.pintowar.sudoscan.core

import mu.KLogging
import org.opencv.core.CvType
import org.opencv.core.Mat
import org.opencv.core.Point
import org.opencv.core.Scalar
import org.opencv.imgproc.Imgproc
import java.awt.Color
import kotlin.math.ceil
import com.github.pintowar.sudoscan.core.OpenCvWrapper as cv2

object Plotter : KLogging() {

    fun plotSolution(image: Parser.CroppedImage, solution: List<Int>, color: Color = Color.green): Mat {
        val base = image.img
        val squareImage = cv2.zeros(base.height().toDouble(), base.width().toDouble(), CvType.CV_8UC3)

        val factor = base.size(0) / 9
        val fSize = 1.0

        var x = 0
        var y = -1

        val font = Imgproc.FONT_HERSHEY_SIMPLEX
        val textColor = Scalar(255.0 - color.red, 255.0 - color.green, 255.0 - color.blue)

        solution.forEach {
            if (x % 9 == 0) {
                x = 0
                y += 1
            }
            val textX = ceil(factor * x + factor / 2.0 - 15)
            val textY = ceil(factor * y + factor / 2.0 + factor / 3.0)

            if (it != 0) {
                logger.debug { "$x, $y : $textX | $textY" }
                Imgproc.putText(squareImage, "$it", Point(textX, textY), font, fSize, textColor, 4)
            }
            x += 1
        }

        return squareImage
    }

    fun changePerspectiveToOriginal(dst: Mat, src: Mat, sudokuResult: Mat, original: Mat): Mat {
        val m = cv2.getPerspectiveTransform(dst, src)
        var img = cv2.warpPerspective(sudokuResult, m, (original.size(1).toDouble() to original.size(0).toDouble()))
        img = cv2.bitwiseNot(img)
        return cv2.bitwiseAnd(img, original)
    }

    fun plotResultOnOriginalSource(original: Mat, cropped: Parser.CroppedImage, solution: List<Int>,
                                   color: Color = Color.green): Mat {
        val result = plotSolution(cropped, solution, color)
        return changePerspectiveToOriginal(cropped.dst, cropped.src, result, original)
    }
}