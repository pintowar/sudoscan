package com.github.pintowar.sudoscan.opencv

import com.github.pintowar.sudoscan.api.ImageMatrix
import com.github.pintowar.sudoscan.api.cv.Area
import com.github.pintowar.sudoscan.api.spi.ImageProvider
import org.bytedeco.opencv.global.opencv_core.CV_8UC1
import org.bytedeco.opencv.global.opencv_core.CV_8UC3
import org.bytedeco.opencv.global.opencv_imgcodecs

class OpenCvProvider : ImageProvider {

    override fun fromBytes(image: ByteArray): ImageMatrix = OpenCvMatrix(image)

    override fun fromFile(src: String, mono: Boolean): ImageMatrix {
        val codec = if (mono) opencv_imgcodecs.IMREAD_GRAYSCALE else opencv_imgcodecs.IMREAD_COLOR
        return OpenCvMatrix(opencv_imgcodecs.imread(src, codec))
    }

    override fun empty(area: Area, mono: Boolean): ImageMatrix =
        OpenCvMatrix(zeros(area, if (mono) CV_8UC1 else CV_8UC3))
}