package com.github.pintowar.sudoscan.djl

import ai.djl.ndarray.NDArray
import ai.djl.ndarray.NDManager
import ai.djl.ndarray.types.DataType
import ai.djl.ndarray.types.Shape
import com.github.pintowar.sudoscan.api.SudokuCell

fun SudokuCell.toNDArray(manager: NDManager): NDArray {
    val bb = manager.allocateDirect((channels * height * width).toInt())
    this.scanMatrix { _, value -> bb.put(value.toByte()) }
    bb.rewind()
    return manager.create(bb, Shape(height, width, channels), DataType.UINT8)
}