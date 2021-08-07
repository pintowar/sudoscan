package com.github.pintowar.sudoscan.core

import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.doubles.plusOrMinus
import io.kotest.matchers.shouldBe
import org.bytedeco.opencv.opencv_core.Mat
import javax.imageio.ImageIO

class ExtractorTest: StringSpec({

    fun cvRead(path: String): Mat {
        val cl = Thread.currentThread().contextClassLoader
        val img = ImageIO.read(cl.getResourceAsStream(path))
        return OpenCvWrapper.toMat(img)
    }

    "test grayscale" {
        val sudoku = cvRead("imgs/sudoku01.jpg")
        val graySudoku = Extractor.toGrayScale(sudoku)

        graySudoku.arrayHeight() shouldBe sudoku.arrayHeight()
        graySudoku.arrayWidth() shouldBe sudoku.arrayWidth()
        graySudoku.channels() shouldBe 1
    }

    "test pre process grayscale" {
        val graySudoku = Extractor.toGrayScale(cvRead("imgs/sudoku01.jpg"))
        val preProcessed = Extractor.preProcessGrayImage(graySudoku)

        preProcessed.arrayHeight() shouldBe graySudoku.arrayHeight()
        preProcessed.arrayWidth() shouldBe graySudoku.arrayWidth()
        preProcessed.channels() shouldBe 1
    }

    "test find corners" {
        val img = cvRead("imgs/pre-processed-sudoku01.jpg")
        val corners = Extractor.findCorners(img)
        val sides = corners.sides()

        sides[0] shouldBe(508.0).plusOrMinus(1.0)
        sides[1] shouldBe(509.0).plusOrMinus(1.0)
        sides[2] shouldBe(462.0).plusOrMinus(1.0)
        sides[3] shouldBe(480.0).plusOrMinus(1.0)
    }

})