package com.github.pintowar.sudoscan.api.cv

import com.github.pintowar.sudoscan.api.cv.CvSpecHelpers.croppedSudoku
import com.github.pintowar.sudoscan.api.cv.CvSpecHelpers.dirtyEight
import com.github.pintowar.sudoscan.api.cv.CvSpecHelpers.frontalSudoku
import com.github.pintowar.sudoscan.api.cv.CvSpecHelpers.preProcessedSudoku
import com.github.pintowar.sudoscan.api.cv.CvSpecHelpers.sudoku
import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.doubles.plusOrMinus
import io.kotest.matchers.shouldBe

class ExtractorSpec : StringSpec({

    "test grayscale" {
        val graySudoku = Extractor.toGrayScale(sudoku)

        graySudoku.arrayHeight() shouldBe sudoku.arrayHeight()
        graySudoku.arrayWidth() shouldBe sudoku.arrayWidth()
        graySudoku.channels() shouldBe 1
    }

    "test pre process grayscale" {
        val graySudoku = Extractor.toGrayScale(sudoku)
        val preProcessed = Extractor.preProcessGrayImage(graySudoku)

        preProcessed.arrayHeight() shouldBe graySudoku.arrayHeight()
        preProcessed.arrayWidth() shouldBe graySudoku.arrayWidth()
        preProcessed.channels() shouldBe 1
    }

    "test find corners" {
        val corners = Extractor.findCorners(preProcessedSudoku)
        val sides = corners.sides()

        sides[0] shouldBe (508.0).plusOrMinus(1.0)
        sides[1] shouldBe (509.0).plusOrMinus(1.0)
        sides[2] shouldBe (462.0).plusOrMinus(1.0)
        sides[3] shouldBe (480.0).plusOrMinus(1.0)
    }

    "test crop image" {
        val frontal = Extractor.preProcessPhases(sudoku).frontal

        frontal.img.arrayHeight() shouldBe croppedSudoku.arrayHeight()
        frontal.img.arrayWidth() shouldBe croppedSudoku.arrayWidth()
        frontal.img.channels() shouldBe croppedSudoku.channels()
    }

    "test split squares" {
        val processedCrop = Extractor.preProcessGrayImage(croppedSudoku, false)
        val squares = Extractor.splitSquares(processedCrop)

        squares.size shouldBe 81
        squares.forEach { square ->
            square.width.toDouble() shouldBe (croppedSudoku.arrayWidth() / 9.0).plusOrMinus(1.0)
            square.height.toDouble() shouldBe (croppedSudoku.arrayHeight() / 9.0).plusOrMinus(1.0)
        }
    }

    "test find largest feature" {
        val (size, margin) = 43 to 17

        val bBox = BBox(Coordinate(margin, margin), size - 2 * margin, size - 2 * margin)
        val corners = Extractor.findLargestFeature(dirtyEight, bBox)

        corners.topLeft shouldBe Coordinate(17, 15)
        corners.topRight shouldBe Coordinate(17, 25)
        corners.bottomLeft shouldBe Coordinate(30, 15)
        corners.bottomRight shouldBe Coordinate(30, 25)
    }

    "test scale and center" {
        val finalSize = 28
        val result = Extractor.scaleAndCenter(dirtyEight, finalSize, 17)

        result.arrayWidth() shouldBe finalSize
        result.arrayHeight() shouldBe finalSize
    }

    "test extract cell - eight" {
        val eightSquare = BBox(Coordinate(0, 0), 43, 43)
        val result = Extractor.extractCell(frontalSudoku, eightSquare)

        result.isEmpty shouldBe false
        result.width shouldBe 28
        result.height shouldBe 28
    }

    "test extract cell - empty" {
        val eightSquare = BBox(Coordinate(43, 0), 43, 43)
        val result = Extractor.extractCell(frontalSudoku, eightSquare)

        result.isEmpty shouldBe true
        result.width shouldBe 28
        result.height shouldBe 28
    }
})