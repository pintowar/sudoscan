package com.github.pintowar.sudoscan.djl

import ai.djl.Application
import ai.djl.Model
import ai.djl.inference.Predictor
import ai.djl.modality.Classifications
import ai.djl.modality.cv.Image
import ai.djl.modality.cv.util.NDImageUtils
import ai.djl.ndarray.NDList
import ai.djl.repository.zoo.Criteria
import ai.djl.translate.Batchifier
import ai.djl.translate.Translator
import ai.djl.translate.TranslatorContext
import mu.KLogging
import java.nio.file.Path

class Recognizer(path: String = "model/chars74k") {

    companion object : KLogging()

    private var model: Model
    private val translator = MyTranslator()

    init {
        val cl = Thread.currentThread().contextClassLoader
        val input = Path.of(cl.getResource(path)!!.toURI())
        model = Criteria.builder()
            .optApplication(Application.CV.IMAGE_CLASSIFICATION)
            .setTypes(Image::class.java, Classifications::class.java)
            .optEngine("TensorFlow")
            .optModelPath(input)
            .build()
            .loadModel()
        logger.debug {
            """
                Input: ${model.describeOutput()}
                Output: ${model.describeOutput()}
            """.trimIndent()
        }
    }

    fun predict(digits: List<Image>): List<Int> = digits.map(this::predict)

    fun predict(digit: Image): Int {
        model.newPredictor(translator).use { predictor ->
            val prediction = predictor.predict(digit)
            return prediction.best<Classifications.Classification>().className.toInt()
        }
    }

    internal class MyTranslator : Translator<Image, Classifications> {
        private val digits = (0..9).map { "$it" }
        private val size = 28
        private val lSize = size.toLong()

        override fun processInput(ctx: TranslatorContext, input: Image): NDList {
            val array = input.toNDArray(ctx.ndManager, Image.Flag.GRAYSCALE)
            return NDList(NDImageUtils.resize(array, size).reshape(lSize, lSize, 1).div(255).neg().add(1))
        }

        override fun processOutput(ctx: TranslatorContext, list: NDList): Classifications {
            val probabilities = list.singletonOrThrow()
            return Classifications(digits, probabilities)
        }

        override fun getBatchifier(): Batchifier = Batchifier.STACK
    }
}