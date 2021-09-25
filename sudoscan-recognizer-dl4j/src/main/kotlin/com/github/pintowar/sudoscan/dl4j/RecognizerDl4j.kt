package com.github.pintowar.sudoscan.dl4j

import com.github.pintowar.sudoscan.api.SudokuCell
import com.github.pintowar.sudoscan.api.spi.Recognizer
import mu.KLogging
import org.deeplearning4j.nn.modelimport.keras.KerasModelImport
import org.deeplearning4j.nn.multilayer.MultiLayerNetwork
import org.nd4j.linalg.api.ndarray.INDArray
import org.nd4j.linalg.factory.Nd4j
import java.net.URL

/**
 * Recognizer implementation that uses DeepLearning4j (Dl4j) to recognize numbers on sudoku cells.
 * This implementation uses a Machine Learning Image classification model trained using Keras + Tensorflow 2.
 * The ML model will be downloaded from a remote server. The url to download the file can be defined with the
 * property: "sudoscan.recognizer.model.url".
 */
class RecognizerDl4j : Recognizer {

    companion object : KLogging()

    private var model: MultiLayerNetwork = URL(modelUrl()).openStream().use { input ->
        KerasModelImport.importKerasSequentialModelAndWeights(input, false)
    }.also {
        logger.debug { it.summary() }
    }

    private fun validateImages(digits: INDArray) {
        val shape = digits.shape()
        assert(shape.size == 4)
        assert(shape[0] >= 0L)
        assert(shape[3] == 1L)
    }

    override val name: String = "Recognizer Dl4j"

    override fun predict(cells: List<SudokuCell>): List<Int> {
        val digitArray = cells.map { it.toNdArray() }.toTypedArray()
        val stackDigits = Nd4j.stack(0, *digitArray)
        return predict(stackDigits).zip(cells).map { (rec, dig) -> if (dig.empty) 0 else rec }
    }

    /**
     * Predict the number provided by a list images of sudoku cells.
     *
     * @param cells list of sudoku cells to have a number recognized.
     * @return list of integer with the number found on each sudoku cell. If an empty sudoku cell is provided,
     * a number 0 (zero) is returned.
     */
    private fun predict(cells: INDArray): List<Int> {
        validateImages(cells)
        return model.predict(cells.div(255.0).neg().add(1)).toList()
    }
}