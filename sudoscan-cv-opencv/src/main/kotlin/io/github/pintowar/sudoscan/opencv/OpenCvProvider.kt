package io.github.pintowar.sudoscan.opencv

import io.github.pintowar.sudoscan.api.GrayMatrix
import io.github.pintowar.sudoscan.api.ImageMatrix
import io.github.pintowar.sudoscan.api.cv.Area
import io.github.pintowar.sudoscan.api.spi.ImageProvider
import org.bytedeco.opencv.global.opencv_core.CV_8UC1

class OpenCvProvider : ImageProvider {

    override fun fromBytes(image: ByteArray, grayscale: Boolean): ImageMatrix =
        if (grayscale) GrayCvMatrix(image) else ColorCvMatrix(image)

    override fun emptyGray(area: Area): GrayMatrix = GrayCvMatrix(zeros(area, CV_8UC1))
}