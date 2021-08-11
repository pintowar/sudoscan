package com.github.pintowar.sudoscan.nd4j

import org.bytedeco.opencv.opencv_core.Mat
import org.bytedeco.opencv.opencv_core.Point
import kotlin.math.pow
import kotlin.math.sqrt

data class ImageCorners(val topLeft: Point, val topRight: Point, val bottomRight: Point, val bottomLeft: Point) {

    companion object {
        val EMPTY_CORNERS = ImageCorners(Point(0, 0), Point(0, 0), Point(0, 0), Point(0, 0))
    }

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

    fun isEmptyCorners() = this == EMPTY_CORNERS
}

data class CroppedImage(val img: Mat, val src: Mat, val dst: Mat)

data class Digit(val data: Mat, val empty: Boolean)