package com.github.pintowar.sudoscan.core

import mu.KLogging
import org.bytedeco.javacpp.indexer.FloatIndexer
import org.bytedeco.javacpp.indexer.IntIndexer
import org.bytedeco.opencv.global.opencv_core.*
import org.bytedeco.opencv.global.opencv_imgproc
import org.bytedeco.opencv.global.opencv_imgproc.COLOR_RGB2GRAY
import org.bytedeco.opencv.opencv_core.Mat
import org.bytedeco.opencv.opencv_core.Point
import kotlin.math.pow
import kotlin.math.sqrt
import com.github.pintowar.sudoscan.core.OpenCvWrapper as cv2

object Parser : KLogging() {

    fun toGrayScale(img: Mat) = cv2.cvtColor(img, COLOR_RGB2GRAY)

    fun preProcessGrayImage(img: Mat, skipDilate: Boolean = false): Mat {
        val proc = cv2.gaussianBlur(img, Pair(9, 9), 0.0)

        val threshold = cv2.adaptiveThreshold(proc, 255.0, opencv_imgproc.ADAPTIVE_THRESH_GAUSSIAN_C,
                opencv_imgproc.THRESH_BINARY, 11, 2.0)

        val thresholdNot = cv2.bitwiseNot(threshold)

        return if (!skipDilate) {
            val kernel = cv2.getStructuringElement(opencv_imgproc.MORPH_DILATE, Pair(3, 3))
            cv2.dilate(thresholdNot, kernel)
        } else thresholdNot
    }

    fun findCorners(img: Mat): ImageCorners {
        val contours = cv2.findContours(img, Mat(), opencv_imgproc.RETR_EXTERNAL, opencv_imgproc.CHAIN_APPROX_SIMPLE)
        val polygon = contours.get().maxBy { cv2.contourArea(it) }!!

        val idx = polygon.createIndexer<IntIndexer>()
        val points = (0 until polygon.size(0)).map {
            Point(idx.get(2L * it), idx.get(2L * it + 1))
        }

        return ImageCorners(
                bottomRight = points.maxBy { it.x() + it.y() }!!,
                topLeft = points.minBy { it.x() + it.y() }!!,
                bottomLeft = points.minBy { it.x() - it.y() }!!,
                topRight = points.maxBy { it.x() - it.y() }!!
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

        val result = cv2.warpPerspective(img, m, Pair(side.toInt(), side.toInt()))
        return CroppedImage(result, src, dst)
    }

    fun splitSquares(img: Mat): List<Pair<Point, Point>> {
        assert(img.arrayHeight() == img.arrayWidth())

        val numPieces = 9
        val side = img.arrayHeight().toDouble() / numPieces

        return (0 until numPieces).flatMap { i ->
            (0 until numPieces).map { j ->
                Point((j * side).toInt(), (i * side).toInt()) to
                        Point(((j + 1) * side).toInt(), ((i + 1) * side).toInt())
            }
        }
    }

    fun scaleAndCenter(img: Mat, size: Int, margin: Int = 0, background: Int = 0): Mat {

        fun centrePad(length: Int): Pair<Int, Int> {
            val side = (size - length) / 2
            return if (length % 2 == 0) side to side else side to (side + 1)
        }

        val isHigher = img.arrayHeight() > img.arrayWidth()
        val ratio = (size - margin).toDouble() / (if (isHigher) img.arrayHeight() else img.arrayWidth())
        val w = (ratio * img.arrayWidth()).toInt()
        val h = (ratio * img.arrayHeight()).toInt()
        val halfMargin = margin / 2

        val (tPad, bPad, lPad, rPad) = if (isHigher) {
            val (l, r) = centrePad(w)
            listOf(halfMargin, halfMargin, l, r)
        } else {
            val (t, b) = centrePad(h)
            listOf(t, b, halfMargin, halfMargin)
        }

        val aux = cv2.resize(img, Pair(w, h))
        val aux2 = cv2.copyMakeBorder(aux, tPad, bPad, lPad, rPad, BORDER_CONSTANT, background.toDouble())
        return cv2.resize(aux2, Pair(size, size))
    }

    fun cutFromRect(img: Mat, s: Pair<Point, Point>) =
            if (s.first.x() <= s.second.x() && s.first.y() <= s.second.y())
                img.colRange(s.first.x(), s.second.x())
                        .rowRange(s.first.y(), s.second.y())
            else img.colRange(0, 0).rowRange(0, 0)

    fun extractDigit(img: Mat, s: Pair<Point, Point>, size: Int): Digit {
        val digit = cutFromRect(img, s)

        val margin = ((digit.arrayWidth() + digit.arrayHeight()) / 2 / 6.0).toInt()

        val noBorder = cutFromRect(digit, Point(margin, margin) to
                Point(digit.arrayWidth() - margin, digit.arrayHeight() - margin))

        val percentFill = (cv2.sumElements(noBorder) / 255) / (noBorder.size(0) * noBorder.size(1))
        logger.debug { "Percent: %.2f".format(percentFill) }

        return if (percentFill >= 0.1) Digit(scaleAndCenter(noBorder, size, 4), false)
        else Digit(Mat.zeros(size, size, CV_8UC1).asMat(), true)
    }

    fun extractAllDigits(img: Mat, squares: List<Pair<Point, Point>>, size: Int = 28) =
            squares.map { s -> extractDigit(img, s, size) }

    private fun floatToMat(data: Array<Array<Float>>): Mat {
        val mat = Mat(data.size, data[0].size, CV_32FC1)
        val idx = mat.createIndexer<FloatIndexer>()
        data.forEachIndexed { i, it ->
            idx.put(2L * i, it[0])
            idx.put(2L * i + 1, it[1])
        }
        return mat
    }

    data class ImageCorners(val topLeft: Point, val topRight: Point, val bottomRight: Point, val bottomLeft: Point) {

        fun sides() = listOf(bottomRight to topRight, topLeft to bottomLeft,
                bottomRight to bottomLeft, topLeft to topRight).map { (a, b) ->
            sqrt((a.x() - b.x()).toDouble().pow(2) + (a.y() - b.y()).toDouble().pow(2))
        }

        fun toFloatArray() = arrayOf(
                arrayOf(topLeft.x().toFloat(), topLeft.y().toFloat()),
                arrayOf(topRight.x().toFloat(), topRight.y().toFloat()),
                arrayOf(bottomRight.x().toFloat(), bottomRight.y().toFloat()),
                arrayOf(bottomLeft.x().toFloat(), bottomLeft.y().toFloat())
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
