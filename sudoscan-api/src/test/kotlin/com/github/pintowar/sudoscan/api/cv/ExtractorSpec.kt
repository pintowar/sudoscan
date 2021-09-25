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
        val cropped = Extractor.cropImage(sudoku)

        cropped.img.arrayHeight() shouldBe croppedSudoku.arrayHeight()
        cropped.img.arrayWidth() shouldBe croppedSudoku.arrayWidth()
        cropped.img.channels() shouldBe croppedSudoku.channels()
    }

    "test split squares" {
        val processedCrop = Extractor.preProcessGrayImage(croppedSudoku, false)
        val squares = Extractor.splitSquares(processedCrop)

        squares.size shouldBe 81
        squares.forEach { square ->
            (square.end.x - square.begin.x).toDouble() shouldBe (croppedSudoku.arrayWidth() / 9.0).plusOrMinus(1.0)
            (square.end.y - square.begin.y).toDouble() shouldBe (croppedSudoku.arrayHeight() / 9.0).plusOrMinus(1.0)
        }
    }

    "test rect from segment" {
        val validSquare = Segment(Coordinate(0, 0), Coordinate(43, 43))
        val valid = Extractor.rectFromSegment(frontalSudoku, validSquare)

        valid.arrayHeight() shouldBe 43
        valid.arrayWidth() shouldBe 43

        val invalidSquare = Segment(Coordinate(43, 43), Coordinate(0, 0))
        val invalid = Extractor.rectFromSegment(frontalSudoku, invalidSquare)

        invalid.arrayHeight() shouldBe 0
        invalid.arrayWidth() shouldBe 0
    }

    "test find largest feature" {
        val (size, margin) = 43 to 17

        val diagonal = Segment(Coordinate(margin, margin), Coordinate(size - margin, size - margin))
        val corners = Extractor.findLargestFeature(dirtyEight, diagonal)

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
        val eightSquare = Segment(Coordinate(0, 0), Coordinate(43, 43))
        val finalSize = 28
        val result = Extractor.extractCell(frontalSudoku, eightSquare, finalSize)

        result.empty shouldBe false
        result.width shouldBe 28
        result.height shouldBe 28
    }

    "test extract cell - empty" {
        val eightSquare = Segment(Coordinate(43, 0), Coordinate(86, 43))
        val finalSize = 28
        val result = Extractor.extractCell(frontalSudoku, eightSquare, finalSize)

        result.empty shouldBe true
        result.width shouldBe 28
        result.height shouldBe 28
    }
})