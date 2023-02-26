package com.github.pintowar.sudoscan.opencv

import com.github.pintowar.sudoscan.api.cv.CvSpecHelpers.dirtyEight
import com.github.pintowar.sudoscan.api.cv.CvSpecHelpers.preProcessedSudoku
import com.github.pintowar.sudoscan.api.cv.CvSpecHelpers.sudoku
import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.doubles.plusOrMinus
import io.kotest.matchers.shouldBe

class OpenCvMatrixSpec : StringSpec({

    "test grayscale" {
        val graySudoku = sudoku.toGrayScale()

        graySudoku.height() shouldBe sudoku.height()
        graySudoku.width() shouldBe sudoku.width()
        graySudoku.channels() shouldBe 1
    }

    "test pre process grayscale" {
        val graySudoku = sudoku.toGrayScale()
        val preProcessed = graySudoku.preProcessGrayImage()

        preProcessed.height() shouldBe graySudoku.height()
        preProcessed.width() shouldBe graySudoku.width()
        preProcessed.channels() shouldBe 1
    }

    "test find corners" {
        val corners = preProcessedSudoku.findCorners()
        val sides = corners.sides()

        sides[0] shouldBe (508.0).plusOrMinus(1.0)
        sides[1] shouldBe (509.0).plusOrMinus(1.0)
        sides[2] shouldBe (462.0).plusOrMinus(1.0)
        sides[3] shouldBe (480.0).plusOrMinus(1.0)
    }

    "test scale and center" {
        val finalSize = 28
        val result = dirtyEight.scaleAndCenter(finalSize, 17)

        result.width() shouldBe finalSize
        result.height() shouldBe finalSize
    }
})