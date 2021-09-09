package com.github.pintowar.sudoscan.dl4j

import io.kotest.core.spec.style.StringSpec
import io.kotest.data.forAll
import io.kotest.data.row
import io.kotest.matchers.shouldBe
import org.bytedeco.opencv.global.opencv_imgcodecs
import org.nd4j.linalg.api.ndarray.INDArray
import java.io.File

class RecognizerDl4jTest : StringSpec({
    val recognizer = RecognizerDl4j()

    fun cvRead(path: String): INDArray {
        val cl = Thread.currentThread().contextClassLoader
        val filename = File(cl.getResource(path)!!.toURI()).absolutePath
        val mat = opencv_imgcodecs.imread(filename, opencv_imgcodecs.IMREAD_GRAYSCALE)
        return mat.toNdArray()
    }

    "should recognize sample digits" {
        forAll(
            row("one", 1),
            row("two", 2),
            row("three", 3),
            row("four", 4),
            row("five", 5),
            row("six", 6),
            row("seven", 7),
            row("eight", 8),
            row("nine", 9)
        ) { file: String, digit: Int ->
            val img = cvRead("imgs/digits/${file}.png")
            recognizer.predict(img.reshape(1, 28, 28, 1))[0] shouldBe digit
        }
    }
})
