package com.github.pintowar.sudoscan.opencv

import com.github.pintowar.sudoscan.api.ImageMatrix
import com.github.pintowar.sudoscan.api.cv.Area
import com.github.pintowar.sudoscan.api.cv.BBox
import com.github.pintowar.sudoscan.api.cv.CellIndex
import org.bytedeco.javacpp.indexer.UByteIndexer
import org.bytedeco.opencv.global.opencv_core.BORDER_CONSTANT
import org.bytedeco.opencv.global.opencv_imgcodecs
import org.bytedeco.opencv.global.opencv_imgproc
import org.bytedeco.opencv.opencv_core.Mat
import org.bytedeco.opencv.opencv_core.Size
import org.opencv.imgcodecs.Imgcodecs

@JvmInline
internal value class OpenCvMatrix(internal val mat: Mat) : ImageMatrix {

    override fun height() = mat.arrayHeight()
    override fun width() = mat.arrayWidth()
    override fun channels() = mat.channels()

    constructor(bytes: ByteArray) : this(opencv_imgcodecs.imdecode(Mat(*bytes), Imgcodecs.IMREAD_UNCHANGED))

    override fun toBytes(ext: String): ByteArray {
        return ByteArray(mat.channels() * mat.cols() * mat.rows()).also { bytes ->
            opencv_imgcodecs.imencode(".$ext", mat, bytes)
        }
    }

    override fun concat(img: ImageMatrix, horizontal: Boolean): ImageMatrix = when (img) {
        is OpenCvMatrix -> OpenCvMatrix(mat.concat(img.mat, horizontal))
        else -> throw InvalidImageInstance()
    }

    override fun resize(area: Area): ImageMatrix = OpenCvMatrix(
        Mat().also { dst -> opencv_imgproc.resize(mat, dst, Size(area.width, area.height)) }
    )

    override fun crop(bBox: BBox): ImageMatrix = OpenCvMatrix(mat.crop(bBox))

    override fun area(): Area = Area(mat.arrayWidth(), mat.arrayHeight())

    override fun countNonZero(): Int = mat.countNonZero()

    override fun toGrayScale(): ImageMatrix = OpenCvMatrix(mat.cvtColor(opencv_imgproc.COLOR_RGB2GRAY))

    override fun getPerspectiveTransform(dst: ImageMatrix): ImageMatrix = when (dst) {
        is OpenCvMatrix -> OpenCvMatrix(opencv_imgproc.getPerspectiveTransform(mat, dst.mat))
        else -> throw InvalidImageInstance()
    }

    override fun warpPerspective(m: ImageMatrix, area: Area): ImageMatrix = when (m) {
        is OpenCvMatrix -> OpenCvMatrix(
            Mat().also { dst -> opencv_imgproc.warpPerspective(mat, dst, m.mat, Size(area.width, area.height)) }
        )

        else -> throw InvalidImageInstance()
    }

    override fun bitwiseAnd(that: ImageMatrix): ImageMatrix = when (that) {
        is OpenCvMatrix -> OpenCvMatrix(mat.bitwiseAnd(that.mat))
        else -> throw InvalidImageInstance()
    }

    override fun revertColors(): ImageMatrix = OpenCvMatrix(mat.bitwiseNot())
//        OpenCvMatrix((if (channels() > 1) (toGrayScale() as OpenCvMatrix).mat else mat).bitwiseNot())

    override fun copyMakeBorder(top: Int, bottom: Int, left: Int, right: Int, background: Int): ImageMatrix =
        OpenCvMatrix(mat.copyMakeBorder(top, bottom, left, right, BORDER_CONSTANT, background.toDouble()))

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