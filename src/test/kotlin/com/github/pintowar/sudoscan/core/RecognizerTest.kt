package com.github.pintowar.sudoscan.core

import org.datavec.image.loader.ImageLoader
import org.nd4j.linalg.api.ndarray.INDArray
import org.nd4j.linalg.factory.Nd4j

import java.io.File
import javax.imageio.ImageIO
import kotlin.test.Test
import kotlin.test.assertEquals

internal class RecognizerTest {

    val recognizer = Recognizer()

    fun imRead(path: String): INDArray {
        val cl = Thread.currentThread().contextClassLoader
        val img = ImageIO.read(cl.getResourceAsStream(path))
        return ImageLoader(img.height.toLong(), img.width.toLong(), 1).asMatrix(img)
                .reshape(img.height.toLong(), img.width.toLong(), 1)
    }

    fun cvRead(path: String): INDArray {
        val cl = Thread.currentThread().contextClassLoader
        val mat = OpenCvWrapper.imread(File(cl.getResource(path).toURI()).absolutePath)
        return OpenCvWrapper.toNdArray(mat)
    }

    @Test
    fun predict() {
        val digits = listOf("one", "two", "three", "four", "five", "six", "seven", "eight", "nine")
                .map { cvRead("imgs/${it}.png") }.toTypedArray()
        val table = Nd4j.stack(0, *digits)

        assertEquals(listOf(1, 2, 3, 4, 5, 6, 7, 8, 9), recognizer.predict(table))
    }
}