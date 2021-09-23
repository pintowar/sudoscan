package com.github.pintowar.sudoscan.dl4j

import com.github.pintowar.sudoscan.api.SudokuCell
import org.nd4j.linalg.api.ndarray.INDArray
import org.nd4j.linalg.factory.Nd4j

fun SudokuCell.toNdArray(): INDArray {
    val ret = Nd4j.create(channels, height, width)
    scanMatrix { (w, h, c), value -> ret.putScalar(longArrayOf(c, h, w), value) }
    return ret.reshape(height, width, channels)
}