package com.github.pintowar.sudoscan.core.cv

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
        val img = cvRead("imgs/pre-processed-sudoku01.jpg", true)
        val corners = Extractor.findCorners(img)
        val sides = corners.sides()

        sides[0] shouldBe (508.0).plusOrMinus(1.0)
        sides[1] shouldBe (509.0).plusOrMinus(1.0)
        sides[2] shouldBe (462.0).plusOrMinus(1.0)
        sides[3] shouldBe (480.0).plusOrMinus(1.0)
    }

    "test crop image" {
        val sudoku = cvRead("imgs/sudoku01.jpg")
        val cropped = Extractor.cropImage(sudoku)

        cropped.img.arrayHeight() shouldBe 387
        cropped.img.arrayWidth() shouldBe 387
        cropped.img.channels() shouldBe 1
    }

})