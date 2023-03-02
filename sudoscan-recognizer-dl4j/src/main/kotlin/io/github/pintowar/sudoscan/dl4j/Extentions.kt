package io.github.pintowar.sudoscan.dl4j

import io.github.pintowar.sudoscan.api.SudokuCell
import org.nd4j.linalg.api.ndarray.INDArray
import org.nd4j.linalg.factory.Nd4j

/**
 * Extension function to convert a SudokuCell into a normalized (0 - 1) INDArray.
 * INDArray is the default format used by the Dl4j/Nd4j framework.
 *
 * @return INDArray representation of the sudoku cell image.
 */
fun SudokuCell.toNdArray(): INDArray {
    val ret = Nd4j.create(channels, height, width)
    scanMatrix { (w, h, c), value -> ret.putScalar(longArrayOf(c, h, w), value) }
    return ret.reshape(height, width, channels).div(255.0)
}