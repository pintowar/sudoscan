package io.github.pintowar.sudoscan.api.spi

import io.github.pintowar.sudoscan.api.Digit
import io.github.pintowar.sudoscan.api.SudokuCell
import io.github.pintowar.sudoscan.api.cv.CvSpecHelpers.dirtyEight
import io.kotest.core.spec.style.StringSpec
import io.kotest.extensions.system.withSystemProperties
import io.kotest.matchers.shouldBe
import io.kotest.matchers.types.shouldBeInstanceOf
import io.mockk.every
import io.mockk.spyk

class RecognizerSpec : StringSpec({

    "test provider" {
        Recognizer.provider().shouldBeInstanceOf<MockRecognizer>()
    }

    "test model url" {
        withSystemProperties("sudoscan.recognizer.model.url" to "myModel") {
            Recognizer.provider().modelUrl() shouldBe "myModel"
        }

        Recognizer.provider().modelUrl() shouldBe "myFileModel"
    }

    "test reliable predict" {
        val cells = listOf(SudokuCell(dirtyEight))
        val recognizer = spyk<MockRecognizer>()

        every { recognizer.predict(any()) } returns sequenceOf(Digit.Valid(8, 1.0))
        val (validEight, _) = recognizer.reliablePredict(cells)
        validEight.shouldBeInstanceOf<Digit.Valid>()

        every { recognizer.predict(any()) } returns sequenceOf(Digit.Valid(8, 0.6))
        val (invalidEight, _) = recognizer.reliablePredict(cells)
        invalidEight.shouldBeInstanceOf<Digit.Unknown>()

        every { recognizer.predict(any()) } returns sequenceOf(Digit.Unknown)
        val (unknown, _) = recognizer.reliablePredict(cells)
        unknown.shouldBeInstanceOf<Digit.Unknown>()
    }
})