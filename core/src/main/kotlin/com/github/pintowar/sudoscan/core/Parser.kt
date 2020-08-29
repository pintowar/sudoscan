package com.github.pintowar.sudoscan.core

import mu.KLogging
import org.opencv.core.Core
import org.opencv.core.CvType
import org.opencv.core.Mat
import org.opencv.core.Point
import org.opencv.imgproc.Imgproc

import kotlin.math.pow
import kotlin.math.sqrt
import com.github.pintowar.sudoscan.core.OpenCvWrapper as cv2

object Parser : KLogging() {

    fun toGrayScale(img: Mat) = cv2.cvtColor(img, Imgproc.COLOR_RGB2GRAY)

    fun preProcessGrayImage(img: Mat, skipDilate: Boolean = false): Mat {
        val proc = cv2.gaussianBlur(img, Pair(9.0, 9.0), 0.0)
//        val proc = cv2.gaussianBlur(img, Pair(7.0, 7.0), 3.0)

        val threshold = cv2.adaptiveThreshold(proc, 255.0, Imgproc.ADAPTIVE_THRESH_GAUSSIAN_C,
                Imgproc.THRESH_BINARY, 11, 2.0)

        val thresholdNot = cv2.bitwiseNot(threshold)

        return if (!skipDilate) {
            val kernel = cv2.getStructuringElement(Imgproc.MORPH_DILATE, Pair(3.0, 3.0))
            cv2.dilate(thresholdNot, kernel)
        } else thresholdNot
    }

    fun findCorners(img: Mat): ImageCorners {
        val contours = cv2.findContours(img, Mat(), Imgproc.RETR_EXTERNAL, Imgproc.CHAIN_APPROX_SIMPLE)
        val polygon = contours.maxBy { cv2.contourArea(it) }!!.toList()

        return ImageCorners(
                bottomRight = polygon.maxBy { it.x + it.y }!!,
                topLeft = polygon.minBy { it.x + it.y }!!,
                bottomLeft = polygon.minBy { it.x - it.y }!!,
                topRight = polygon.maxBy { it.x - it.y }!!
        )
    }

    fun cropSudoku(img: Mat, corners: ImageCorners): CroppedImage {
        val sides = corners.sides()
        val side = sides.max()!!.toFloat()

        val src = floatToMat(corners.toFloatArray())
        val dst = floatToMat(arrayOf(
                arrayOf(0.0f, 0.0f), arrayOf(side - 1, 0.0f), arrayOf(side - 1, side - 1), arrayOf(0.0f, side - 1)
        ))
        val m = cv2.getPerspectiveTransform(src, dst)

        val result = cv2.warpPerspective(img, m, Pair(side.toDouble(), side.toDouble()))
        return CroppedImage(result, src, dst)
    }

    fun splitSquares(img: Mat): List<Pair<Point, Point>> {
        assert(img.height() == img.width())

        val numPieces = 9
        val side = img.height().toDouble() / numPieces

        return (0 until numPieces).flatMap { i ->
            (0 until numPieces).map { j ->
                Point(i * side, j * side) to Point((i + 1) * side, (j + 1) * side)
            }
        }
    }

    fun scaleAndCenter(img: Mat, size: Int, margin: Int = 0, background: Int = 0): Mat {

        fun centrePad(length: Int): Pair<Int, Int> {
            val side = (size - length) / 2
            return if (length % 2 == 0) side to side else side to (side + 1)
        }

        val isHigher = img.height() > img.width()
        val ratio = (size - margin).toDouble() / (if (isHigher) img.height() else img.width())
        val w = (ratio * img.width()).toInt()
        val h = (ratio * img.height()).toInt()
        val halfMargin = margin / 2

        val (tPad, bPad, lPad, rPad) = if (isHigher) {
            val (l, r) = centrePad(w)
            listOf(halfMargin, halfMargin, l, r)
        } else {
            val (t, b) = centrePad(h)
            listOf(t, b, halfMargin, halfMargin)
        }

        val aux = cv2.resize(img, Pair(w.toDouble(), h.toDouble()))
        val aux2 = cv2.copyMakeBorder(aux, tPad, bPad, lPad, rPad, Core.BORDER_CONSTANT, background.toDouble())
        return cv2.resize(aux2, Pair(size.toDouble(), size.toDouble()))
    }

    fun cutFromRect(img: Mat, s: Pair<Point, Point>) =
            if (s.first.x <= s.second.x && s.first.y <= s.second.y)
                img.submat(s.first.x.toInt(), s.second.x.toInt(),
                        s.first.y.toInt(), s.second.y.toInt())
            else img.submat(0, 0, 0, 0)

    fun extractDigit(img: Mat, s: Pair<Point, Point>, size: Int): Digit {
        val digit = cutFromRect(img, s)
        val margin = ((digit.width() + digit.height()) / 2 / 6.0).toInt()

        val noBorder = cutFromRect(digit, Point(margin.toDouble(), margin.toDouble()) to
                Point((digit.width() - margin).toDouble(), (digit.height() - margin).toDouble()))

        val percentFill = (cv2.sumElements(noBorder) / 255) / (noBorder.size(0) * noBorder.size(1))
        logger.debug { "Percent: %.2f".format(percentFill) }

        return if (percentFill >= 0.1) Digit(scaleAndCenter(noBorder, size, 4), false)
        else Digit(Mat.zeros(size, size, CvType.CV_8UC1), true)
    }

    fun extractAllDigits(img: Mat, squares: List<Pair<Point, Point>>, size: Int = 28) =
            squares.map { s -> extractDigit(img, s, size) }

    private fun floatToMat(data: Array<Array<Float>>): Mat {
        val mat = Mat(data.size, data[0].size, CvType.CV_32FC1)
        mat.put(0, 0, data.flatten().toFloatArray())
        return mat
    }

    data class ImageCorners(val topLeft: Point, val topRight: Point, val bottomRight: Point, val bottomLeft: Point) {

        fun sides() = listOf(bottomRight to topRight, topLeft to bottomLeft,
                bottomRight to bottomLeft, topLeft to topRight).map { (a, b) ->
            sqrt((a.x - b.x).pow(2) + (a.y - b.y).pow(2))
        }

        fun toFloatArray() = arrayOf(
                arrayOf(topLeft.x.toFloat(), topLeft.y.toFloat()),
                arrayOf(topRight.x.toFloat(), topRight.y.toFloat()),
                arrayOf(bottomRight.x.toFloat(), bottomRight.y.toFloat()),
                arrayOf(bottomLeft.x.toFloat(), bottomLeft.y.toFloat())
        )

    }

    fun cropImage(img: Mat): CroppedImage {
        val gray = toGrayScale(img)
        val proc = preProcessGrayImage(gray)
        val corners = findCorners(proc)
        return cropSudoku(gray, corners)
    }

    data class CroppedImage(val img: Mat, val src: Mat, val dst: Mat)

    data class Digit(val data: Mat, val empty: Boolean)
}
