package com.github.pintowar.sudoscan.api.cv

import com.github.pintowar.sudoscan.api.SudokuCell
import org.bytedeco.javacpp.indexer.FloatIndexer
import org.bytedeco.javacpp.indexer.IntIndexer
import org.bytedeco.javacpp.indexer.UByteIndexer
import org.bytedeco.opencv.global.opencv_core.*
import org.bytedeco.opencv.global.opencv_imgproc
import org.bytedeco.opencv.global.opencv_imgproc.COLOR_RGB2GRAY
import org.bytedeco.opencv.opencv_core.Mat
import kotlin.math.max
import kotlin.math.min

/**
 * Object containing general functions to extract and transform the input image.
 */
internal object Extractor {

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
     * @return preprocessed image (Mat).
     */
    fun preProcessGrayImage(img: Mat, dilate: Boolean = true): Mat {
        assert(img.channels() == 1) { "Image must be in gray scale." }
        val proc = img.gaussianBlur(Area(9), 0.0)

        val threshold = proc.adaptiveThreshold(
            255.0, opencv_imgproc.ADAPTIVE_THRESH_GAUSSIAN_C, opencv_imgproc.THRESH_BINARY, 11, 2.0
        )

        val thresholdNot = threshold.bitwiseNot()

        return if (dilate) {
            val kernel = getStructuringElement(opencv_imgproc.MORPH_DILATE, Area(3))
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
    fun findCorners(img: Mat): RectangleCorners {
        assert(img.channels() == 1) { "Image must be in gray scale." }

        val contours = img.findContours(Mat(), opencv_imgproc.RETR_EXTERNAL, opencv_imgproc.CHAIN_APPROX_SIMPLE)
        val polygons = contours.get()

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

    /**
     * This function changes an image perspective to a frontal view given a square corners coordinates.
     *
     * @param img original image.
     * @param corners square coordinates of the desired object.
     * @return img with a frontal view.
     */
    private fun frontalPerspective(img: Mat, corners: RectangleCorners): FrontalPerspective {
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
        val result = img.warpPerspective(m, Area(side.toInt()))
        return FrontalPerspective(result, src, dst)
    }

    /**
     * This function has the objective to find the "biggest square" in the image, crop it and changes its perspective
     * so that the view is frontal.
     *
     * This function is a pipe the goes through other functions [toGrayScale] -> [preProcessGrayImage] -> [findCorners]
     * -> [frontalPerspective].  This function returns an [PreProcessPhases], that stores some images of the pipe.
     *
     * @param img original image to be processed.
     * @return original image with a frontal view.
     */
    fun preProcessPhases(img: Mat): PreProcessPhases {
        val gray = toGrayScale(img)
        val proc = preProcessGrayImage(gray)
        val corners = findCorners(proc)
        val frontal = frontalPerspective(gray, corners)

        return PreProcessPhases(gray, proc, frontal)
    }

    /**
     * Splits the image into a 9x9 (81) grid. Assuming an image with a frontal perspective of a Sudoku is provided,
     * a list of diagonal segments (top left to bottom right) is returned.
     *
     * @param img original image (for better function, assume an image with a frontal perspective of a Sudoku puzzle).
     * @return list of bounding boxes of every sudoku piece (cell).
     */
    fun splitSquares(img: Mat): List<BBox> {
        assert(img.arrayHeight() == img.arrayWidth())

        val numPieces = 9
        val side = img.arrayHeight().toDouble() / numPieces

        return (0 until numPieces).flatMap { i ->
            (0 until numPieces).map { j ->
                BBox(
                    Coordinate((j * side).toInt(), (i * side).toInt()),
                    Coordinate(((j + 1) * side).toInt(), ((i + 1) * side).toInt())
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
        return aux2.resize(Area(size))
    }

    /**
     * Cut a rectangle from an original image based on a bounding box or an empty
     * matrix in case of an empty bounding box informed.
     *
     * @param img original image.
     * @param bBox a bounding box.
     * @return the cut image.
     */
    fun rectFromSegment(img: Mat, bBox: BBox): Mat =
        if (bBox.isNotEmpty())
            Mat(img, bBox.toRect())
        else
            throw IllegalStateException("Segment is invalid.")

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
     * @param bBox initial bounding box with a safe margin from the borders.
     * @return a square containing the bounds of an object (number on sudoku context).
     */
    fun findLargestFeature(
        inputImg: Mat,
        bBox: BBox = BBox(Coordinate(0, 0), Coordinate(inputImg.arrayWidth(), inputImg.arrayHeight()))
    ): RectangleCorners {
        val img = inputImg.clone()
        val (black, gray, white) = listOf(0, 64, 255)
        return img.createIndexer<UByteIndexer>(isNotAndroid).use { indexer ->
            val size = img.size()

            (bBox.origin.x until min(bBox.origin.x + bBox.width, size.width())).forEach { x ->
                (bBox.origin.y until min(bBox.origin.y + bBox.height, size.height())).forEach { y ->
                    if (indexer[y.toLong(), x.toLong()] == white) {
                        img.floodFill(Coordinate(x, y), gray.toDouble())
                    }
                }
            }

            var (top, bottom, left, right) = listOf(size.height(), 0, size.width(), 0).map { it.toLong() }
            val (width, height) = listOf(size.width(), size.height()).map { it.toLong() }

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

    /**
     * Extract sudoku cell information from an input image. For proper extraction, this must be a frontal view
     * of the sudoku puzzle.
     *
     * @param img input image (for proper extraction, this must be a frontal view of the sudoku puzzle).
     * @param bBox bounding box of the area to be extracted from the original image.
     * @return an object with the image extracted and additional information about the cell.
     */
    fun extractCell(img: Mat, bBox: BBox): SudokuCell = try {
        val digit = rectFromSegment(img, bBox)
        val margin = ((digit.arrayWidth() + digit.arrayHeight()) / 5.0).toInt()

        val rect = findLargestFeature(
            digit,
            BBox(Coordinate(margin, margin), Coordinate(digit.arrayWidth() - margin, digit.arrayHeight() - margin))
        )

        val noBorder = rectFromSegment(digit, rect.bBox())
        SudokuCell(noBorder)
    } catch (e: RuntimeException) {
        SudokuCell.EMPTY
    }

    /**
     * Extract sudoku cells information from an input image. This function uses a list of segments as parameter.
     * For proper extraction, this must be a frontal view of the sudoku puzzle.
     *
     * @param img input image (for proper extraction, this must be a frontal view of the sudoku puzzle).
     * @return an object with the image extracted and additional information about the cell.
     */
    fun extractSudokuCells(img: Mat): List<SudokuCell> {
        val squares = splitSquares(img)
        return squares.map { s -> extractCell(img, s) }
    }

    /**
     * Convert a 2d array into an image.
     * @param data original 2d array.
     * @return converted matrix.
     */
    private fun floatToMat(data: Array<FloatArray>): Mat {
        val mat = Mat(data.size, data[0].size, CV_32FC1)
        mat.createIndexer<FloatIndexer>(isNotAndroid).use { idx ->
            data.forEachIndexed { i, it ->
                idx.put(longArrayOf(i.toLong(), 0, 0), it[0])
                idx.put(longArrayOf(i.toLong(), 1, 0), it[1])
            }
        }
        return mat
    }
}