package com.github.pintowar.sudoscan.api.cv

import org.bytedeco.javacpp.Loader
import org.bytedeco.opencv.global.opencv_core.*
import org.bytedeco.opencv.global.opencv_imgcodecs
import org.bytedeco.opencv.global.opencv_imgproc
import org.bytedeco.opencv.opencv_core.Mat
import org.bytedeco.opencv.opencv_core.MatVector
import org.bytedeco.opencv.opencv_core.Scalar
import org.opencv.imgcodecs.Imgcodecs

internal val isNotAndroid = !Loader.getPlatform().startsWith("android")

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

internal fun Mat.crop(bBox: BBox): Mat {
    return if (bBox.isNotEmpty())
        Mat(this, bBox.toRect())
    else
        throw IllegalArgumentException("Bounding Box is empty.")
}

internal fun Mat.area() = Area(this.arrayWidth(), this.arrayHeight())

internal fun Mat.sumElements() = sumElems(this).get()