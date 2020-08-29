package com.github.pintowar.sudoscan.core

import nu.pattern.OpenCV
import org.datavec.image.loader.NativeImageLoader
import org.nd4j.linalg.api.ndarray.INDArray
import org.opencv.core.*
import org.opencv.highgui.HighGui
import org.opencv.imgcodecs.Imgcodecs
import org.opencv.imgproc.Imgproc
import java.awt.image.BufferedImage
import java.awt.image.DataBufferByte

object OpenCvWrapper {

    init {
        OpenCV.loadShared()
    }

    fun imread(path: String, flag: Int = Imgcodecs.IMREAD_GRAYSCALE) = Imgcodecs.imread(path, flag)

    fun toMat(img: BufferedImage): Mat {
        val data = (img.raster.dataBuffer as DataBufferByte).data
        val mat = Mat(img.height, img.width, CvType.CV_8UC3)
        mat.put(0, 0, data)
        return mat
    }

    fun toImage(mat: Mat, type: Int = BufferedImage.TYPE_3BYTE_BGR): BufferedImage {
        val bytes = ByteArray(mat.rows() * mat.cols() * mat.elemSize().toInt())
        mat.get(0, 0, bytes)
        val image = BufferedImage(mat.cols(), mat.rows(), type)
        image.raster.setDataElements(0, 0, mat.cols(), mat.rows(), bytes)
        return image
    }

    fun toNdArray(mat: Mat): INDArray {
        return NativeImageLoader(mat.height().toLong(), mat.width().toLong()).asMatrix(mat)
                .reshape(mat.height().toLong(), mat.width().toLong(), mat.elemSize())
    }

    fun zeros(width: Double, height: Double, type: Int = CvType.CV_8U) = Mat.zeros(Size(width, height), type)

    fun cvtColor(src: Mat, code: Int): Mat {
        val dst = Mat()
        Imgproc.cvtColor(src, dst, code)
        return dst
    }

    fun gaussianBlur(src: Mat, ksize: Pair<Double, Double>, sigmaX: Double): Mat {
        val dst = Mat()
        Imgproc.GaussianBlur(src, dst, Size(ksize.first, ksize.second), sigmaX)
        return dst
    }

    fun adaptiveThreshold(src: Mat, maxValue: Double, adaptiveMethod: Int, thresholdType: Int, blockSize: Int, C: Double): Mat {
        val dst = Mat()
        Imgproc.adaptiveThreshold(src, dst, maxValue, adaptiveMethod, thresholdType, blockSize, C)
        return dst
    }

    fun bitwiseNot(src: Mat): Mat {
        val dst = Mat()
        Core.bitwise_not(src, dst)
        return dst
    }

    fun bitwiseAnd(src1: Mat, src2: Mat): Mat {
        val dst = Mat()
        Core.bitwise_and(src1, src2, dst)
        return dst
    }

    fun getStructuringElement(shape: Int, ksize: Pair<Double, Double>): Mat {
        return Imgproc.getStructuringElement(shape, Size(ksize.first, ksize.second))
    }

    fun dilate(src: Mat, kernel: Mat): Mat {
        val dst = Mat()
        Imgproc.dilate(src, dst, kernel)
        return dst
    }

    fun findContours(image: Mat, hierarchy: Mat, mode: Int, method: Int): List<MatOfPoint> {
        val contours = mutableListOf<MatOfPoint>()
        Imgproc.findContours(image, contours, hierarchy, mode, method)
        return contours.toList()
    }

    fun contourArea(contour: Mat): Double {
        return Imgproc.contourArea(contour)
    }

    fun getPerspectiveTransform(src: Mat, dst: Mat): Mat {
        return Imgproc.getPerspectiveTransform(src, dst)
    }

    fun warpPerspective(src: Mat, M: Mat, dsize: Pair<Double, Double>): Mat {
        val dst = Mat()
        Imgproc.warpPerspective(src, dst, M, Size(dsize.first, dsize.second))
        return dst
    }

    fun resize(src: Mat, dsize: Pair<Double, Double>): Mat {
        val dst = Mat()
        Imgproc.resize(src, dst, Size(dsize.first, dsize.second))
        return dst
    }

    fun copyMakeBorder(src: Mat, top: Int, bottom: Int, left: Int, right: Int, borderType: Int, value: Double): Mat {
        val dst = Mat()
        Core.copyMakeBorder(src, dst, top, bottom, left, right, borderType, Scalar(value))
        return dst
    }

    fun sumElements(src: Mat) = Core.sumElems(src).`val`.sum()

    fun destroy() {
        HighGui.destroyAllWindows()
    }

}