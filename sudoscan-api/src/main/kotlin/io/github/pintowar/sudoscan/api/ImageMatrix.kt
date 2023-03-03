package io.github.pintowar.sudoscan.api

import io.github.pintowar.sudoscan.api.cv.*
import io.github.pintowar.sudoscan.api.spi.ImageProvider
import java.awt.Color

interface ImageMatrix {

    companion object {
        private val provider: ImageProvider = ImageProvider.provider()

        fun fromBytes(bytes: ByteArray, grayscale: Boolean = false): ImageMatrix = provider.fromBytes(bytes, grayscale)

        fun empty(area: Area): GrayMatrix = provider.emptyGray(area)
    }

    fun height(): Int

    fun width(): Int

    fun channels(): Int

    fun area(): Area = Area(width(), height())

    fun validate()

    fun toBytes(ext: String): ByteArray

    fun concat(img: ImageMatrix, horizontal: Boolean = true): ImageMatrix

    fun resize(area: Area): ImageMatrix

    fun clone(): ImageMatrix

    fun bitwiseAnd(that: ImageMatrix): ImageMatrix

    fun revertColors(): ImageMatrix

    fun countNonZero(): Int

    fun similarity(other: ImageMatrix): Double

    fun findLargestFeature(bBox: BBox): RectangleCorners

    fun scanMatrix(callBack: (idx: CellIndex, value: Int) -> Unit)

    /**
     * Convert image to gray matrix.
     *
     * @return gray scale image.
     */
    fun toGrayMatrix(): GrayMatrix

    /**
     * Convert image to color matrix.
     *
     * @return colored image.
     */
    fun toColorMatrix(): ColorMatrix
}

interface GrayMatrix : ImageMatrix {

    override fun resize(area: Area): GrayMatrix

    override fun clone(): GrayMatrix

    override fun bitwiseAnd(that: ImageMatrix): GrayMatrix

    /**
     * Transform image from black-white to white-black.
     */
    override fun revertColors(): GrayMatrix

    fun crop(bBox: BBox): GrayMatrix

    /**
     * Pre-process a gray scale image. Basically uses a gaussian blur + adaptive threshold.
     * It also can dilate the image (true by default).
     *
     * @param dilate dilate flag (true by default).
     * @return preprocessed image.
     */
    fun preProcessGrayImage(dilate: Boolean = true): GrayMatrix

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
    fun frontalPerspective(corners: RectangleCorners): FrontalPerspective

    fun getPerspectiveTransform(dst: GrayMatrix): GrayMatrix

    /**
     * This function has the responsibility to remove (or at least try) the grids of the pre-processed frontal image.
     *
     * @return frontal image without the grids, or it self case it fails.
     */
    fun removeGrid(): GrayMatrix

    fun copyMakeBorder(top: Int, bottom: Int, left: Int, right: Int, background: Int): GrayMatrix

    /**
     * Rescale image given a new size and centralize the middle object (number).
     *
     * @param size new size (width and height) to be scaled.
     * @param margin margin to help on centralization.
     * @param background background color.
     * @return transformed image.
     */
    fun scaleAndCenter(size: Int, margin: Int = 0, background: Int = 0): GrayMatrix {
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

interface ColorMatrix : ImageMatrix {

    override fun resize(area: Area): ColorMatrix

    override fun clone(): ColorMatrix

    override fun bitwiseAnd(that: ImageMatrix): ColorMatrix

    override fun revertColors(): ColorMatrix

    fun warpPerspective(m: GrayMatrix, area: Area): ColorMatrix

    fun putText(digit: Digit, coord: Coordinate, fSize: Double, color: Color)
}