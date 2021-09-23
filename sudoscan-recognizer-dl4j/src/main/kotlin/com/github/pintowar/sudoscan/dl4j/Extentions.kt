package com.github.pintowar.sudoscan.dl4j

import org.bytedeco.javacpp.indexer.UByteIndexer
import org.bytedeco.opencv.opencv_core.Mat
import org.nd4j.linalg.api.ndarray.INDArray
import org.nd4j.linalg.factory.Nd4j

fun Mat.toNdArray(): INDArray {
    val width = this.arrayWidth().toLong()
    val height = this.arrayHeight().toLong()
    val channels = this.channels().toLong()

    val ret = Nd4j.create(channels, height, width)
    this.createIndexer<UByteIndexer>().use { idx ->
        for (c in 0 until channels) {
            for (h in 0 until height) {
                for (w in 0 until width) {
                    ret.putScalar(longArrayOf(c, h, w), idx.get(h, w, c))
                }
            }
        }
    }

    return ret.reshape(this.arrayHeight().toLong(), this.arrayWidth().toLong(), this.elemSize())
}
