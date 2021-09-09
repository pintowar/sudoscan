package com.github.pintowar.sudoscan.dl4j

import com.github.pintowar.sudoscan.dl4j.loader.NativeImageLoader
import org.bytedeco.opencv.opencv_core.Mat
import org.nd4j.linalg.api.ndarray.INDArray

fun Mat.toNdArray(): INDArray {
    return NativeImageLoader(
        this.arrayHeight().toLong(),
        this.arrayWidth().toLong(),
        this.channels().toLong()
    )
        .asMatrix(this).reshape(this.arrayHeight().toLong(), this.arrayWidth().toLong(), this.elemSize())
}