package com.github.pintowar.sudoscan.api

import com.github.pintowar.sudoscan.api.cv.isNotAndroid
import org.bytedeco.javacpp.indexer.UByteIndexer
import org.bytedeco.opencv.opencv_core.Mat

/**
 * This class represents the image (and shape metadata) of a Sudoku cell.
 * This must be a grayscale (1 channel) and squared (width = height) image.
 *
 * @property data image in Mat (OpenCV) format.
 * @property empty flag that indicates if a cell is empty (without number)
 */
class SudokuCell(private val data: Mat, val empty: Boolean) {
    val width = data.arrayWidth().toLong()
    val height = data.arrayHeight().toLong()
    val channels = data.channels().toLong()

    init {
        if (channels > 1) throw IllegalArgumentException("Number fo channels must be 1.")
        if (width != height) throw IllegalArgumentException("Sudoku Cell must be a square (in size).")
    }

    /**
     * This function encapsulates the full scan of the image (height, width, channel) and uses a void callback function
     * to process avery point of the image. This is useful to change the Mat (OpenCV) format to other formats.
     *
     * @param callBack the void function to process every data of the image.
     */
    fun scanMatrix(callBack: (idx: CellIndex, value: Int) -> Unit) {
        data.createIndexer<UByteIndexer>(isNotAndroid).use { idx ->
            for (c in 0 until channels) {
                for (h in 0 until height) {
                    for (w in 0 until width) {
                        callBack(CellIndex(w, h, c), idx.get(h, w, c))
                    }
                }
            }
        }
    }

    data class CellIndex(val width: Long, val height: Long, val channels: Long)
}