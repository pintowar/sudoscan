package com.github.pintowar.sudoscan.djl

import ai.djl.Application
import ai.djl.Model
import ai.djl.modality.Classifications
import ai.djl.modality.cv.Image
import ai.djl.ndarray.NDList
import ai.djl.repository.zoo.Criteria
import ai.djl.translate.Batchifier
import ai.djl.translate.Translator
import ai.djl.translate.TranslatorContext
import com.github.pintowar.sudoscan.api.Digit
import com.github.pintowar.sudoscan.api.SudokuCell
import com.github.pintowar.sudoscan.api.spi.Recognizer
import mu.KLogging

/**
 * Recognizer implementation that uses Deep Java Learning (DJL) to recognize numbers on sudoku cells.
 * This implementation uses a Machine Learning Image classification model trained using Keras + Tensorflow 2.
 * The ML model will be downloaded from a remote server. The url to download the file can be defined with the
 * property: "sudoscan.recognizer.model.url".
 */
class RecognizerDjl : Recognizer {

    companion object : KLogging()

    private val translator = MatTranslator()
    private val model: Model = Criteria.builder()
        .optApplication(Application.CV.IMAGE_CLASSIFICATION)
        .setTypes(Image::class.java, Classifications::class.java)
        .optEngine("TensorFlow")
        .optModelUrls(modelUrl())
        .build()
        .loadModel()
        .also {
            logger.debug {
                """
                Input: ${it.describeOutput()}
                Output: ${it.describeOutput()}
                """.trimIndent()
            }
        }

    override val name: String = "Recognizer DJL"

    override fun predict(cells: List<SudokuCell>): Sequence<Digit> {
        return cells.asSequence().map { predict(it) }
    }

    /**
     * Predict the number provided by a list images of sudoku cells.
     *
     * @param cell a sudoku cells to have a number recognized.
     * @return the digit found on the image. If an empty sudoku cell is provided, an unknown digit is returned.
     */
    fun predict(cell: SudokuCell): Digit {
        return if (cell.empty) Digit.Unknown else
            model.newPredictor(translator).use { predictor ->
                val prediction = predictor.predict(cell)
                val best = prediction.best<Classifications.Classification>()
                Digit.Valid(best.className.toInt(), best.probability)
            }
    }

    internal class MatTranslator : Translator<SudokuCell, Classifications> {
        private val digits = (0..9).map { "$it" }

        override fun processInput(ctx: TranslatorContext, input: SudokuCell): NDList {
            val array = input.toNDArray(ctx.ndManager)
            return NDList(array)
        }

        override fun processOutput(ctx: TranslatorContext, list: NDList): Classifications {
            val probabilities = list.singletonOrThrow()
            return Classifications(digits, probabilities)
        }

        override fun getBatchifier(): Batchifier = Batchifier.STACK
    }
}