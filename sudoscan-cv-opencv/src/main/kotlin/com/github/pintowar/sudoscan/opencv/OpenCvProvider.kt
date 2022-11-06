package com.github.pintowar.sudoscan.opencv

import com.github.pintowar.sudoscan.api.ImageMatrix
import com.github.pintowar.sudoscan.api.cv.Area
import com.github.pintowar.sudoscan.api.spi.ImageProvider
import org.bytedeco.opencv.global.opencv_core.CV_8UC1
import org.bytedeco.opencv.global.opencv_core.CV_8UC3

class OpenCvProvider : ImageProvider {

    override fun fromBytes(image: ByteArray): ImageMatrix = OpenCvMatrix(image)

    override fun empty(area: Area, mono: Boolean): ImageMatrix =
        OpenCvMatrix(zeros(area, if (mono) CV_8UC1 else CV_8UC3))
}