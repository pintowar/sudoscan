package io.github.pintowar.sudoscan.djl

import ai.djl.ndarray.NDArray
import ai.djl.ndarray.NDManager
import ai.djl.ndarray.types.DataType
import ai.djl.ndarray.types.Shape
import io.github.pintowar.sudoscan.api.SudokuCell

/**
 * Extension function to convert a SudokuCell into a normalized (0 - 1) NDArray.
 * NDArray is the default format used by the DJL framework.
 *
 * @param manager NDArray managers are used to create NDArrays (n-dimensional array on native engine).
 * @return NDArray representation of the sudoku cell image.
 */
fun SudokuCell.toNDArray(manager: NDManager): NDArray {
    val bb = manager.allocateDirect((channels * height * width).toInt())
    this.scanMatrix { _, value -> bb.put(value.toByte()) }
    bb.rewind()
    return manager.create(bb, Shape(height, width, channels), DataType.UINT8)
        .toType(DataType.FLOAT32, true).div(255)
}