package io.github.pintowar.sudoscan.api.cv

import io.github.pintowar.sudoscan.api.cv.CvSpecHelpers.croppedSudoku
import io.github.pintowar.sudoscan.api.cv.CvSpecHelpers.dirtyEight
import io.github.pintowar.sudoscan.api.cv.CvSpecHelpers.frontalSudoku
import io.github.pintowar.sudoscan.api.cv.CvSpecHelpers.sudoku
import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.doubles.plusOrMinus
import io.kotest.matchers.shouldBe

class ExtractorSpec : StringSpec({

    "test crop image" {
        val frontal = Extractor.preProcessPhases(sudoku).frontal

        frontal.img.similarity(croppedSudoku) shouldBe (1.0).plusOrMinus(0.1)
        frontal.img.height() shouldBe croppedSudoku.height()
        frontal.img.width() shouldBe croppedSudoku.width()
        frontal.img.channels() shouldBe croppedSudoku.channels()
    }

    "test split squares" {
        val processedCrop = croppedSudoku.preProcessGrayImage(false)
        val squares = Extractor.splitSquares(processedCrop)

        squares.size shouldBe 81
        squares.forEach { square ->
            square.width.toDouble() shouldBe (croppedSudoku.width() / 9.0).plusOrMinus(1.0)
            square.height.toDouble() shouldBe (croppedSudoku.height() / 9.0).plusOrMinus(1.0)
        }
    }

    "test find largest feature" {
        val margin = 17

        val corners = Extractor.findLargestFeature(dirtyEight, margin)

        corners.topLeft shouldBe Coordinate(17, 15)
        corners.topRight shouldBe Coordinate(17, 25)
        corners.bottomLeft shouldBe Coordinate(30, 15)
        corners.bottomRight shouldBe Coordinate(30, 25)
    }

    "test extract cell - eight" {
        val eightSquare = BBox(0, 0, 43, 43)
        val result = Extractor.extractCell(frontalSudoku, eightSquare)

        result.isEmpty shouldBe false
        result.width shouldBe 28
        result.height shouldBe 28
    }

    "test extract cell - empty" {
        val eightSquare = BBox(43, 0, 43, 43)
        val result = Extractor.extractCell(frontalSudoku, eightSquare)

        result.isEmpty shouldBe true
        result.width shouldBe 28
        result.height shouldBe 28
    }
})