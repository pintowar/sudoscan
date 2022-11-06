package com.github.pintowar.sudoscan.api

import com.github.pintowar.sudoscan.api.cv.Area
import com.github.pintowar.sudoscan.api.cv.CellIndex

/**
 * This class represents the image (and shape metadata) of a Sudoku cell.
 * This must be a grayscale (1 channel) and squared (width = height) image.
 *
 * @param mat image in Mat (OpenCV) format.
 */
class SudokuCell(mat: ImageMatrix) {
    private val centeredEmpty = scaleAndCenter(mat)
    internal val data = centeredEmpty.first.revertColors()

    val isEmpty = centeredEmpty.second
    val width = data.width().toLong()
    val height = data.height().toLong()
    val channels = data.channels().toLong()

    companion object {
        const val CELL_SIZE = 28
        val EMPTY = SudokuCell(ImageMatrix.empty(Area(CELL_SIZE)))
    }

    /**
     * Checks if the informed image has more than 10% of data or else it assumes it's an empty cell.
     */
    private fun scaleAndCenter(cleanedImage: ImageMatrix): Pair<ImageMatrix, Boolean> {
        val area = cleanedImage.area().value().toDouble()
        val percentFill = if (area > 0) cleanedImage.countNonZero() / area else 0.0

        return if (percentFill > 0.1)
            cleanedImage.scaleAndCenter(CELL_SIZE, CELL_SIZE / 7) to false
        else
            ImageMatrix.empty(Area(CELL_SIZE)) to true
    }

    /**
     * This function encapsulates the full scan of the image (height, width, channel) and uses a void callback function
     * to process avery point of the image. This is useful to change the Mat (OpenCV) format to other formats.
     *
     * @param callBack the void function to process every data of the image.
     */
    fun scanMatrix(callBack: (idx: CellIndex, value: Int) -> Unit) {
        data.scanMatrix(callBack)
    }
}