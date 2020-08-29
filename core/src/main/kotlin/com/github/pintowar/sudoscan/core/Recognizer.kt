package com.github.pintowar.sudoscan.core

import mu.KLogging
import org.deeplearning4j.nn.modelimport.keras.KerasModelImport
import org.deeplearning4j.nn.multilayer.MultiLayerNetwork
import org.nd4j.linalg.api.ndarray.INDArray
import org.nd4j.linalg.factory.Nd4j
import kotlin.properties.Delegates

class Recognizer {

    companion object : KLogging()

    private var model: MultiLayerNetwork by Delegates.notNull()

    constructor(path: String = "model/chars74k_model.h5") {
        val cl = Thread.currentThread().contextClassLoader
        val input = cl.getResourceAsStream(path)
        model = KerasModelImport.importKerasSequentialModelAndWeights(input, false)

        logger.debug { model.summary() }
    }

    fun validateImages(digits: INDArray) {
        val shape = digits.shape()
        assert(shape.size == 4)
        assert(shape[0] >= 0L)
        assert(shape[3] == 1L)
    }

    fun predict(digits: List<Parser.Digit>): List<Int> {
        val digitArray = digits.map { OpenCvWrapper.toNdArray(it.data) }.toTypedArray()
        val stackDigits = Nd4j.stack(0, *digitArray)
        return predict(stackDigits).zip(digits).map { (rec, dig) -> if (dig.empty) 0 else rec }
    }

    fun predict(digits: INDArray): List<Int> {
        validateImages(digits)
        return model.predict(digits.div(255.0).neg().add(1)).toList()
    }
}