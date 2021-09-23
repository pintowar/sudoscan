package com.github.pintowar.sudoscan.djl

import ai.djl.Application
import ai.djl.Model
import ai.djl.modality.Classifications
import ai.djl.modality.cv.Image
import ai.djl.modality.cv.util.NDImageUtils
import ai.djl.ndarray.NDList
import ai.djl.repository.zoo.Criteria
import ai.djl.translate.Batchifier
import ai.djl.translate.Translator
import ai.djl.translate.TranslatorContext
import com.github.pintowar.sudoscan.api.SudokuCell
import com.github.pintowar.sudoscan.api.spi.Recognizer
import mu.KLogging
import org.bytedeco.opencv.opencv_core.Mat

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

    override fun predict(cells: List<SudokuCell>): List<Int> {
        return cells.map { predict(it) }
    }

    fun predict(digit: SudokuCell): Int {
        return if (digit.empty) 0 else
            model.newPredictor(translator).use { predictor ->
                val prediction = predictor.predict(digit)
                prediction.best<Classifications.Classification>().className.toInt()
            }
    }

    internal class MatTranslator : Translator<SudokuCell, Classifications> {
        private val digits = (0..9).map { "$it" }
        private val size = 28
        private val lSize = size.toLong()

        override fun processInput(ctx: TranslatorContext, input: SudokuCell): NDList {
            val array = input.toNDArray(ctx.ndManager)
            return NDList(NDImageUtils.resize(array, size).reshape(lSize, lSize, 1).div(255).neg().add(1))
        }

        override fun processOutput(ctx: TranslatorContext, list: NDList): Classifications {
            val probabilities = list.singletonOrThrow()
            return Classifications(digits, probabilities)
        }

        override fun getBatchifier(): Batchifier = Batchifier.STACK
    }
}