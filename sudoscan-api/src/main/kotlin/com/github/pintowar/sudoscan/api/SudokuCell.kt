package com.github.pintowar.sudoscan.api

import com.github.pintowar.sudoscan.api.cv.*
import com.github.pintowar.sudoscan.api.cv.Extractor.scaleAndCenter
import com.github.pintowar.sudoscan.api.cv.Extractor.toGrayScale
import org.bytedeco.javacpp.indexer.UByteIndexer
import org.bytedeco.opencv.global.opencv_core
import org.bytedeco.opencv.opencv_core.Mat

/**
 * This class represents the image (and shape metadata) of a Sudoku cell.
 * This must be a grayscale (1 channel) and squared (width = height) image.
 *
 * @param mat image in Mat (OpenCV) format.
 */
class SudokuCell(mat: Mat) {
    private val centeredEmpty = scaleAndCenter(mat)
    private val data = revertColors(centeredEmpty.first)

    val isEmpty = centeredEmpty.second
    val width = data.arrayWidth().toLong()
    val height = data.arrayHeight().toLong()
    val channels = data.channels().toLong()

    companion object {
        const val CELL_SIZE = 28
        val EMPTY = SudokuCell(zeros(Area(CELL_SIZE), opencv_core.CV_8UC1))
    }

    /**
     * Checks if the informed image has more than 10% of data or else it assumes it's an empty cell.
     */
    private fun scaleAndCenter(cleanedImage: Mat): Pair<Mat, Boolean> {
        val area = cleanedImage.area().value()
        val percentFill = if (area > 0) (cleanedImage.sumElements() / 255) / area else 0.0

        return if (percentFill > 0.1)
            scaleAndCenter(cleanedImage, CELL_SIZE, CELL_SIZE / 7) to false
        else
            zeros(Area(CELL_SIZE), opencv_core.CV_8UC1) to true
    }

    /**
     * Transform image from black-white to white-black.
     */
    private fun revertColors(mat: Mat): Mat {
        return (if (mat.channels() > 1) toGrayScale(mat) else mat).bitwiseNot()
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