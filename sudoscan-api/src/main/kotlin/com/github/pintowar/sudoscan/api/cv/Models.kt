package com.github.pintowar.sudoscan.api.cv

import org.bytedeco.opencv.opencv_core.Mat
import org.bytedeco.opencv.opencv_core.Point
import org.bytedeco.opencv.opencv_core.Rect
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
 * Represents a Bounding Box.
 *
 * @property origin the topLeft Coordinate
 * @property width
 * @property height
 */
internal data class BBox(val origin: Coordinate, val width: Int, val height: Int) {

    constructor(x: Int, y: Int, width: Int, height: Int) : this(Coordinate(x, y), width, height)

    /**
     * Checks if bounding box is not empty.
     */
    fun isNotEmpty() = width > 0 || height > 0

    /**
     * Converts to OpnCV Rect.
     */
    fun toRect() = Rect(origin.x, origin.y, width, height)
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
     * The bounding box of that square.
     */
    fun bBox() = BBox(topLeft, bottomRight.x - topLeft.x, bottomRight.y - topLeft.y)

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