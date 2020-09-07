package com.github.pintowar.sudoscan.core

import com.github.pintowar.sudoscan.NativeImageLoader
import org.bytedeco.javacv.Java2DFrameUtils
import org.bytedeco.opencv.global.opencv_core.*
import org.bytedeco.opencv.global.opencv_imgcodecs
import org.bytedeco.opencv.global.opencv_imgproc
import org.bytedeco.opencv.opencv_core.*
import org.nd4j.linalg.api.ndarray.INDArray
import java.awt.image.BufferedImage

object OpenCvWrapper {

    fun imread(path: String, flag: Int = opencv_imgcodecs.IMREAD_GRAYSCALE) = opencv_imgcodecs.imread(path, flag)

    fun toMat(img: BufferedImage) = Java2DFrameUtils.toMat(img)

    fun toImage(mat: Mat) = Java2DFrameUtils.toBufferedImage(mat)

    fun toFrame(mat: Mat) = Java2DFrameUtils.toFrame(mat)

    fun toNdArray(mat: Mat): INDArray {
        return NativeImageLoader(mat.arrayHeight().toLong(), mat.arrayWidth().toLong()).asMatrix(mat)
                .reshape(mat.arrayHeight().toLong(), mat.arrayWidth().toLong(), mat.elemSize())
    }

    fun zeros(width: Int, height: Int, type: Int = CV_8U) = Mat.zeros(Size(width, height), type).asMat()

    fun cvtColor(src: Mat, code: Int): Mat {
        val dst = Mat()
        opencv_imgproc.cvtColor(src, dst, code)
        return dst
    }

    fun gaussianBlur(src: Mat, ksize: Pair<Int, Int>, sigmaX: Double): Mat {
        val dst = Mat()
        opencv_imgproc.GaussianBlur(src, dst, Size(ksize.first, ksize.second), sigmaX)
        return dst
    }

    fun adaptiveThreshold(src: Mat, maxValue: Double, adaptiveMethod: Int, thresholdType: Int, blockSize: Int, C: Double): Mat {
        val dst = Mat()
        opencv_imgproc.adaptiveThreshold(src, dst, maxValue, adaptiveMethod, thresholdType, blockSize, C)
        return dst
    }

    fun floodFill(img: Mat, seed: Pair<Int, Int>, newVal: Double) =
            opencv_imgproc.floodFill(img, Point(seed.first, seed.second), Scalar(newVal))

    fun bitwiseNot(src: Mat): Mat {
        val dst = Mat()
        bitwise_not(src, dst)
        return dst
    }

    fun bitwiseAnd(src1: Mat, src2: Mat): Mat {
        val dst = Mat()
        bitwise_and(src1, src2, dst)
        return dst
    }

    fun getStructuringElement(shape: Int, ksize: Pair<Int, Int>): Mat {
        return opencv_imgproc.getStructuringElement(shape, Size(ksize.first, ksize.second))
    }

    fun dilate(src: Mat, kernel: Mat): Mat {
        val dst = Mat()
        opencv_imgproc.dilate(src, dst, kernel)
        return dst
    }

    fun findContours(image: Mat, hierarchy: Mat, mode: Int, method: Int): MatVector {
        val contours = MatVector()
        opencv_imgproc.findContours(image, contours, hierarchy, mode, method)
        return contours
    }

    fun contourArea(contour: Mat): Double {
        return opencv_imgproc.contourArea(contour)
    }

    fun getPerspectiveTransform(src: Mat, dst: Mat): Mat {
        return opencv_imgproc.getPerspectiveTransform(src, dst)
    }

    fun warpPerspective(src: Mat, M: Mat, dsize: Pair<Int, Int>): Mat {
        val dst = Mat()
        opencv_imgproc.warpPerspective(src, dst, M, Size(dsize.first, dsize.second))
        return dst
    }

    fun resize(src: Mat, dsize: Pair<Int, Int>): Mat {
        val dst = Mat()
        opencv_imgproc.resize(src, dst, Size(dsize.first, dsize.second))
        return dst
    }

    fun copyMakeBorder(src: Mat, top: Int, bottom: Int, left: Int, right: Int, borderType: Int, value: Double): Mat {
        val dst = Mat()
        copyMakeBorder(src, dst, top, bottom, left, right, borderType, Scalar(value))
        return dst
    }

    fun sumElements(src: Mat) = sumElems(src).get()

}