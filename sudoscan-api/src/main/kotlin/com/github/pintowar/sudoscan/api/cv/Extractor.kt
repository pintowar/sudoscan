package com.github.pintowar.sudoscan.api.cv

import com.github.pintowar.sudoscan.api.Digit
import mu.KLogging
import org.bytedeco.javacpp.indexer.FloatIndexer
import org.bytedeco.javacpp.indexer.IntIndexer
import org.bytedeco.javacpp.indexer.UByteIndexer
import org.bytedeco.opencv.global.opencv_core.*
import org.bytedeco.opencv.global.opencv_imgproc
import org.bytedeco.opencv.global.opencv_imgproc.COLOR_RGB2GRAY
import org.bytedeco.opencv.opencv_core.Mat
import kotlin.math.max
import kotlin.math.min

internal object Extractor : KLogging() {

    /**
     * Convert image to gray scale.
     *
     * @param img image to be converted.
     * @return gray scale image (Mat).
     */
    fun toGrayScale(img: Mat) = img.cvtColor(COLOR_RGB2GRAY)

    /**
     * Pre-process a gray scale image. Basically uses a gaussian blur + adaptive threshold.
     * It also can dilate the image (true by default).
     *
     * @param img image to pre-process.
     * @param dilate dilate flag (true by default).
     * @return pre processed image (Mat).
     */
    fun preProcessGrayImage(img: Mat, dilate: Boolean = true): Mat {
        assert(img.channels() == 1) { "Image must be in gray scale." }
        val proc = img.gaussianBlur(Area(9, 9), 0.0)

        val threshold = proc.adaptiveThreshold(
            255.0, opencv_imgproc.ADAPTIVE_THRESH_GAUSSIAN_C, opencv_imgproc.THRESH_BINARY, 11, 2.0
        )

        val thresholdNot = threshold.bitwiseNot()

        return if (dilate) {
            val kernel = getStructuringElement(opencv_imgproc.MORPH_DILATE, Area(3, 3))
            thresholdNot.dilate(kernel)
        } else thresholdNot
    }

    /**
     * Find corners of the biggest square found in the image.
     * The input image must be in grayscale.
     *
     * @param img gray scale image to find the biggest square corners.
     * @return the biggest square coordinates.
     */
    fun findCorners(img: Mat): ImageCorners {
        assert(img.channels() == 1) { "Image must be in gray scale." }

        val contours = img.findContours(Mat(), opencv_imgproc.RETR_EXTERNAL, opencv_imgproc.CHAIN_APPROX_SIMPLE)
        val polygons = contours.get()

        return if (polygons.isNotEmpty()) {
            val polygon = polygons.maxByOrNull { it.contourArea() }!!
            val points = polygon.createIndexer<IntIndexer>().use { idx ->
                (0 until idx.size(0)).map { Coord(idx.get(it, 0, 0), idx.get(it, 0, 1)) }
            }

            ImageCorners(
                bottomRight = points.maxByOrNull { it.x + it.y }!!,
                topLeft = points.minByOrNull { it.x + it.y }!!,
                bottomLeft = points.minByOrNull { it.x - it.y }!!,
                topRight = points.maxByOrNull { it.x - it.y }!!
            )
        } else ImageCorners.EMPTY_CORNERS
    }

    /**
     * This function changes an image perspective to a frontal view given a square corners coordinates.
     *
     * @param img original image.
     * @param corners square coordinates of the desired object.
     * @return img with a frontal view.
     */
    private fun frontalPerspective(img: Mat, corners: ImageCorners): CroppedImage {
        val sides = corners.sides()
        val side = sides.maxOrNull()!!.toFloat()

        val src = floatToMat(corners.toFloatArray())
        val dst = floatToMat(
            arrayOf(
                arrayOf(0.0f, 0.0f), arrayOf(side - 1, 0.0f), arrayOf(side - 1, side - 1), arrayOf(0.0f, side - 1)
            )
        )
        val m = src.getPerspectiveTransform(dst)
        val result = img.warpPerspective(m, Area(side.toInt(), side.toInt()))
        return CroppedImage(result, src, dst)
    }

    /**
     * This function has the objective to find the "biggest square" in the image, crop it and changes its perspective
     * so that the view is frontal.
     *
     * This function is a pipe the goes through other functions [toGrayScale] -> [preProcessGrayImage] -> [findCorners]
     * -> [frontalPerspective]
     *
     * @param img original image to be processed.
     * @return original image with a frontal view.
     */
    fun cropImage(img: Mat): CroppedImage {
        val gray = toGrayScale(img)
        val proc = preProcessGrayImage(gray)
        val corners = findCorners(proc)
        return frontalPerspective(gray, corners)
    }

    /**
     * Splits the image into a 9x9 (81) grid. Assuming an image with a frontal perspective of a Sudoku is provided,
     * a list of diagonal segments (top left to bottom right) is returned.
     *
     * @param img original image (for better function, assume an image with a frontal perspective of a Sudoku puzzle).
     * @return list of diagonal segments (top left to bottom right) of every sudoku piece.
     */
    fun splitSquares(img: Mat): List<Segment> {
        assert(img.arrayHeight() == img.arrayWidth())

        val numPieces = 9
        val side = img.arrayHeight().toDouble() / numPieces

        return (0 until numPieces).flatMap { i ->
            (0 until numPieces).map { j ->
                Segment(
                    Coord((j * side).toInt(), (i * side).toInt()),
                    Coord(((j + 1) * side).toInt(), ((i + 1) * side).toInt())
                )
            }
        }
    }

    /**
     * Rescale image given a new size and centralize the middle object (number).
     *
     * @param img image to adjust.
     * @param size new size (width and height) to be scaled.
     * @param margin margin to help on centralization.
     * @param background background color.
     * @return transformed image.
     */
    fun scaleAndCenter(img: Mat, size: Int, margin: Int = 0, background: Int = 0): Mat {

        fun centrePad(length: Int): Pair<Int, Int> {
            val side = (size - length) / 2
            return if (length % 2 == 0) side to side else side to (side + 1)
        }

        val isHigher = img.arrayHeight() > img.arrayWidth()
        val ratio = (size - margin).toDouble() / (if (isHigher) img.arrayHeight() else img.arrayWidth())
        val w = (ratio * img.arrayWidth()).toInt()
        val h = (ratio * img.arrayHeight()).toInt()
        val halfMargin = margin / 2

        val (tPad, bPad, lPad, rPad) = if (isHigher) {
            val (l, r) = centrePad(w)
            listOf(halfMargin, halfMargin, l, r)
        } else {
            val (t, b) = centrePad(h)
            listOf(t, b, halfMargin, halfMargin)
        }

        val aux = img.resize(Area(w, h))
        val aux2 = aux.copyMakeBorder(tPad, bPad, lPad, rPad, BORDER_CONSTANT, background.toDouble())
        return aux2.resize(Area(size, size))
    }

    /**
     * Cut a rectangle from an original image based on a back slash segment (top left to bottom right) or an empty
     * matrix in case of a **non** back slash informed.
     *
     * @param img original image.
     * @param segment a back slashed segment.
     * @return the cut image.
     */
    fun rectFromSegment(img: Mat, segment: Segment): Mat =
        if (segment.isBackSlash())
            img.colRange(segment.begin.x, segment.end.x).rowRange(segment.begin.y, segment.end.y)
        else
            img.colRange(0, 0).rowRange(0, 0)

    /**
     * Scans the image in search of any relevant data (on the sudoku context, it searches for a number in a cell).
     * The process assumes that invalid pixel is BLACK and valid pixel is white (for instance, imagine a black image
     * with a white eight in the middle).
     *
     * The scan uses a diagonal parameter that inform it the start area. This start area is a secure area with a
     * safe distance from the borders. For the sudoku context, when it scans a cell this is used to exclude its borders.
     * With the safe area informed, it marks all valid (white) data as gray.
     *
     * After the first step, it will then scan the full square (this time without the secure margin) and change all not
     * gray pixel to an invalid pixel (black) and all gray pixel to a valid (white) pixel.
     *
     * After this process it will detect the valid bounds of the final white object found.
     *
     * @param inputImg gray scale image to be scanned.
     * @param diagonal initial area with a safe margin from the borders.
     * @return a square containing the bounds of an object (number on sudoku context).
     */
    fun findLargestFeature(
        inputImg: Mat,
        diagonal: Segment = Segment(Coord(0, 0), Coord(inputImg.arrayWidth(), inputImg.arrayHeight()))
    ): ImageCorners {
        val img = inputImg.clone()
        val (black, gray, white) = listOf(0, 64, 255)
        return img.createIndexer<UByteIndexer>().use { indexer ->
            val size = img.size()

            (diagonal.begin.x until min(diagonal.end.x, size.width())).forEach { x ->
                (diagonal.begin.y until min(diagonal.end.y, size.height())).forEach { y ->
                    if (indexer[y.toLong(), x.toLong()] == white) {
                        img.floodFill(Coord(x, y), gray.toDouble())
                    }
                }
            }

            var (top, bottom, left, right) = listOf(size.height(), 0, size.width(), 0)

            (0 until size.width()).forEach { x ->
                (0 until size.height()).forEach { y ->
                    val color = if (indexer[y.toLong(), x.toLong()] != gray) black else white
                    indexer.put(y.toLong(), x.toLong(), color)

                    if (indexer[y.toLong(), x.toLong()] == white) {
                        top = min(x, top)
                        bottom = max(x, bottom)
                        left = min(y, left)
                        right = max(y, right)
                    }
                }
            }

            ImageCorners(Coord(top, left), Coord(top, right), Coord(bottom, right), Coord(bottom, left))
        }
    }

    /**
     * Extract sudoku cell information from an input image. For proper extraction, this must be a frontal view
     * of the sudoku puzzle.
     *
     * @param img input image (for proper extraction, this must be a frontal view of the sudoku puzzle).
     * @param segment back slash diagonal of the area to be extracted from the original image.
     * @param size final (and resized) size of the image extracted.
     * @return an object with the image extracted and additional information about the cell.
     */
    fun extractDigit(img: Mat, segment: Segment, size: Int): Digit {
        val digit = rectFromSegment(img, segment)

        val margin = ((digit.arrayWidth() + digit.arrayHeight()) / 5.0).toInt()

        val box = findLargestFeature(
            digit,
            Segment(Coord(margin, margin), Coord(digit.arrayWidth() - margin, digit.arrayHeight() - margin))
        )

        val noBorder = rectFromSegment(digit, box.diagonal())

        val area = noBorder.size(0) * noBorder.size(1)
        val percentFill = if (area > 0) (noBorder.sumElements() / 255) / area else 0.0
        logger.debug { "Percent: %.2f".format(percentFill) }

        return if (percentFill > 0.1) Digit(scaleAndCenter(noBorder, size, 4), false)
        else Digit(Mat.zeros(size, size, CV_8UC1).asMat(), true)
    }

    /**
     * Extract sudoku cells information from an input image. This function uses a list of segments as parameter.
     * For proper extraction, this must be a frontal view of the sudoku puzzle.
     *
     * @param img input image (for proper extraction, this must be a frontal view of the sudoku puzzle).
     * @param squares a list of back slash diagonals of the areas to be extracted from the original image.
     * @param size final (and resized) size of the image extracted.
     * @return an object with the image extracted and additional information about the cell.
     */
    fun extractAllDigits(img: Mat, squares: List<Segment>, size: Int = 28) =
        squares.map { s -> extractDigit(img, s, size) }

    /**
     * Convert a 2d array into an image.
     * @param data original 2d array.
     * @return converted matrix.
     */
    private fun floatToMat(data: Array<Array<Float>>): Mat {
        val mat = Mat(data.size, data[0].size, CV_32FC1)
        mat.createIndexer<FloatIndexer>().use { idx ->
            data.forEachIndexed { i, it ->
                idx.put(longArrayOf(i.toLong(), 0, 0), it[0])
                idx.put(longArrayOf(i.toLong(), 1, 0), it[1])
            }
        }
        return mat
    }
}
