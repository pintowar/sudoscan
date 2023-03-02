package io.github.pintowar.sudoscan.djl

import io.github.pintowar.sudoscan.api.Digit
import io.github.pintowar.sudoscan.api.SudokuCell
import io.github.pintowar.sudoscan.api.cv.CvSpecHelpers.cvRead
import io.kotest.core.spec.style.StringSpec
import io.kotest.data.forAll
import io.kotest.data.row
import io.kotest.matchers.shouldBe

class RecognizerDjlSpec : StringSpec({
    val recognizer = RecognizerDjl()

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
            val img = SudokuCell(cvRead("imgs/digits/$file.png").toGrayScale())
            val predicted = recognizer.predict(img)
            predicted.value shouldBe digit
        }
    }

    "unknown digit predict" {
        val predicted = recognizer.predict(listOf(SudokuCell.EMPTY))
        predicted.first() shouldBe Digit.Unknown
    }
})