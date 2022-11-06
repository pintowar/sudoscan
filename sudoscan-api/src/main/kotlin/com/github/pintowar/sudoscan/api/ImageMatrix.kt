package com.github.pintowar.sudoscan.api

import com.github.pintowar.sudoscan.api.cv.Area
import com.github.pintowar.sudoscan.api.cv.BBox
import com.github.pintowar.sudoscan.api.cv.CellIndex
import com.github.pintowar.sudoscan.api.spi.ImageProvider

interface ImageMatrix {

    companion object {
        private val provider: ImageProvider = ImageProvider.provider()

        fun fromBytes(bytes: ByteArray): ImageMatrix = provider.fromBytes(bytes)

        fun fromFile(src: String, mono: Boolean = true): ImageMatrix = provider.fromFile(src, mono)

        fun empty(area: Area, mono: Boolean = true): ImageMatrix = provider.empty(area, mono)
    }

    fun height(): Int

    fun width(): Int

    fun channels(): Int

    fun toBytes(ext: String): ByteArray

    fun concat(img: ImageMatrix, horizontal: Boolean = true): ImageMatrix

    fun resize(area: Area): ImageMatrix

    fun crop(bBox: BBox): ImageMatrix

    fun area(): Area

    fun countNonZero(): Int

    /**
     * Convert image to gray scale.
     *
     * @return gray scale image.
     */
    fun toGrayScale(): ImageMatrix

    /**
     * Transform image from black-white to white-black.
     */
    fun revertColors(): ImageMatrix

    fun copyMakeBorder(top: Int, bottom: Int, left: Int, right: Int, background: Int): ImageMatrix

    fun scanMatrix(callBack: (idx: CellIndex, value: Int) -> Unit)

    /**
     * Rescale image given a new size and centralize the middle object (number).
     *
     * @param size new size (width and height) to be scaled.
     * @param margin margin to help on centralization.
     * @param background background color.
     * @return transformed image.
     */
    fun scaleAndCenter(size: Int, margin: Int = 0, background: Int = 0): ImageMatrix {
        fun centrePad(length: Int): Pair<Int, Int> {
            val side = (size - length) / 2
            return if (length % 2 == 0) side to side else side to (side + 1)
        }

        val isHigher = height() > width()
        val ratio = (size - margin).toDouble() / (if (isHigher) height() else width())
        val w = (ratio * width()).toInt()
        val h = (ratio * height()).toInt()
        val halfMargin = margin / 2

        val (tPad, bPad, lPad, rPad) = if (isHigher) {
            val (l, r) = centrePad(w)
            listOf(halfMargin, halfMargin, l, r)
        } else {
            val (t, b) = centrePad(h)
            listOf(t, b, halfMargin, halfMargin)
        }

        val aux = resize(Area(w, h))
        val aux2 = aux.copyMakeBorder(tPad, bPad, lPad, rPad, background)
        return aux2.resize(Area(size))
    }
}