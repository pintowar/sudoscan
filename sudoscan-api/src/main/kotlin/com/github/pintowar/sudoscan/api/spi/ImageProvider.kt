package com.github.pintowar.sudoscan.api.spi

import com.github.pintowar.sudoscan.api.ImageMatrix
import com.github.pintowar.sudoscan.api.cv.Area
import java.util.*

interface ImageProvider {

    companion object {
        fun provider(): ImageProvider {
            val loader = ServiceLoader.load(ImageProvider::class.java)
            return loader.single()
        }
    }

    fun fromBytes(image: ByteArray): ImageMatrix

    fun empty(area: Area, mono: Boolean = true): ImageMatrix
}