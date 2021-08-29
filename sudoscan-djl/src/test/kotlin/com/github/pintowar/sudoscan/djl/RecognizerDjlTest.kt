package com.github.pintowar.sudoscan.djl

import ai.djl.modality.cv.Image
import ai.djl.modality.cv.ImageFactory
import io.kotest.core.spec.style.StringSpec
import io.kotest.data.forAll
import io.kotest.data.row
import io.kotest.matchers.shouldBe

class RecognizerDjlTest : StringSpec({
    val recognizer = RecognizerDjl()

    fun cvRead(path: String): Image {
        val cl = Thread.currentThread().contextClassLoader
        return ImageFactory.getInstance().fromInputStream(cl.getResourceAsStream(path))
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
