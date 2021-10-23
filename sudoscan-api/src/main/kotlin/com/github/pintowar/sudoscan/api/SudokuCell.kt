package com.github.pintowar.sudoscan.api

import com.github.pintowar.sudoscan.api.cv.Area
import com.github.pintowar.sudoscan.api.cv.Extractor.toGrayScale
import com.github.pintowar.sudoscan.api.cv.bitwiseNot
import com.github.pintowar.sudoscan.api.cv.isNotAndroid
import com.github.pintowar.sudoscan.api.cv.resize
import org.bytedeco.javacpp.indexer.UByteIndexer
import org.bytedeco.opencv.opencv_core.Mat

/**
 * This class represents the image (and shape metadata) of a Sudoku cell.
 * This must be a grayscale (1 channel) and squared (width = height) image.
 *
 * @param mat image in Mat (OpenCV) format.
 * @property empty flag that indicates if a cell is empty (without number)
 */
class SudokuCell(mat: Mat, val empty: Boolean) {
    private val cellSize = 28
    private val data = mat.resize(Area(cellSize, cellSize)).let {
        (if (it.channels() > 1) toGrayScale(it) else it).bitwiseNot()
    }
    val width = data.arrayWidth().toLong()
    val height = data.arrayHeight().toLong()
    val channels = data.channels().toLong()

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