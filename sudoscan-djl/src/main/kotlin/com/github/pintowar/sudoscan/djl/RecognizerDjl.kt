package com.github.pintowar.sudoscan.djl

import ai.djl.Application
import ai.djl.Model
import ai.djl.modality.Classifications
import ai.djl.modality.cv.BufferedImageFactory
import ai.djl.modality.cv.Image
import ai.djl.modality.cv.util.NDImageUtils
import ai.djl.ndarray.NDList
import ai.djl.repository.zoo.Criteria
import ai.djl.translate.Batchifier
import ai.djl.translate.Translator
import ai.djl.translate.TranslatorContext
import com.github.pintowar.sudoscan.core.Digit
import com.github.pintowar.sudoscan.core.OpenCvWrapper
import com.github.pintowar.sudoscan.core.spi.Recognizer
import mu.KLogging

class RecognizerDjl(path: String) : Recognizer {

    constructor() : this("model/chars74k")

    companion object : KLogging()

    private var model: Model
    private val translator = ImageTranslator()

    init {
        val cl = Thread.currentThread().contextClassLoader
        val input = cl.getResource(path)!!
        model = Criteria.builder()
            .optApplication(Application.CV.IMAGE_CLASSIFICATION)
            .setTypes(Image::class.java, Classifications::class.java)
            .optEngine("TensorFlow")
            .optModelUrls(input.toString())
            .build()
            .loadModel()
        logger.debug {
            """
                Input: ${model.describeOutput()}
                Output: ${model.describeOutput()}
            """.trimIndent()
        }
    }

    override fun predict(digits: List<Digit>): List<Int> {
        return digits.map {
            if (it.empty) 0 else predict(BufferedImageFactory().fromImage(OpenCvWrapper.toImage(it.data)))
        }
    }

    fun predict(digit: Image): Int {
        model.newPredictor(translator).use { predictor ->
            val prediction = predictor.predict(digit)
            return prediction.best<Classifications.Classification>().className.toInt()
        }
    }

    internal class ImageTranslator : Translator<Image, Classifications> {
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