package com.github.pintowar.sudoscan.api.cv

import org.bytedeco.opencv.opencv_core.Mat
import org.bytedeco.opencv.opencv_core.Point
import org.bytedeco.opencv.opencv_core.Size
import kotlin.math.pow
import kotlin.math.sqrt

/**
 * Represents the area of an image.
 *
 * @property width
 * @property height
 */
internal data class Area(val width: Int, val height: Int) {

    constructor(size: Int) : this(size, size)

    /**
     * Converts to OpenCV Size object.
     */
    fun toSize() = Size(width, height)

    fun value() = width * height

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

    fun height() = end.y - begin.y

    fun width() = end.x - begin.x
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