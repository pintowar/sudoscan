package com.github.pintowar.sudoscan.api

import org.bytedeco.javacpp.indexer.UByteIndexer
import org.bytedeco.opencv.opencv_core.Mat

class SudokuCell(val data: Mat, val empty: Boolean) {
    val width = data.arrayWidth().toLong()
    val height = data.arrayHeight().toLong()
    val channels = data.channels().toLong()

    fun scanMatrix(callBack: (idx: CellIndex, value: Int) -> Unit) {
        data.createIndexer<UByteIndexer>().use { idxr ->
            for (c in 0 until channels) {
                for (h in 0 until height) {
                    for (w in 0 until width) {
                        callBack(CellIndex(w,h,c), idxr.get(h, w, c))
                    }
                }
            }
        }
    }

    data class CellIndex(val width: Long, val height: Long, val channels: Long)
}