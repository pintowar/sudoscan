package com.github.pintowar.sudoscan.api.cv

import com.github.pintowar.sudoscan.api.Digit
import mu.KLogging
import org.bytedeco.javacpp.indexer.FloatIndexer
import org.bytedeco.javacpp.indexer.IntIndexer
import org.bytedeco.javacpp.indexer.UByteIndexer
import org.bytedeco.opencv.global.opencv_core.*
import org.bytedeco.opencv.global.opencv_imgproc
import org.bytedeco.opencv.global.opencv_imgproc.COLOR_RGB2GRAY
import org.bytedeco.opencv.opencv_core.Mat
import org.bytedeco.opencv.opencv_core.Point
import kotlin.math.max
import kotlin.math.min

internal object Extractor : KLogging() {

    fun toGrayScale(img: Mat) = img.cvtColor(COLOR_RGB2GRAY)

    fun preProcessGrayImage(img: Mat, skipDilate: Boolean = false): Mat {
        assert(img.channels() == 1)
        val proc = img.gaussianBlur(Area(9, 9), 0.0)

        val threshold = proc.adaptiveThreshold(
            255.0, opencv_imgproc.ADAPTIVE_THRESH_GAUSSIAN_C, opencv_imgproc.THRESH_BINARY, 11, 2.0
        )

        val thresholdNot = threshold.bitwiseNot()

        return if (!skipDilate) {
            val kernel = getStructuringElement(opencv_imgproc.MORPH_DILATE, Area(3, 3))
            thresholdNot.dilate(kernel)
        } else thresholdNot
    }

    fun findCorners(img: Mat): ImageCorners {
        val contours = img.findContours(Mat(), opencv_imgproc.RETR_EXTERNAL, opencv_imgproc.CHAIN_APPROX_SIMPLE)
        val polygons = contours.get()

        return if (polygons.isNotEmpty()) {
            val polygon = polygons.maxByOrNull { it.contourArea() }!!
            val points = polygon.createIndexer<IntIndexer>().use { idx ->
                (0 until idx.size(0)).map { Coord(idx.get(it, 0, 0), idx.get(it, 0, 1)) }
            }

            ImageCorners(
                bottomRight = points.maxByOrNull { it.x + it.y }!!,
                topLeft = points.minByOrNull { it.x + it.y }!!,
                bottomLeft = points.minByOrNull { it.x - it.y }!!,
                topRight = points.maxByOrNull { it.x - it.y }!!
            )
        } else ImageCorners.EMPTY_CORNERS
    }

    fun cropSudoku(img: Mat, corners: ImageCorners): CroppedImage {
        val sides = corners.sides()
        val side = sides.maxOrNull()!!.toFloat()

        val src = floatToMat(corners.toFloatArray())
        val dst = floatToMat(
            arrayOf(
                arrayOf(0.0f, 0.0f), arrayOf(side - 1, 0.0f), arrayOf(side - 1, side - 1), arrayOf(0.0f, side - 1)
            )
        )
        val m = src.getPerspectiveTransform(dst)
        val result = img.warpPerspective(m, Area(side.toInt(), side.toInt()))
        return CroppedImage(result, src, dst)
    }

    fun splitSquares(img: Mat): List<Segment> {
        assert(img.arrayHeight() == img.arrayWidth())

        val numPieces = 9
        val side = img.arrayHeight().toDouble() / numPieces

        return (0 until numPieces).flatMap { i ->
            (0 until numPieces).map { j ->
                Segment(
                    Coord((j * side).toInt(), (i * side).toInt()),
                    Coord(((j + 1) * side).toInt(), ((i + 1) * side).toInt())
                )
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

        val aux = img.resize(Area(w, h))
        val aux2 = aux.copyMakeBorder(tPad, bPad, lPad, rPad, BORDER_CONSTANT, background.toDouble())
        return aux2.resize(Area(size, size))
    }

    fun cutFromRect(img: Mat, segment: Segment) =
        if (segment.begin.x <= segment.end.x && segment.begin.y <= segment.end.y)
            img.colRange(segment.begin.x, segment.end.x)
                .rowRange(segment.begin.y, segment.end.y)
        else img.colRange(0, 0).rowRange(0, 0)

    fun extractDigit(img: Mat, segment: Segment, size: Int): Digit {
        val digit = cutFromRect(img, segment)

        val margin = ((digit.arrayWidth() + digit.arrayHeight()) / 5.0).toInt()

        val box = findLargestFeature(
            digit,
            Point(margin, margin), Point(digit.arrayWidth() - margin, digit.arrayHeight() - margin)
        )

        val noBorder = cutFromRect(digit, box.diagonal())

        val dim = noBorder.size(0) * noBorder.size(1)
        val percentFill = if (dim > 0) (noBorder.sumElements() / 255) / dim else 0.0
        logger.debug { "Percent: %.2f".format(percentFill) }

        return if (percentFill > 0.1) Digit(scaleAndCenter(noBorder, size, 4), false)
        else Digit(Mat.zeros(size, size, CV_8UC1).asMat(), true)
    }

    fun findLargestFeature(
        inputImg: Mat, topLeft: Point = Point(0, 0),
        bottomRight: Point = Point(inputImg.arrayWidth(), inputImg.arrayHeight())
    ): ImageCorners {
        val img = inputImg.clone()
        return img.createIndexer<UByteIndexer>().use { indexer ->
            val size = img.size()

            (topLeft.x() until min(bottomRight.x(), size.width())).forEach { x ->
                (topLeft.y() until min(bottomRight.y(), size.height())).forEach { y ->
                    if (indexer[y.toLong(), x.toLong()] == 255) {
                        img.floodFill(Coord(x, y), 64.0)
                    }
                }
            }

            var (top, bottom, left, right) = listOf(size.height(), 0, size.width(), 0)

            (0 until size.width()).forEach { x ->
                (0 until size.height()).forEach { y ->
                    val color = if (indexer[y.toLong(), x.toLong()] != 64) 0 else 255
                    indexer.put(y.toLong(), x.toLong(), color)

                    if (indexer[y.toLong(), x.toLong()] == 255) {
                        top = min(x, top)
                        bottom = max(x, bottom)
                        left = min(y, left)
                        right = max(y, right)
                    }
                }
            }

            ImageCorners(Coord(top, left), Coord(top, right), Coord(bottom, right), Coord(bottom, left))
        }
    }

    fun extractAllDigits(img: Mat, squares: List<Segment>, size: Int = 28) =
        squares.map { s -> extractDigit(img, s, size) }

    private fun floatToMat(data: Array<Array<Float>>): Mat {
        val mat = Mat(data.size, data[0].size, CV_32FC1)
        mat.createIndexer<FloatIndexer>().use { idx ->
            data.forEachIndexed { i, it ->
                idx.put(longArrayOf(i.toLong(), 0, 0), it[0])
                idx.put(longArrayOf(i.toLong(), 1, 0), it[1])
            }
        }
        return mat
    }

    fun cropImage(img: Mat): CroppedImage {
        val gray = toGrayScale(img)
        val proc = preProcessGrayImage(gray)
        val corners = findCorners(proc)
        return cropSudoku(gray, corners)
    }
}
