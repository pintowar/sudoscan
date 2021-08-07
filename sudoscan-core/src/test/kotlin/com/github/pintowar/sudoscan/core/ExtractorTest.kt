package com.github.pintowar.sudoscan.core

import io.kotest.core.spec.style.StringSpec
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

})