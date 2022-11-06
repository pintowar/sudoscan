package com.github.pintowar.sudoscan.api

import com.github.pintowar.sudoscan.api.cv.*
import com.github.pintowar.sudoscan.api.spi.ImageProvider
import java.awt.Color

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
     * Pre-process a gray scale image. Basically uses a gaussian blur + adaptive threshold.
     * It also can dilate the image (true by default).
     *
     * @param dilate dilate flag (true by default).
     * @return preprocessed image.
     */
    fun preProcessGrayImage(dilate: Boolean = true): ImageMatrix

    /**
     * Find corners of the biggest square found in the image.
     * The input image must be in grayscale.
     *
     * @return the biggest square coordinates.
     */
    fun findCorners(): RectangleCorners

    /**
     * This function changes an image perspective to a frontal view given a square corners coordinates.
     *
     * @param corners square coordinates of the desired object.
     * @return img with a frontal view.
     */
    fun frontalPerspective(corners: RectangleCorners): FrontalPerspective<ImageMatrix>

    fun getPerspectiveTransform(dst: ImageMatrix): ImageMatrix

    fun warpPerspective(m: ImageMatrix, area: Area): ImageMatrix

    fun bitwiseAnd(that: ImageMatrix): ImageMatrix

    /**
     * Transform image from black-white to white-black.
     */
    fun revertColors(): ImageMatrix

    fun clone(): ImageMatrix

    fun findLargestFeature(bBox: BBox): RectangleCorners

    /**
     * This function has the responsibility to remove (or at least try) the grids of the pre-processed frontal image.
     *
     * @return frontal image without the images, or [sudokuGrayImg] case it fails.
     */
    fun removeGrid(): ImageMatrix

    fun copyMakeBorder(top: Int, bottom: Int, left: Int, right: Int, background: Int): ImageMatrix

    fun putText(digit: Digit, coord: Coordinate, fSize: Double, color: Color)

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