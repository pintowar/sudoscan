package com.github.pintowar.sudoscan.opencv

import com.github.pintowar.sudoscan.api.cv.Area
import com.github.pintowar.sudoscan.api.cv.BBox
import com.github.pintowar.sudoscan.api.cv.Coordinate
import org.bytedeco.javacpp.Loader
import org.bytedeco.opencv.global.opencv_core.*
import org.bytedeco.opencv.global.opencv_imgcodecs
import org.bytedeco.opencv.global.opencv_imgproc
import org.bytedeco.opencv.opencv_core.*
import org.opencv.imgcodecs.Imgcodecs

internal val isNotAndroid: Boolean = !Loader.getPlatform().lowercase().startsWith("android")

internal fun zeros(area: Area, type: Int = CV_8U): Mat = Mat.zeros(Size(area.width, area.height), type).asMat()

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
    if (horizontal) hconcat(this, mat, dst) else vconcat(this, mat, dst)
}

internal fun Mat.colored(): Mat = if (this.channels() == 1) Mat().also {
    merge(MatVector(this, this, this), it)
} else this

internal fun Mat.gaussianBlur(area: Area, sigmaX: Double): Mat = Mat().also { dst ->
    opencv_imgproc.GaussianBlur(this, dst, Size(area.width, area.height), sigmaX)
}

internal fun Mat.adaptiveThreshold(
    maxValue: Double,
    adaptiveMethod: Int,
    thresholdType: Int,
    blockSize: Int,
    c: Double
): Mat = Mat().also { dst ->
    opencv_imgproc.adaptiveThreshold(this, dst, maxValue, adaptiveMethod, thresholdType, blockSize, c)
}

internal fun Mat.floodFill(seed: Coordinate, newVal: Double): Int =
    opencv_imgproc.floodFill(this, Point(seed.x, seed.y), Scalar(newVal))

internal fun Mat.floodRect(extRect: BBox, scalar: Double = 255.0): Unit =
    Rect(extRect.origin.x, extRect.origin.y, extRect.width, extRect.height).let { rect ->
        opencv_imgproc.rectangle(this, rect, Scalar(scalar), -1, opencv_imgproc.LINE_8, 0)
    }

internal fun Mat.bitwiseNot(): Mat = Mat().also { dst -> bitwise_not(this, dst) }

internal fun Mat.bitwiseAnd(that: Mat): Mat = Mat().also { dst -> bitwise_and(this, that, dst) }

internal fun getStructuringElement(shape: Int, area: Area): Mat {
    return opencv_imgproc.getStructuringElement(shape, Size(area.width, area.height))
}

internal fun Mat.subtract(other: Mat): Mat = Mat().also { dst -> subtract(this, other, dst) }

internal fun Mat.dilate(kernel: Mat): Mat = Mat().also { dst -> opencv_imgproc.dilate(this, dst, kernel) }

internal fun Mat.erode(kernel: Mat): Mat = Mat().also { dst -> opencv_imgproc.erode(this, dst, kernel) }

internal fun Mat.findContours(hierarchy: Mat, mode: Int, method: Int): Array<Mat> = MatVector().also { contours ->
    opencv_imgproc.findContours(this, contours, hierarchy, mode, method)
}.get()

internal fun Mat.contourArea(): Double = opencv_imgproc.contourArea(this)

internal fun Mat.getPerspectiveTransform(dst: Mat): Mat = opencv_imgproc.getPerspectiveTransform(this, dst)

internal fun Mat.warpPerspective(m: Mat, area: Area): Mat = Mat().also { dst ->
    opencv_imgproc.warpPerspective(this, dst, m, Size(area.width, area.height))
}

internal fun Mat.resize(area: Area): Mat = Mat().also { dst ->
    opencv_imgproc.resize(this, dst, Size(area.width, area.height))
}

internal fun Mat.copyMakeBorder(top: Int, bottom: Int, left: Int, right: Int, borderType: Int, value: Double): Mat {
    return Mat().also { dst ->
        copyMakeBorder(this, dst, top, bottom, left, right, borderType, Scalar(value))
    }
}

internal fun Mat.crop(bBox: BBox): Mat {
    return if (bBox.isNotEmpty())
        Mat(this, Rect(bBox.origin.x, bBox.origin.y, bBox.width, bBox.height))
    else
        throw IllegalArgumentException("Bounding Box is empty.")
}

internal fun Mat.norm(dst: Mat): Double = norm(this, dst)

internal fun Mat.area(): Area = Area(this.arrayWidth(), this.arrayHeight())

internal fun Mat.countNonZero(): Int = countNonZero(this)