package com.github.pintowar.sudoscan.djl

import io.kotest.core.spec.style.StringSpec
import io.kotest.data.forAll
import io.kotest.data.row
import io.kotest.matchers.shouldBe
import org.bytedeco.opencv.global.opencv_imgcodecs
import org.bytedeco.opencv.opencv_core.Mat
import java.io.File

class RecognizerDjlTest : StringSpec({
    val recognizer = RecognizerDjl()

    fun cvRead(path: String): Mat {
        val cl = Thread.currentThread().contextClassLoader
        val filename = File(cl.getResource(path)!!.toURI()).absolutePath
        return opencv_imgcodecs.imread(filename, opencv_imgcodecs.IMREAD_GRAYSCALE)
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
            recognizer.predict(img) shouldBe digit
        }
    }
})