package com.github.pintowar.sudoscan.api.cv

import org.bytedeco.javacpp.Loader
import org.bytedeco.opencv.global.opencv_core.*
import org.bytedeco.opencv.global.opencv_imgcodecs
import org.bytedeco.opencv.global.opencv_imgproc
import org.bytedeco.opencv.opencv_core.*
import org.opencv.imgcodecs.Imgcodecs
import kotlin.math.pow

internal val direct = !Loader.getPlatform().startsWith("android")

internal data class Area(val width: Int, val height: Int) {
    fun toSize() = Size(width, height)
}

internal data class Coord(val x: Int, val y: Int) {
    fun toPoint() = Point(x, y)
}

internal data class Segment(val begin: Coord, val end: Coord) {
    fun isBackSlash() = begin.x <= end.x && begin.y <= end.y
}

internal data class ImageCorners(
    val topLeft: Coord,
    val topRight: Coord,
    val bottomRight: Coord,
    val bottomLeft: Coord
) {

    companion object {
        val EMPTY_CORNERS = ImageCorners(Coord(0, 0), Coord(0, 0), Coord(0, 0), Coord(0, 0))
    }

    fun sides() = listOf(
        bottomRight to topRight, topLeft to bottomLeft,
        bottomRight to bottomLeft, topLeft to topRight
    ).map { (a, b) ->
        kotlin.math.sqrt((a.x - b.x).toDouble().pow(2) + (a.y - b.y).toDouble().pow(2))
    }

    fun diagonal() = Segment(topLeft, bottomRight)

    fun toFloatArray() = arrayOf(
        arrayOf(topLeft.x.toFloat(), topLeft.y.toFloat()),
        arrayOf(topRight.x.toFloat(), topRight.y.toFloat()),
        arrayOf(bottomRight.x.toFloat(), bottomRight.y.toFloat()),
        arrayOf(bottomLeft.x.toFloat(), bottomLeft.y.toFloat())
    )

    fun isEmptyCorners() = this == EMPTY_CORNERS
}

internal class FrontalPerspective(val img: Mat, val src: Mat, val dst: Mat)

internal fun zeros(area: Area, type: Int = CV_8U): Mat = Mat.zeros(area.toSize(), type).asMat()

internal fun ByteArray.bytesToMat(): Mat {
    return opencv_imgcodecs.imdecode(Mat(*this), Imgcodecs.IMREAD_UNCHANGED)
}

internal fun Mat.matToBytes(type: String = ".jpg"): ByteArray {
    return ByteArray(this.channels() * this.cols() * this.rows()).also { bytes ->
        opencv_imgcodecs.imencode(type, this, bytes)
    }
}

internal fun Mat.cvtColor(code: Int): Mat = Mat().also { dst ->
    opencv_imgproc.cvtColor(this, dst, code)
}

internal fun Mat.gaussianBlur(area: Area, sigmaX: Double): Mat = Mat().also { dst ->
    opencv_imgproc.GaussianBlur(this, dst, area.toSize(), sigmaX)
}

internal fun Mat.adaptiveThreshold(
    maxValue: Double,
    adaptiveMethod: Int,
    thresholdType: Int,
    blockSize: Int,
    c: Double
) = Mat().also { dst ->
    opencv_imgproc.adaptiveThreshold(this, dst, maxValue, adaptiveMethod, thresholdType, blockSize, c)
}

internal fun Mat.floodFill(seed: Coord, newVal: Double) =
    opencv_imgproc.floodFill(this, seed.toPoint(), Scalar(newVal))

internal fun Mat.bitwiseNot(): Mat = Mat().also { dst -> bitwise_not(this, dst) }

internal fun Mat.bitwiseAnd(that: Mat): Mat = Mat().also { dst -> bitwise_and(this, that, dst) }

internal fun getStructuringElement(shape: Int, area: Area): Mat {
    return opencv_imgproc.getStructuringElement(shape, area.toSize())
}

internal fun Mat.dilate(kernel: Mat): Mat = Mat().also { dst -> opencv_imgproc.dilate(this, dst, kernel) }

internal fun Mat.findContours(hierarchy: Mat, mode: Int, method: Int): MatVector = MatVector().also { contours ->
    opencv_imgproc.findContours(this, contours, hierarchy, mode, method)
}

internal fun Mat.contourArea(): Double = opencv_imgproc.contourArea(this)

internal fun Mat.getPerspectiveTransform(dst: Mat): Mat = opencv_imgproc.getPerspectiveTransform(this, dst)

internal fun Mat.warpPerspective(m: Mat, area: Area): Mat = Mat().also { dst ->
    opencv_imgproc.warpPerspective(this, dst, m, area.toSize())
}

internal fun Mat.resize(area: Area): Mat = Mat().also { dst ->
    opencv_imgproc.resize(this, dst, area.toSize())
}

internal fun Mat.copyMakeBorder(top: Int, bottom: Int, left: Int, right: Int, borderType: Int, value: Double): Mat {
    return Mat().also { dst ->
        copyMakeBorder(this, dst, top, bottom, left, right, borderType, Scalar(value))
    }
}

internal fun Mat.area() = Area(this.arrayWidth(), this.arrayHeight())

internal fun Mat.sumElements() = sumElems(this).get()