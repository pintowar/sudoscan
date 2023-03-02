package io.github.pintowar.sudoscan.api.spi

import io.github.pintowar.sudoscan.api.GrayMatrix
import io.github.pintowar.sudoscan.api.ImageMatrix
import io.github.pintowar.sudoscan.api.cv.Area
import java.util.*

interface ImageProvider {

    companion object {
        fun provider(): ImageProvider {
            val loader = ServiceLoader.load(ImageProvider::class.java)
            return loader.single()
        }
    }

    fun fromBytes(image: ByteArray, grayscale: Boolean = false): ImageMatrix

    fun emptyGray(area: Area): GrayMatrix
}