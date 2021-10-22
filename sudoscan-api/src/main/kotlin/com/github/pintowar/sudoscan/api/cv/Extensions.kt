package com.github.pintowar.sudoscan.api.cv

import org.bytedeco.javacpp.Loader
import org.bytedeco.opencv.global.opencv_core.*
import org.bytedeco.opencv.global.opencv_imgcodecs
import org.bytedeco.opencv.global.opencv_imgproc
import org.bytedeco.opencv.opencv_core.*
import org.opencv.imgcodecs.Imgcodecs
import kotlin.math.pow
import kotlin.math.sqrt

internal val isNotAndroid = !Loader.getPlatform().startsWith("android")

/**
 * Represents the area of an image.
 *
 * @property width
 * @property height
 */
internal data class Area(val width: Int, val height: Int) {

    /**
     * Converts to OpenCV Size object.
     */
    fun toSize() = Size(width, height)

    operator fun times(scale: Double) = Area((width * scale).toInt(), (height * scale).toInt())
}

/**
 * Represents a cartesian coordinate with its x (for width) and y for (height) values.
 *
 * @property x
 * @property y
 */
internal data class Coordinate(val x: Int, val y: Int) {

    constructor(x: Long, y: Long) : this(x.toInt(), y.toInt())

    /**
     * Converts to OpenCV Point object (x, y).
     */
    fun toPoint() = Point(x, y)

    /**
     * Convert this coordinate to a float array (x, y).
     */
    fun toFloatArray() = floatArrayOf(x.toFloat(), y.toFloat())
}

/**
 * Represents a line segment between two coordinates.
 *
 * @property begin
 * @property end
 */
internal data class Segment(val begin: Coordinate, val end: Coordinate) {

    /**
     * Checks if this segment has a backslash (like a '\') format.
     */
    fun isBackSlash() = begin.x <= end.x && begin.y <= end.y
}

/**
 * Represents the four coordinates that forms a selected rectangle of an image.
 * This rectangle is usually the bounds of a detected object.
 */
internal data class RectangleCorners(
    val topLeft: Coordinate,
    val topRight: Coordinate,
    val bottomRight: Coordinate,
    val bottomLeft: Coordinate
) {

    companion object {
        val EMPTY_CORNERS = RectangleCorners(Coordinate(0, 0), Coordinate(0, 0), Coordinate(0, 0), Coordinate(0, 0))
    }

    /**
     * The euclidean distance fo the sides of the rectangle.
     */
    fun sides() = listOf(bottomRight to topRight, topLeft to bottomLeft, bottomRight to bottomLeft, topLeft to topRight)
        .map { (a, b) -> sqrt((a.x - b.x).toDouble().pow(2) + (a.y - b.y).toDouble().pow(2)) }

    /**
     * The diagonal segment of that square.
     */
    fun diagonal() = Segment(topLeft, bottomRight)

    /**
     * Convert to a 2d float array.
     */
    fun toFloatArray() = arrayOf(
        topLeft.toFloatArray(), topRight.toFloatArray(), bottomRight.toFloatArray(), bottomLeft.toFloatArray()
    )
}

/**
 * Represents an image from a frontal perspective.
 */
internal class FrontalPerspective(val img: Mat, val src: Mat, val dst: Mat)

/**
 * Stores image versions on the pre-processing phase
 */
internal class PreProcessPhases(val grayScale: Mat, val preProcessedGrayImage: Mat, val frontal: FrontalPerspective)

internal fun zeros(area: Area, type: Int = CV_8U): Mat = Mat.zeros(area.toSize(), type).asMat()

internal fun ByteArray.bytesToMat(): Mat {
    return opencv_imgcodecs.imdecode(Mat(*this), Imgcodecs.IMREAD_UNCHANGED)
}

internal fun Mat.matToBytes(type: String = "jpg"): ByteArray {
    return ByteArray(this.channels() * this.cols() * this.rows()).also { bytes ->
        opencv_imgcodecs.imencode(".$type", this, bytes)
    }
}

internal fun Mat.cvtColor(code: Int): Mat = Mat().also { dst ->
    opencv_imgproc.cvtColor(this, dst, code)
}

internal fun Mat.concat(mat: Mat, horizontal: Boolean = true): Mat = Mat().also { dst ->
    val a = if (this.channels() < 3) Mat().also { merge(MatVector(this, this, this), it) } else this
    val b = if (mat.channels() < 3) Mat().also { merge(MatVector(mat, mat, mat), it) } else mat
    if (horizontal) hconcat(a, b, dst) else vconcat(a, b, dst)
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

internal fun Mat.floodFill(seed: Coordinate, newVal: Double) =
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