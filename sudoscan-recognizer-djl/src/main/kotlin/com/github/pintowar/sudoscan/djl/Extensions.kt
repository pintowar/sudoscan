package com.github.pintowar.sudoscan.djl

import ai.djl.ndarray.NDArray
import ai.djl.ndarray.NDManager
import ai.djl.ndarray.types.DataType
import ai.djl.ndarray.types.Shape
import org.bytedeco.javacpp.indexer.UByteRawIndexer
import org.bytedeco.opencv.global.opencv_imgcodecs
import org.bytedeco.opencv.opencv_core.Mat
import kotlin.math.roundToInt

fun Mat.toNDArray(manager: NDManager, flag: Int = opencv_imgcodecs.IMREAD_GRAYSCALE): NDArray {
    val width: Int = this.arrayWidth()
    val height: Int = this.arrayHeight()
    val channel: Int = if (flag == opencv_imgcodecs.IMREAD_GRAYSCALE) 1 else 3

    val bb = manager.allocateDirect(channel * height * width)
    if (this.channels() == 1) {
        val length = width * height
        val data = this.reshape(1, length).createIndexer<UByteRawIndexer>().use { idx ->
            IntArray(length) { idx.get(it.toLong(), 0, 0) }
        }

        for (gray in data) {
            val b = gray.toByte()
            bb.put(b)
            if (flag != opencv_imgcodecs.IMREAD_GRAYSCALE) {
                bb.put(b)
                bb.put(b)
            }
        }
    } else if (this.channels() == 3) {
        val length = width * height * this.channels()
        val pixels = this.reshape(1, length).createIndexer<UByteRawIndexer>().use { idx ->
            IntArray(length) { idx.get(it.toLong(), 0, 0) }
        }

        pixels.asSequence().windowed(this.channels(), this.channels(), false).forEach { (r, g, b) ->
            if (flag == opencv_imgcodecs.IMREAD_GRAYSCALE) {
                val gray = (0.299f * r + 0.587f * g + 0.114f * b).roundToInt()
                bb.put(gray.toByte())
            } else {
                bb.put(r.toByte())
                bb.put(g.toByte())
                bb.put(b.toByte())
            }
        }
    } else {
        throw IllegalArgumentException("Unexpected number of channels.")
    }
    bb.rewind()
    return manager.create(bb, Shape(height.toLong(), width.toLong(), channel.toLong()), DataType.UINT8)
}