package com.github.pintowar.sudoscan.djl

import ai.djl.ndarray.NDArray
import ai.djl.ndarray.NDManager
import ai.djl.ndarray.types.DataType
import ai.djl.ndarray.types.Shape
import org.bytedeco.javacpp.indexer.UByteIndexer
import org.bytedeco.opencv.opencv_core.Mat

fun Mat.toNDArray(manager: NDManager): NDArray {
    val width = this.arrayWidth().toLong()
    val height = this.arrayHeight().toLong()
    val channels = this.channels().toLong()

    val bb = manager.allocateDirect((channels * height * width).toInt())
    this.createIndexer<UByteIndexer>().use { idx ->
        for (c in 0 until channels) {
            for (h in 0 until height) {
                for (w in 0 until width) {
                    bb.put(idx.get(h, w, c).toByte())
                }
            }
        }
    }
    bb.rewind()
    return manager.create(bb, Shape(height, width, channels), DataType.UINT8)
}