package com.github.pintowar.sudoscan.api.cv

import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.doubles.plusOrMinus
import io.kotest.matchers.shouldBe
import org.bytedeco.opencv.global.opencv_imgcodecs
import org.bytedeco.opencv.opencv_core.Mat
import java.io.File

class ExtractorTest : StringSpec({

    fun cvRead(path: String, gray: Boolean = false): Mat {
        val flag = if (gray) opencv_imgcodecs.IMREAD_GRAYSCALE else opencv_imgcodecs.IMREAD_COLOR
        val cl = Thread.currentThread().contextClassLoader
        val filename = File(cl.getResource(path)!!.toURI()).absolutePath
        return opencv_imgcodecs.imread(filename, flag)
    }

    val sudoku = cvRead("imgs/sudoku01.jpg")
    val preProcessedSudoku = cvRead("imgs/pre-processed-sudoku01.jpg", true)
    val croppedSudoku = cvRead("imgs/cropped-sudoku-image01.jpg", true)
    val frontalSudoku = cvRead("imgs/frontal-processed-sudoku01.jpg", true)
    val dirtyEight = cvRead("imgs/dirty-eight.jpg", true)

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
        val validSquare = Segment(Coord(0, 0), Coord(43, 43))
        val valid = Extractor.rectFromSegment(frontalSudoku, validSquare)

        valid.arrayHeight() shouldBe 43
        valid.arrayWidth() shouldBe 43

        val invalidSquare = Segment(Coord(43, 43), Coord(0, 0))
        val invalid = Extractor.rectFromSegment(frontalSudoku, invalidSquare)

        invalid.arrayHeight() shouldBe 0
        invalid.arrayWidth() shouldBe 0
    }

    "test find largest feature" {
        val (size, margin) = 43 to 17

        val diagonal = Segment(Coord(margin, margin), Coord(size - margin, size - margin))
        val corners = Extractor.findLargestFeature(dirtyEight, diagonal)

        corners.topLeft shouldBe Coord(17, 15)
        corners.topRight shouldBe Coord(17, 25)
        corners.bottomLeft shouldBe Coord(30, 15)
        corners.bottomRight shouldBe Coord(30, 25)
    }

    "test scale and center" {
        val finalSize = 28
        val result = Extractor.scaleAndCenter(dirtyEight, finalSize, 17)

        result.arrayWidth() shouldBe finalSize
        result.arrayHeight() shouldBe finalSize
    }

    "test extract digit - eight" {
        val eightSquare = Segment(Coord(0, 0), Coord(43, 43))
        val finalSize = 28
        val result = Extractor.extractDigit(frontalSudoku, eightSquare, finalSize)

        result.empty shouldBe false
        result.data.arrayWidth() shouldBe 28
        result.data.arrayHeight() shouldBe 28
    }

    "test extract digit - empty" {
        val eightSquare = Segment(Coord(43, 0), Coord(86, 43))
        val finalSize = 28
        val result = Extractor.extractDigit(frontalSudoku, eightSquare, finalSize)

        result.empty shouldBe true
        result.data.arrayWidth() shouldBe 28
        result.data.arrayHeight() shouldBe 28
    }

})