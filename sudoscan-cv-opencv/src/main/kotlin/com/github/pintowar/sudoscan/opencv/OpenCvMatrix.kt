package com.github.pintowar.sudoscan.opencv

import com.github.pintowar.sudoscan.api.ColorMatrix
import com.github.pintowar.sudoscan.api.Digit
import com.github.pintowar.sudoscan.api.GrayMatrix
import com.github.pintowar.sudoscan.api.ImageMatrix
import com.github.pintowar.sudoscan.api.cv.*
import org.bytedeco.javacpp.indexer.FloatIndexer
import org.bytedeco.javacpp.indexer.IntIndexer
import org.bytedeco.javacpp.indexer.UByteIndexer
import org.bytedeco.opencv.global.opencv_core
import org.bytedeco.opencv.global.opencv_core.BORDER_CONSTANT
import org.bytedeco.opencv.global.opencv_imgcodecs
import org.bytedeco.opencv.global.opencv_imgproc
import org.bytedeco.opencv.global.opencv_imgproc.FONT_HERSHEY_DUPLEX
import org.bytedeco.opencv.global.opencv_imgproc.putText
import org.bytedeco.opencv.opencv_core.Mat
import org.bytedeco.opencv.opencv_core.Point
import org.bytedeco.opencv.opencv_core.Scalar
import org.bytedeco.opencv.opencv_core.Size
import org.opencv.imgcodecs.Imgcodecs
import java.awt.Color
import kotlin.math.max
import kotlin.math.min

internal abstract class OpenCvMatrix(internal val mat: Mat) : ImageMatrix {

    init {
        validate()
    }

    override fun height() = mat.arrayHeight()
    override fun width() = mat.arrayWidth()
    override fun channels() = mat.channels()

    override fun toBytes(ext: String): ByteArray {
        return ByteArray(mat.channels() * mat.cols() * mat.rows()).also { bytes ->
            opencv_imgcodecs.imencode(".$ext", mat, bytes)
        }
    }

    override fun countNonZero(): Int = mat.countNonZero()

    override fun findLargestFeature(bBox: BBox): RectangleCorners {
        val (black, gray, white) = listOf(0, 64, 255)

        return mat.createIndexer<UByteIndexer>(isNotAndroid).use { indexer ->
            (bBox.origin.x until min(bBox.origin.x + bBox.width, width())).forEach { x ->
                (bBox.origin.y until min(bBox.origin.y + bBox.height, height())).forEach { y ->
                    if (indexer[y.toLong(), x.toLong()] == white) {
                        mat.floodFill(Coordinate(x, y), gray.toDouble())
                    }
                }
            }

            var (top, bottom, left, right) = listOf(height(), 0, width(), 0).map { it.toLong() }
            val (width, height) = listOf(width(), height()).map { it.toLong() }

            (0 until width).forEach { x ->
                (0 until height).forEach { y ->
                    val color = if (indexer[y, x] != gray) black else white
                    indexer.put(y, x, color)

                    if (indexer[y, x] == white) {
                        top = min(x, top)
                        bottom = max(x, bottom)
                        left = min(y, left)
                        right = max(y, right)
                    }
                }
            }

            RectangleCorners(
                Coordinate(top, left), Coordinate(top, right), Coordinate(bottom, right), Coordinate(bottom, left)
            )
        }
    }

    override fun scanMatrix(callBack: (idx: CellIndex, value: Int) -> Unit) {
        mat.createIndexer<UByteIndexer>(isNotAndroid).use { idx ->
            for (c in 0 until channels().toLong()) {
                for (h in 0 until height().toLong()) {
                    for (w in 0 until width().toLong()) {
                        callBack(CellIndex(w, h, c), idx.get(h, w, c))
                    }
                }
            }
        }
    }
}

internal class GrayCvMatrix(mat: Mat) : OpenCvMatrix(mat), GrayMatrix {

    constructor(bytes: ByteArray) : this(opencv_imgcodecs.imdecode(Mat(*bytes), Imgcodecs.IMREAD_GRAYSCALE))

    override fun validate() {
        if (channels() != 1) throw IllegalArgumentException("A GrayMatrix must contains only one channel.")
    }

    override fun concat(img: ImageMatrix, horizontal: Boolean): ImageMatrix = when (img) {
        is GrayCvMatrix -> GrayCvMatrix(mat.concat(img.mat, horizontal))
        is ColorCvMatrix -> ColorCvMatrix(colored().mat.concat(img.mat, horizontal))
        else -> throw InvalidImageInstance()
    }

    override fun resize(area: Area): GrayMatrix = GrayCvMatrix(
        Mat().also { dst -> opencv_imgproc.resize(mat, dst, Size(area.width, area.height)) }
    )

    override fun clone(): GrayMatrix = GrayCvMatrix(mat.clone())

    override fun bitwiseAnd(that: ImageMatrix): GrayMatrix = when (that) {
        is GrayCvMatrix -> GrayCvMatrix(mat.bitwiseAnd(that.mat))
        else -> throw InvalidImageInstance()
    }

    override fun crop(bBox: BBox): GrayMatrix = GrayCvMatrix(mat.crop(bBox))

    override fun revertColors(): GrayMatrix = GrayCvMatrix(mat.bitwiseNot())

    override fun preProcessGrayImage(dilate: Boolean): GrayMatrix {
        val blurred = mat.gaussianBlur(Area(9), 0.0)

        val threshold = blurred.adaptiveThreshold(
            255.0, opencv_imgproc.ADAPTIVE_THRESH_GAUSSIAN_C, opencv_imgproc.THRESH_BINARY, 11, 2.0
        )

        val thresholdNot = threshold.bitwiseNot()

        return GrayCvMatrix(
            if (dilate) {
                val kernel = getStructuringElement(opencv_imgproc.MORPH_DILATE, Area(3))
                thresholdNot.dilate(kernel)
            } else thresholdNot
        )
    }

    override fun findCorners(): RectangleCorners {
        val polygons = mat.findContours(Mat(), opencv_imgproc.RETR_EXTERNAL, opencv_imgproc.CHAIN_APPROX_SIMPLE)

        return if (polygons.isNotEmpty()) {
            try {
                val polygon = polygons.maxByOrNull { it.contourArea() }!!
                val points = polygon.createIndexer<IntIndexer>(isNotAndroid).use { idx ->
                    (0 until idx.size(0)).map { Coordinate(idx.get(it, 0, 0), idx.get(it, 0, 1)) }
                }

                RectangleCorners(
                    bottomRight = points.maxByOrNull { it.x + it.y }!!,
                    topLeft = points.minByOrNull { it.x + it.y }!!,
                    bottomLeft = points.minByOrNull { it.x - it.y }!!,
                    topRight = points.maxByOrNull { it.x - it.y }!!
                )
            } catch (e: RuntimeException) {
                RectangleCorners.EMPTY_CORNERS
            }
        } else RectangleCorners.EMPTY_CORNERS
    }

    override fun frontalPerspective(corners: RectangleCorners): FrontalPerspective {
        val sides = corners.sides()
        val side = sides.maxOrNull()!!.toFloat()

        val src = floatToMat(corners.toFloatArray())
        val dst = floatToMat(
            arrayOf(
                floatArrayOf(0.0f, 0.0f),
                floatArrayOf(side - 1, 0.0f),
                floatArrayOf(side - 1, side - 1),
                floatArrayOf(0.0f, side - 1)
            )
        )
        val m = src.getPerspectiveTransform(dst)
        val result = mat.warpPerspective(m, Area(side.toInt()))
        return FrontalPerspective(GrayCvMatrix(result), GrayCvMatrix(src), GrayCvMatrix(dst))
    }

    override fun copyMakeBorder(top: Int, bottom: Int, left: Int, right: Int, background: Int): GrayCvMatrix =
        GrayCvMatrix(mat.copyMakeBorder(top, bottom, left, right, BORDER_CONSTANT, background.toDouble()))

    override fun getPerspectiveTransform(dst: GrayMatrix): GrayCvMatrix = when (dst) {
        is GrayCvMatrix -> GrayCvMatrix(opencv_imgproc.getPerspectiveTransform(mat, dst.mat))
        else -> throw InvalidImageInstance()
    }

    override fun removeGrid(): GrayCvMatrix = try {
        GrayCvMatrix(mat.subtract(onlyLines(mat, true)).subtract(onlyLines(mat, false)))
    } catch (e: RuntimeException) {
        this
    }

    override fun toGrayScale(): GrayMatrix = this

    override fun colored(): ColorCvMatrix = ColorCvMatrix(mat.colored())

    /**
     * Convert a 2d array into an image.
     * @param data original 2d array.
     * @return converted matrix.
     */
    private fun floatToMat(data: Array<FloatArray>): Mat {
        val mat = Mat(data.size, data[0].size, opencv_core.CV_32FC1)
        mat.createIndexer<FloatIndexer>(isNotAndroid).use { idx ->
            data.forEachIndexed { i, it ->
                idx.put(longArrayOf(i.toLong(), 0, 0), it[0])
                idx.put(longArrayOf(i.toLong(), 1, 0), it[1])
            }
        }
        return mat
    }

    private fun onlyLines(img: Mat, horizontal: Boolean = true): Mat {
        val size = if (horizontal) Area(img.arrayHeight() / 9, 1) else Area(1, img.arrayWidth() / 9)
        val struct = opencv_imgproc.getStructuringElement(opencv_imgproc.MORPH_RECT, Size(size.width, size.height))
        val grids = img.erode(struct).dilate(struct)

        grids.findContours(Mat(), opencv_imgproc.RETR_TREE, opencv_imgproc.CHAIN_APPROX_SIMPLE).forEach {
            val poly = Mat().also { rect -> opencv_imgproc.approxPolyDP(it, rect, 3.0, true) }
            val rect = opencv_imgproc.boundingRect(poly)
            val extRect = if (horizontal)
                BBox(0, max(0, rect.y() - 2), grids.arrayWidth(), rect.height() + 4)
            else
                BBox(max(0, rect.x() - 2), 0, rect.width() + 4, grids.arrayHeight())

            grids.floodRect(extRect)
        }

        return grids
    }
}

internal class ColorCvMatrix(mat: Mat) : OpenCvMatrix(mat), ColorMatrix {

    constructor(bytes: ByteArray) : this(opencv_imgcodecs.imdecode(Mat(*bytes), Imgcodecs.IMREAD_COLOR))

    override fun validate() {
        if (channels() != 3) throw IllegalArgumentException("A ColorMatrix must contains only 3 channels.")
    }

    override fun concat(img: ImageMatrix, horizontal: Boolean): ImageMatrix = when (img) {
        is ColorCvMatrix -> ColorCvMatrix(mat.concat(img.mat, horizontal))
        is GrayCvMatrix -> ColorCvMatrix(mat.concat(img.colored().mat, horizontal))
        else -> throw InvalidImageInstance()
    }

    override fun resize(area: Area): ColorMatrix = ColorCvMatrix(
        Mat().also { dst -> opencv_imgproc.resize(mat, dst, Size(area.width, area.height)) }
    )

    override fun clone(): ColorMatrix = ColorCvMatrix(mat.clone())

    override fun bitwiseAnd(that: ImageMatrix): ColorMatrix = when (that) {
        is ColorCvMatrix -> ColorCvMatrix(mat.bitwiseAnd(that.mat))
        else -> throw InvalidImageInstance()
    }

    override fun revertColors(): ColorCvMatrix = ColorCvMatrix(mat.bitwiseNot())

    override fun toGrayScale(): GrayMatrix = GrayCvMatrix(mat.cvtColor(opencv_imgproc.COLOR_RGB2GRAY))

    override fun colored(): ColorCvMatrix = this

    override fun warpPerspective(m: GrayMatrix, area: Area): ColorMatrix = when (m) {
        is GrayCvMatrix -> ColorCvMatrix(
            Mat().also { dst -> opencv_imgproc.warpPerspective(mat, dst, m.mat, Size(area.width, area.height)) }
        )
        else -> throw InvalidImageInstance()
    }

    override fun putText(digit: Digit, coord: Coordinate, fSize: Double, color: Color) {
        val font = FONT_HERSHEY_DUPLEX
        val scalar = Scalar(color.blue.toDouble(), color.green.toDouble(), color.red.toDouble(), 255.toDouble())
        putText(mat, "${digit.value}", Point(coord.x, coord.y), font, fSize, scalar)
    }
}