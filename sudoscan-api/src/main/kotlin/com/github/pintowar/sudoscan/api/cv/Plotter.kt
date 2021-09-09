package com.github.pintowar.sudoscan.api.cv

import com.github.pintowar.sudoscan.api.CroppedImage
import mu.KLogging
import org.bytedeco.opencv.global.opencv_core.CV_8UC3
import org.bytedeco.opencv.global.opencv_imgproc.FONT_HERSHEY_DUPLEX
import org.bytedeco.opencv.global.opencv_imgproc.putText
import org.bytedeco.opencv.opencv_core.Mat
import org.bytedeco.opencv.opencv_core.Point
import org.bytedeco.opencv.opencv_core.Scalar
import java.awt.Color
import kotlin.math.ceil
import com.github.pintowar.sudoscan.api.cv.OpenCvWrapper as cv2

internal object Plotter : KLogging() {

    fun plotSolution(image: CroppedImage, solution: List<Int>, color: Color = Color.GREEN): Mat {
        val base = image.img
        val squareImage = cv2.zeros(base.arrayHeight(), base.arrayWidth(), CV_8UC3)

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

    fun changePerspectiveAndPasteToOriginal(dst: Mat, src: Mat, sudokuResult: Mat, original: Mat): Mat {
        val sol = changePerspectiveToOriginalSize(dst, src, sudokuResult, original)
        return combineSolutionToOriginal(original, sol)
    }

    fun changePerspectiveToOriginalSize(dst: Mat, src: Mat, sudokuResult: Mat, original: Mat): Mat {
        val m = cv2.getPerspectiveTransform(dst, src)
        val img = cv2.warpPerspective(sudokuResult, m, (original.size(1) to original.size(0)))
        return cv2.bitwiseNot(img)
    }

    fun combineSolutionToOriginal(original: Mat, solution: Mat): Mat {
        return cv2.bitwiseAnd(solution, original)
    }

    fun plotResultOnOriginalSource(original: Mat, cropped: CroppedImage, solution: List<Int>,
                                   color: Color = Color.GREEN): Mat {
        val result = plotSolution(cropped, solution, color)
        return changePerspectiveAndPasteToOriginal(cropped.dst, cropped.src, result, original)
    }
}